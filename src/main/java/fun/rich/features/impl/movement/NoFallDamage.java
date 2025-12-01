package fun.rich.features.impl.movement;

import antidaunleak.api.annotation.Native;
import fun.rich.features.module.setting.implement.SelectSetting;
import fun.rich.events.packet.PacketEvent;
import fun.rich.utils.math.projection.Projection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.utils.client.managers.event.EventHandler;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NoFallDamage extends Module {

    SelectSetting mode = new SelectSetting("Режим", "Выберите тип")
            .value("SpookyTime")
            .selected("SpookyTime");

    public NoFallDamage() {
        super("NoFallDamage", "No Fall Damage", ModuleCategory.MOVEMENT);
        setup(mode);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onPacket(PacketEvent e) {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.fallDistance > 0 && Projection.getDistanceToGround() >4) {
            mc.player.setVelocity(0, 0, 0);
        }
    }
}
