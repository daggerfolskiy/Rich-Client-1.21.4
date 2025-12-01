package fun.rich.features.impl.misc;

import antidaunleak.api.annotation.Native;
import fun.rich.utils.features.aura.warp.Turns;
import fun.rich.utils.features.aura.warp.TurnsConfig;
import fun.rich.utils.features.aura.warp.TurnsConnection;
import fun.rich.utils.math.task.TaskPriority;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BindSetting;
import fun.rich.events.keyboard.HotBarScrollEvent;
import fun.rich.events.keyboard.KeyEvent;
import fun.rich.events.player.HotBarUpdateEvent;
import fun.rich.events.player.TickEvent;
import fun.rich.events.render.WorldRenderEvent;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.interactions.inv.InventoryTask;
import fun.rich.utils.math.time.StopWatch;
import fun.rich.utils.math.script.Script;
import fun.rich.utils.features.aura.utils.MathAngle;
import fun.rich.features.impl.render.ProjectilePrediction;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WindJump extends Module {
    private final Turns rot = new Turns(0, 0);
    BindSetting windChargeBind = new BindSetting("Заряд ветра", "Бросить заряд ветра");
    StopWatch stopWatch = new StopWatch();
    Script script = new Script();

    public WindJump() {
        super("WindJump", "Wind Jump", ModuleCategory.MISC);
        setup(windChargeBind);
    }

    @EventHandler
    public void onHotBarUpdate(HotBarUpdateEvent e) {
        if (!script.isFinished()) e.cancel();
    }

    @EventHandler
    public void onHotBarScroll(HotBarScrollEvent e) {
        if (!script.isFinished()) e.cancel();
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.isKeyReleased(windChargeBind.getKey())) {
            if (stopWatch.finished(0)) {
                InventoryTask.swapAndUse(Items.WIND_CHARGE);
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (PlayerInteractionHelper.isKey(windChargeBind)) {
            rot.setYaw(mc.player.getYaw());
            rot.setPitch(90);
            TurnsConnection.INSTANCE.rotateTo(rot, TurnsConfig.DEFAULT, TaskPriority.LOW_PRIORITY, this);
            ItemStack stack = Items.WIND_CHARGE.getDefaultStack();
            ProjectilePrediction.getInstance().drawPredictionInHand(e.getStack(), List.of(stack), MathAngle.cameraAngle());
        }
    }

    @EventHandler

    public void onTick(TickEvent e) {
        if (!script.isFinished() && stopWatch.every(250)) {
            script.update();
        }
    }
}