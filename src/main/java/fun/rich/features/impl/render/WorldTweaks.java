package fun.rich.features.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.MultiSelectSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.client.Instance;
import fun.rich.events.render.FogEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorldTweaks extends Module {
    public static WorldTweaks getInstance() {
        return Instance.get(WorldTweaks.class);
    }

    public final MultiSelectSetting modeSetting = new MultiSelectSetting("Настройки мира", "Позволяет настроить мир")
            .value("Bright", "Time", "Fog");

    public final SliderSettings brightSetting = new SliderSettings("Яркость", "Устанавливает значение максимальной яркости")
            .setValue(1.0F).range(0.0F, 1.0F).visible(() -> modeSetting.isSelected("Bright"));

    public final SliderSettings timeSetting = new SliderSettings("Время", "Устанавливает значение времени")
            .setValue(12).range(0, 24).visible(() -> modeSetting.isSelected("Time"));

    public final SliderSettings distanceSetting = new SliderSettings("Дистанция тумана", "Устанавливает расстояние тумана")
            .setValue(100).range(20, 200).visible(() -> modeSetting.isSelected("Fog"));

    public WorldTweaks() {
        super("WorldTweaks", "World Tweaks", ModuleCategory.RENDER);
        setup(modeSetting, brightSetting, timeSetting, distanceSetting);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @EventHandler
    public void onFog(FogEvent e) {
        if (modeSetting.isSelected("Fog")) {
            e.setDistance(distanceSetting.getValue());
            e.setColor(ColorAssist.getClientColor());
            e.cancel();
        }
    }
}
