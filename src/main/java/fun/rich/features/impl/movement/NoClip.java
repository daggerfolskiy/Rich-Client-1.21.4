package fun.rich.features.impl.movement;

import antidaunleak.api.annotation.Native;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.events.block.BlockCollisionEvent;
import fun.rich.events.packet.PacketEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoClip extends Module {
    @NonFinal List<Packet<?>> packets = new CopyOnWriteArrayList<>();
    @NonFinal Box box;
    @NonFinal int tickCounter;

    public NoClip() {
        super("No Clip", ModuleCategory.MOVEMENT);
    }

    private boolean shouldPhase() {
        if (mc.player == null || mc.world == null) return false;
        Box hitbox = mc.player.getBoundingBox();
        BlockPos min = new BlockPos((int) Math.floor(hitbox.minX), (int) Math.floor(hitbox.minY), (int) Math.floor(hitbox.minZ));
        BlockPos max = new BlockPos((int) Math.floor(hitbox.maxX), (int) Math.floor(hitbox.maxY), (int) Math.floor(hitbox.maxZ));
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    if (!state.isAir() && mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).getBoundingBoxes().stream().anyMatch(box -> box.intersects(hitbox.offset(-pos.getX(), -pos.getY(), -pos.getZ())))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void resumePackets() {
        if (mc.player == null || mc.world == null || state) return;
        if (!packets.isEmpty()) {
            for (Packet<?> packet : new ArrayList<>(packets)) {
                mc.getNetworkHandler().sendPacket(packet);
            }
            packets.clear();
            box = mc.player.getBoundingBox();
        }
    }

    @EventHandler
    public void onCollide(BlockCollisionEvent e) {
        if (!state) return;
        BlockPos playerPos = BlockPos.ofFloored(mc.player.getPos());
        if (e.getBlockPos().equals(playerPos.down())) return;
        e.setState(Blocks.AIR.getDefaultState());
    }

    private void adjustToSneakingSpeed() {
        if (mc.player == null || !state) return;
        final double SNEAK_SPEED_MULTIPLIER = 0.3;
        Vec3d motion = mc.player.getVelocity();
        double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontalSpeed > 0) {
            double baseSpeed = 0.6;
            double targetSpeed = baseSpeed * SNEAK_SPEED_MULTIPLIER;
            double scale = targetSpeed / horizontalSpeed;
            mc.player.setVelocity(motion.x * scale, motion.y, motion.z * scale);
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!state) return;
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        boolean onGround = mc.player.isOnGround();
        if (event.getType() == PacketEvent.Type.SEND) {
            Packet<?> p = event.getPacket();
            if (shouldPhase() && !(p instanceof KeepAliveC2SPacket) && !(p instanceof CommonPongC2SPacket)) {
                packets.add(p);
                event.cancel();
            }
        }
        if (event.getType() == PacketEvent.Type.RECEIVE && event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            resumePackets();
            Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(new PlayerMoveC2SPacket.Full(x - 1000, y, z - 1000, yaw, pitch, false, false));
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, mc.player.isOnGround(), false));

            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getX(),
                    mc.player.getY(),
                    mc.player.getZ(),
                    mc.player.getYaw(),
                    mc.player.getPitch(),
                    mc.player.isOnGround(),
                    false
            ));
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        resumePackets();
        box = null;
        tickCounter = 0;
    }
}