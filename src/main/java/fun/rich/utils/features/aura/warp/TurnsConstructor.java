package fun.rich.utils.features.aura.warp;

import fun.rich.features.impl.combat.Aura;
import fun.rich.features.impl.movement.Strafe;
import fun.rich.features.impl.movement.TargetStrafe;
import fun.rich.features.impl.player.AutoPilot;
import fun.rich.utils.features.aura.utils.MathAngle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.utils.features.aura.rotations.constructor.RotateConstructor;

@Setter
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TurnsConstructor implements QuickImports {
    Turns angle;
    Vec3d vec3d;
    Entity entity;
    RotateConstructor angleSmooth;
    int ticksUntilReset;
    float resetThreshold;
    public boolean moveCorrection;
    @Getter(AccessLevel.PUBLIC)
    public boolean freeCorrection;
    public boolean changeLook = (AutoPilot.getInstance().isState() && AutoPilot.getInstance().target !=null) ? true : (Aura.getInstance().isState() && Aura.getInstance().getTarget() !=null && Aura.getInstance().getCorrectionType().isSelected("Change Look")) ? true : false;


    public Turns nextRotation(Turns fromAngle, boolean isResetting) {
        if (isResetting) {
            return angleSmooth.limitAngleChange(fromAngle, MathAngle.fromVec2f(mc.player.getRotationClient()));
        }
        return angleSmooth.limitAngleChange(fromAngle, angle, vec3d, entity);
    }
}