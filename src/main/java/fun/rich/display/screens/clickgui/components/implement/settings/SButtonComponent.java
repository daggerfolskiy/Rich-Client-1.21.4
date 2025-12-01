package fun.rich.display.screens.clickgui.components.implement.settings;

import fun.rich.display.screens.clickgui.components.implement.other.ButtonComponent;
import net.minecraft.client.gui.DrawContext;

import fun.rich.features.module.setting.implement.ButtonSetting;
import fun.rich.utils.display.font.Fonts;

import static fun.rich.utils.display.font.Fonts.Type.BOLD;

public class SButtonComponent extends AbstractSettingComponent {
    private final ButtonComponent buttonComponent = new ButtonComponent();
    private final ButtonSetting setting;

    public SButtonComponent(ButtonSetting setting) {
        super(setting);
        this.setting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        height = 20;

        Fonts.getSize(14, BOLD).drawString(context.getMatrices(), setting.getName(), x + 9, y + 6, 0xFFD4D6E1);
//        Fonts.getSize(12).drawString(context.getMatrices(), wrapped, x + 9, y + 15, 0xFF878894);

        ((ButtonComponent) buttonComponent.setText("Click on me")
                .setRunnable(setting.getRunnable())
                .position(x + width - 9 - buttonComponent.width, y + 5))
                .render(context, mouseX, mouseY, delta);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        buttonComponent.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }
}