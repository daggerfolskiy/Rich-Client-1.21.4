package fun.rich.utils.features.aura.rotations.impl;

import fun.rich.Rich;
import fun.rich.features.impl.combat.Aura;
import fun.rich.utils.features.aura.point.Vector;
import fun.rich.utils.features.aura.rotations.constructor.RotateConstructor;
import fun.rich.utils.features.aura.striking.StrikeManager;
import fun.rich.utils.features.aura.warp.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import fun.rich.utils.features.aura.utils.MathAngle;

import java.security.SecureRandom;

public class SnapAngle extends RotateConstructor {
    public SnapAngle() {
        super("Snap");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        if (entity !=null && Aura.getInstance().getAimMode().isSelected("Snap")) {
            Vec3d aimPoint = Vector.hitbox(entity, 1, 1, 1, 2);
            targetAngle = MathAngle.calculateAngle(aimPoint);
        }
        StrikeManager attackHandler = Rich.getInstance().getAttackPerpetrator().getAttackHandler();
        Aura aura = Aura.getInstance();
        Turns angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean canAttack = entity != null && attackHandler.canAttack(aura.getConfig(), 0);

        float speed = 1f;

        float maxRotation = 180.0f;
        float lineYaw = (Math.abs(yawDelta / rotationDifference) * maxRotation);
        float linePitch = (Math.abs(pitchDelta / rotationDifference) * maxRotation);

        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(speed, currentAngle.getYaw(), currentAngle.getYaw() + moveYaw));
        moveAngle.setPitch(MathHelper.lerp(speed, currentAngle.getPitch(), currentAngle.getPitch() + movePitch));

        return new Turns(moveAngle.getYaw(), moveAngle.getPitch());
    }


    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
