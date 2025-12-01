package fun.rich.features.impl.player;

import fun.rich.events.player.RotationUpdateEvent;
import fun.rich.events.player.TickEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.MultiSelectSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.math.time.StopWatch;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class AntiAFK extends Module {

    StopWatch timer = new StopWatch();
    MultiSelectSetting multiSetting = new MultiSelectSetting("Режим", "Выберите, что будет происходить")
            .value("Walk", "Jump", "Spin", "Rotation change");

    SliderSettings time = new SliderSettings("Выполнять каждые (в минутах)", "пиф")
            .setValue(10).range(1F, 25F);

    private double yawTarget = 0;
    private boolean rotating = false;

    public AntiAFK() {
        super("AntiAFK", "Anti AFK", ModuleCategory.PLAYER);
        setup(multiSetting, time);
    }

    @Override
    public void activate() {
        timer.reset();
    }

    @Override
    public void deactivate() {
        rotating = false;
    }

    @EventHandler
    public void rotate(RotationUpdateEvent e) {
        if (rotating) {
            double diff = yawTarget - mc.player.getYaw();
            double diff2 = yawTarget - mc.player.getPitch();
            if (Math.abs(diff) > 1) {
                mc.player.setYaw((float) (mc.player.getYaw() + diff * 0.1));
                mc.player.setPitch((float) (mc.player.getPitch() + diff * 0.1));
            } else {
                rotating = false;
            }
        }
    }

    @EventHandler
    public void tick(TickEvent e) {
        if (PlayerInteractionHelper.nullCheck()) return;
        long intervalMs = (long) (time.getValue() * 60 * 100);
        if (timer.every(intervalMs)) {
            List<String> modes = multiSetting.getSelected();

            for (String mode : modes) {
                switch (mode) {
                    case "Walk":
                        walkForward();
                        break;
                    case "Jump":
                        if (mc.player.isOnGround()) {
                            mc.options.jumpKey.setPressed(true);
                        }
                        break;
                    case "Spin":
                        spin();
                        break;
                    case "Rotation change":
                        randomRotation();
                        break;
                    case "Break Block":
                        breakBlockUnder();
                        break;
                }
            }
        }
    }
    private Vec3d walkStartPos = null;
    private boolean walking = false;

    private void walkForward() {
        if (!walking) {
            walkStartPos = mc.player.getPos();
            walking = true;
        }

        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);

        Vec3d look = mc.player.getRotationVector();
        BlockPos target = mc.player.getBlockPos().add((int) look.x * 5, 0, (int) look.z * 5);

        double distance = mc.player.getPos().distanceTo(walkStartPos);

        if (distance >= 5) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            walking = false;
            walkStartPos = null;
            return;
        }

        if (!mc.world.getBlockState(target).isAir()) {
            mc.options.backKey.setPressed(true);
        } else {
            mc.options.forwardKey.setPressed(true);
        }
    }

    private void spin() {
        yawTarget = mc.player.getYaw() + 360;
        rotating = true;
    }

    private void randomRotation() {
        yawTarget = mc.player.getYaw() + (Math.random() * 360 - 180);
        rotating = true;
    }
    private BlockPos breakingBlockPos = null;

    private void breakBlockUnder() {
        BlockPos pos = mc.player.getBlockPos().down();
        BlockState block = mc.world.getBlockState(pos);

        if (block.isAir()) {
            mc.options.attackKey.setPressed(false);
            breakingBlockPos = null;
            return;
        }

        if (!pos.equals(breakingBlockPos)) {
            breakingBlockPos = pos;
        }

        mc.options.attackKey.setPressed(true);
    }
}
