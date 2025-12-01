package fun.rich.display.screens.clickgui.components.implement.settings;

import net.minecraft.client.gui.DrawContext;

import fun.rich.features.module.setting.implement.BooleanSetting;
import fun.rich.utils.display.font.Fonts;
import fun.rich.display.screens.clickgui.components.implement.other.CheckComponent;

import java.awt.*;

import static fun.rich.utils.display.font.Fonts.Type.*;
import static fun.rich.utils.display.font.Fonts.Type.DEFAULT;

public class CheckboxComponent extends AbstractSettingComponent {
    private final CheckComponent checkComponent = new CheckComponent();
    private final BooleanSetting setting;

    public CheckboxComponent(BooleanSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        height = 15;

        Fonts.getSize(20, GUIICONS).drawString(context.getMatrices(), "K", x + 6, y + 10f, new Color(128, 128, 128, 64).getRGB());

        Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), setting.getName(), x + 20, y + 11f, 0xFFD4D6E1);

        ((CheckComponent) checkComponent.position(x + width - 19, y + 6))
                .setRunnable(() -> setting.setValue(!setting.isValue()))
                .setState(setting.isValue())
                .render(context, mouseX, mouseY, delta);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        checkComponent.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
