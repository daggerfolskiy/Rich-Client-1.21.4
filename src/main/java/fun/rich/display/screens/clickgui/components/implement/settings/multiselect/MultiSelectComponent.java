package fun.rich.display.screens.clickgui.components.implement.settings.multiselect;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import fun.rich.utils.display.scissor.ScissorAssist;
import fun.rich.features.module.setting.implement.MultiSelectSetting;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.Rich;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static fun.rich.utils.display.font.Fonts.Type.*;

public class MultiSelectComponent extends AbstractSettingComponent {
    private final List<MultiSelectedButton> multiSelectedButtons = new ArrayList<>();
    private final MultiSelectSetting setting;
    private boolean open;
    private float dropdownListX, dropDownListY, dropDownListWidth, dropDownListHeight;
    private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(1);
    private final Animation heightAnimation = new Decelerate().setMs(200).setValue(0);

    public MultiSelectComponent(MultiSelectSetting setting) {
        super(setting);
        this.setting = setting;
        alphaAnimation.setDirection(Direction.BACKWARDS);
        heightAnimation.setDirection(Direction.BACKWARDS);
        for (String s : setting.getList()) {
            multiSelectedButtons.add(new MultiSelectedButton(setting, s));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        float baseHeight = 20;
        List<String> fullSettingsList = setting.getList();
        this.dropdownListX = x + width - 75;
        this.dropDownListY = y + 24;
        this.dropDownListWidth = 66;
        this.dropDownListHeight = fullSettingsList.size() * 12;
        if (open) {
            alphaAnimation.setDirection(Direction.FORWARDS);
            heightAnimation.setDirection(Direction.FORWARDS);
            heightAnimation.setValue(dropDownListHeight);
        } else {
            alphaAnimation.setDirection(Direction.BACKWARDS);
            heightAnimation.setDirection(Direction.BACKWARDS);
        }
        height = (int) (baseHeight + heightAnimation.getOutput().floatValue() + (open ? 5 : 0));
        renderSelected(matrices);
        if (!alphaAnimation.isFinished(Direction.BACKWARDS)) renderSelectList(context, mouseX, mouseY, delta);
        Fonts.getSize(20, GUIICONS).drawString(matrices, "I", x + 6, y + 12f, new Color(128, 128, 128, 64).getRGB());
        Fonts.getSize(12, DEFAULT).drawString(matrices, setting.getName(), x + 19, y + 13f, 0xFFD4D6E1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (Calculate.isHovered(mouseX, mouseY, x + width - 75, y + 8, 66, 14)) {
                open = !open;
            } else if (open && !isHoveredList(mouseX, mouseY)) {
                open = false;
            }
            if (open) {
                multiSelectedButtons.forEach(selectedButton -> selectedButton.mouseClicked(mouseX, mouseY, button));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return open && isHoveredList(mouseX, mouseY);
    }

    private void renderSelected(MatrixStack matrix) {
        FontRenderer font = Fonts.getSize(12);
        int x1 = (int) (x + width - 72);
        rectangle.render(ShapeProperties.create(matrix, x + width - 75, y + 7, 66, 14)
                .round(3).thickness(2).outlineColor(new Color(35, 52, 55, 155).getRGB())
                .color(
                        new Color(15, 15, 15, 0).getRGB(),
                        new Color(15, 15, 15, 0).getRGB(),
                        new Color(15, 15, 15, 0).getRGB(),
                        new Color(15, 15, 15, 0).getRGB())
                .build());
        String selectedName = String.join(", ", setting.getSelected());
        float offset = 64;
        ScissorAssist scissor = Rich.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), x1 - 2, y + 4, 64, 14);
        font.drawStringWithScroll(matrix, selectedName, x1, y + 13, offset, new Color(225, 225, 225, 225).getRGB());
        scissor.pop();
//        if (font.getStringWidth(selectedName) - offset > 0) {
//            rectangle.render(ShapeProperties.create(matrix, x + width - 13F, y + 7, 6, 14)
//                    .round(2.5f).softness(1).color(new Color(115, 115, 115, 190).getRGB()).build());
//            rectangle.render(ShapeProperties.create(matrix, x1 - 2.5F, y + 7, 6, 14)
//                    .round(0,0,2.5f,2.5f).softness(12).color(new Color(115, 115, 115, 190).getRGB()).build());
//        }
    }

    private void renderSelectList(DrawContext context, int mouseX, int mouseY, float delta) {
        float opacity = alphaAnimation.getOutput().floatValue();
        int alpha = (int) (opacity * 0);
        float animatedHeight = heightAnimation.getOutput().floatValue();
        rectangle.render(ShapeProperties.create(context.getMatrices(), dropdownListX, dropDownListY, dropDownListWidth, animatedHeight)
                .round(3).thickness(2).outlineColor(new Color(55, 52, 55, 155).getRGB())
                .color(
                        new Color(15, 15, 15, alpha).getRGB(),
                        new Color(15, 15, 15, alpha).getRGB(),
                        new Color(15, 15, 15, alpha).getRGB(),
                        new Color(15, 15, 15, alpha).getRGB())
                .build());
        float offset = dropDownListY;
        for (MultiSelectedButton button : multiSelectedButtons) {
            button.x = dropdownListX;
            button.y = offset;
            button.width = dropDownListWidth;
            button.height = 12;
            button.setAlpha(opacity);
            if (offset - dropDownListY < animatedHeight) {
                button.render(context, mouseX, mouseY, delta);
            }
            offset += 12;
        }
    }

    private boolean isHoveredList(double mouseX, double mouseY) {
        return Calculate.isHovered(mouseX, mouseY, dropdownListX, dropDownListY - 16, dropDownListWidth, dropDownListHeight + 16);
    }
}