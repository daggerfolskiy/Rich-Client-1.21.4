package fun.rich.features.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.client.sound.SoundManager;
import fun.rich.Rich;
import fun.rich.features.module.setting.SettingRepository;
import fun.rich.utils.client.managers.event.EventManager;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.display.hud.Notifications;
import fun.rich.features.impl.render.Hud;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Module extends SettingRepository implements QuickImports {
    String name;
    String visibleName;
    ModuleCategory category;
    Animation animation = new Decelerate().setMs(175).setValue(1);

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
        this.visibleName = name;
    }

    public Module(String name, String visibleName, ModuleCategory category) {
        this.name = name;
        this.visibleName = visibleName;
        this.category = category;
    }

    @NonFinal
    int key = GLFW.GLFW_KEY_UNKNOWN, type = 1;

    @NonFinal
    public boolean state;

    public void switchState() {
        setState(!state);
    }

    public void setState(boolean state) {
        animation.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
        if (state != this.state) {
            this.state = state;
            handleStateChange();
        }
    }

    private void handleStateChange() {
        MinecraftClient mc = MinecraftClient.getInstance();
        float volume = Hud.getInstance().getModuleVolume();

        if (mc.player != null && mc.world != null) {
            if (state) {
                if (Hud.getInstance().notificationSettings.isSelected("Module Switch")) {
                    Notifications.getInstance().addList("Feature " + Formatting.GRAY + visibleName + Formatting.RESET + " - enabled!", 2000, null);
                    SoundManager.playSound(SoundManager.ENABLE_MODULE, volume, 1.0f);
                }
                activate();
            } else {
                if (Hud.getInstance().notificationSettings.isSelected("Module Switch")) {
                    Notifications.getInstance().addList("Feature " + Formatting.GRAY + visibleName + Formatting.RESET + " - disabled!", 2000, null);
                    SoundManager.playSound(SoundManager.DISABLE_MODULE, volume, 1.0f);
                }
                deactivate();
            }
        }
        toggleSilent(state);
    }

    private void toggleSilent(boolean activate) {
        EventManager eventManager = Rich.getInstance().getEventManager();
        if (activate) {
            eventManager.register(this);
        } else {
            eventManager.unregister(this);
        }
    }

    public void activate() {
    }

    public void deactivate() {
    }
}
