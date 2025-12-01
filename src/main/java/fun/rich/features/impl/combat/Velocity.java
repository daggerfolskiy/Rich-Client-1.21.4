package fun.rich.features.impl.combat;

import antidaunleak.api.annotation.Native;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.SelectSetting;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.events.packet.PacketEvent;
import fun.rich.events.player.TickEvent;
import fun.rich.utils.client.Instance;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Velocity extends Module {
    public static Velocity getInstance() {
        return Instance.get(Velocity.class);
    }

    SelectSetting mode = new SelectSetting("Режим", "Выберите режим уменьшения отдачи")
            .value("NewGrim", "OldGrim", "Matrix", "Normal")
            .selected("NewGrim");

    @NonFinal boolean flag;
    @NonFinal int grimTicks;
    @NonFinal int ccCooldown;

    public Velocity() {
        super("Velocity", ModuleCategory.COMBAT);
        setup(mode);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (!state) return;
        if (e.getType() != PacketEvent.Type.RECEIVE) return;
        if (mc.player == null || mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava()) return;
        if (ccCooldown > 0) {
            ccCooldown--;
            return;
        }

        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket pac && pac.getEntityId() == mc.player.getId()) {
            switch (mode.getSelected()) {
                case "Matrix":
                    if (!flag) {
                        e.setCancelled(true);
                        flag = true;
                    } else {
                        flag = false;
                        setVelocityX(pac, (int) (pac.getVelocityX() * -0.1));
                        setVelocityZ(pac, (int) (pac.getVelocityZ() * -0.1));
                    }
                    break;
                case "Normal":
                    e.setCancelled(true);
                    break;
                case "OldGrim":
                    e.setCancelled(true);
                    grimTicks = 6;
                    break;
                case "NewGrim":
                    e.setCancelled(true);
                    flag = true;
                    break;
            }
        }

        if (mode.isSelected("OldGrim") && e.getPacket() instanceof CommonPingS2CPacket && grimTicks > 0) {
            e.setCancelled(true);
            grimTicks--;
        }

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket && mode.isSelected("NewGrim")) {
            ccCooldown = 5;
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (!state || mc.player == null || mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) return;

        if (mode.isSelected("Matrix") && mc.player.hurtTime > 0 && !mc.player.isOnGround()) {
            double var3 = mc.player.getYaw() * 0.017453292F;
            double var5 = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
            mc.player.setVelocity(-Math.sin(var3) * var5, mc.player.getVelocity().y, Math.cos(var3) * var5);
            mc.player.setSprinting(mc.player.age % 2 != 0);
        }

        if (mode.isSelected("NewGrim") && flag) {
            if (ccCooldown <= 0) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), mc.player.isOnGround(), false));
                mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, BlockPos.ofFloored(mc.player.getPos()), Direction.DOWN));
            }
            flag = false;
        }

        if (grimTicks > 0) {
            grimTicks--;
        }
    }

    @Override
    public void activate() {
        super.activate();
        grimTicks = 0;
        flag = false;
        ccCooldown = 0;
    }

    private void setVelocityX(EntityVelocityUpdateS2CPacket packet, int value) {
        try {
            java.lang.reflect.Field field = EntityVelocityUpdateS2CPacket.class.getDeclaredField("velocityX");
            field.setAccessible(true);
            field.setInt(packet, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setVelocityZ(EntityVelocityUpdateS2CPacket packet, int value) {
        try {
            java.lang.reflect.Field field = EntityVelocityUpdateS2CPacket.class.getDeclaredField("velocityZ");
            field.setAccessible(true);
            field.setInt(packet, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}