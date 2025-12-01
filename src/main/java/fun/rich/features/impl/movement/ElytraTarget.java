package fun.rich.features.impl.movement;

import com.google.common.eventbus.Subscribe;
import fun.rich.display.hud.Notifications;
import fun.rich.events.keyboard.KeyEvent;
import fun.rich.features.impl.render.Hud;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BindSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.client.Instance;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.client.sound.SoundManager;
import net.minecraft.util.Formatting;

public class ElytraTarget extends Module {

    public static ElytraTarget getInstance() {
        return Instance.get(ElytraTarget.class);
    }

    public SliderSettings elytraFindRange = new SliderSettings("Дистанция наводки", "Дальность поиска цели во время полета на элитре")
            .setValue(32).range(6F, 64F);

    public SliderSettings elytraForward = new SliderSettings("Значение перегона", "заебался")
            .setValue(3).range(0F, 6F);

    final BindSetting forward = new BindSetting("Кнопка вкл/выкл перегона", "ужас");

    public static boolean shouldElytraTarget = false;

    public ElytraTarget() {
        super("ElytraTarget", "Elytra Target", ModuleCategory.MOVEMENT);
        setup(elytraFindRange, elytraForward, forward);
    }

    @EventHandler
    private void onEventKey(KeyEvent e) {
        if (e.isKeyDown(forward.getKey())) {
            float volume = Hud.getInstance().getModuleVolume();
            shouldElytraTarget = !shouldElytraTarget;
            Notifications.getInstance().addList("Elytra Forward " + (shouldElytraTarget ? "enabled!" : "disabled"), 1500, null);
            SoundManager.playSound(shouldElytraTarget ? SoundManager.ENABLE_MODULE : SoundManager.DISABLE_MODULE, volume, 1.0f);
        }
    }

}