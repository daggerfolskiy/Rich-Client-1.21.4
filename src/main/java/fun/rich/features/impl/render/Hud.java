package fun.rich.features.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.features.module.setting.implement.ColorSetting;
import fun.rich.features.module.setting.implement.MultiSelectSetting;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.utils.client.Instance;

import java.awt.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Hud extends Module {
    public static Hud getInstance() {
        return Instance.get(Hud.class);
    }

    public MultiSelectSetting interfaceSettings = new MultiSelectSetting("Элементы", "Настройка элементов интерфейса").value("Watermark", "Hot Keys", "Potions", "Staff List", "Target Hud", "Binds", "Cool Downs", "Inventory", "Player Info", "Notifications", "Hot Keys")
            .selected("Watermark", "Hot Keys", "Potions", "Staff List", "Target Hud", "Binds", "Cool Downs", "Inventory", "Player Info", "Notifications" );

    public MultiSelectSetting notificationSettings = new MultiSelectSetting("Уведомления", "Выберите, когда будут появляться уведомления")
            .value("Module Switch", "Staff Join", "Staff Leave", "Item Pick Up", "Auto Armor", "Break Shield").selected("Module Switch", "Item Pick Up", "Auto Armor", "Break Shield").visible(()-> interfaceSettings.isSelected("Notifications"));

    public ColorSetting colorSetting = new ColorSetting("Изменяет цвет некоторых модулей", "Выберите цвет клиента")
            .setColor(new Color(255, 101, 57, 255).getRGB()).presets(0xFF6C9AFD, 0xFF8C7FFF, 0xFFFFA576, 0xFFFF7B7B);

    public SliderSettings soundVolumeSetting = new SliderSettings("Sound Volume", "Volume for module switch sounds")
            .range(0.0f, 1.0f)
            .setValue(1.0f)
            .visible(() -> interfaceSettings.isSelected("Notifications"));

    public float getModuleVolume() {
        return soundVolumeSetting.getValue();
    }

    public Hud() {
        super("Hud", ModuleCategory.RENDER);
        setup(colorSetting, interfaceSettings, notificationSettings, soundVolumeSetting);
    }
}