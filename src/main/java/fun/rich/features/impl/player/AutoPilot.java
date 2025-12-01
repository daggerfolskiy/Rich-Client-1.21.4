package fun.rich.features.impl.player;

import antidaunleak.api.annotation.Native;
import com.google.common.eventbus.Subscribe;
import fun.rich.events.player.TickEvent;
import fun.rich.features.impl.combat.Aura;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.utils.client.Instance;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.features.aura.utils.MathAngle;
import fun.rich.utils.features.aura.warp.Turns;
import fun.rich.utils.features.aura.warp.TurnsConfig;
import fun.rich.utils.features.aura.warp.TurnsConnection;
import fun.rich.utils.math.task.TaskPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector2d;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AutoPilot extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public ItemEntity target;
    private float lastYaw, lastPitch;
    private float targetYaw, targetPitch;
    Turns rot = new Turns(0, 0);

    public AutoPilot() {
        super("AutoPilot", "Auto Pilot", ModuleCategory.MISC);
    }
    public static AutoPilot getInstance() {
        return Instance.get(AutoPilot.class);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) {
            target = null;
            return;
        }

        target = findTarget();

        if (target != null) {
            double dx = target.getPos().getX() - mc.player.getPos().getX();
            double dy = (target.getPos().getY()) - (mc.player.getPos().getY() + mc.player.getEyeHeight(mc.player.getPose()));
            double dz = target.getPos().getZ() - mc.player.getPos().getZ();

            targetYaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI - 90.0);
            targetPitch = (float) (-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0 / Math.PI);

            float maxRotation = 1024;

            float yawDiff = MathHelper.wrapDegrees(targetYaw - lastYaw);
            float yawStep = MathHelper.clamp(yawDiff, -maxRotation, maxRotation);
            lastYaw += yawStep;

            float pitchDiff = MathHelper.wrapDegrees(targetPitch - lastPitch);
            float pitchStep = MathHelper.clamp(pitchDiff, -maxRotation, maxRotation);
            lastPitch += pitchStep;
            mc.player.setYaw(lastYaw);
            mc.player.setPitch(lastPitch);
            rot.setYaw(lastYaw);
            rot.setPitch(lastPitch);
            TurnsConnection.INSTANCE.rotateTo(rot, TurnsConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_1, this);
        } else {
            lastYaw = mc.player.getYaw();
            lastPitch = mc.player.getPitch();
        }
    }

    private ItemEntity findTarget() {
        List<ItemEntity> items = mc.world.getEntitiesByClass(ItemEntity.class,
                        mc.player.getBoundingBox().expand(50.0),
                        e -> e.isAlive() && isValidItem(e))
                .stream()
                .sorted(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e)))
                .collect(Collectors.toList());
        return items.isEmpty() ? null : items.get(0);
    }

    private boolean isValidItem(ItemEntity item) {
        var stack = item.getStack();
        return stack.getItem() == Items.SPAWNER ||
                stack.getItem() == Items.PLAYER_HEAD ||
                stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE ||
                stack.getItem().toString().contains("_spawn_egg");
    }

    public void deactivate() {
        target = null;
        if (mc.player != null) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getPos().getX(),
                    mc.player.getPos().getY(),
                    mc.player.getPos().getZ(),
                    mc.player.getYaw(),
                    mc.player.getPitch(),
                    mc.player.isOnGround(),
                    false
            ));
        }
    }
}