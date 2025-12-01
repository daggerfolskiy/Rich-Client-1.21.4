package fun.rich.display.screens.clickgui.components.implement.module;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.glfw.GLFW;
import fun.rich.features.module.Module;
import fun.rich.features.module.setting.SettingComponentAdder;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.shape.implement.Rectangle;
import fun.rich.display.screens.clickgui.MenuScreen;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.display.screens.clickgui.components.implement.other.StatusRender;
import fun.rich.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.client.chat.StringHelper;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.implement.Decelerate;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static fun.rich.utils.display.font.Fonts.Type.*;
import static fun.rich.common.animation.Direction.*;

@Getter
public class ModuleComponent extends AbstractComponent {
    private final List<AbstractSettingComponent> components = new ArrayList<>();
    private final StatusRender statusRender = new StatusRender();
    private final Module module;
    private boolean binding;
    private final Rectangle rectangle = new Rectangle();
    private final Animation colorAnimation = new Decelerate().setMs(400).setValue(9);
    private final Animation alphaAnimation = new Decelerate().setMs(400).setValue(105);

    public ModuleComponent(Module module) {
        this.module = module;
        new SettingComponentAdder().addSettingComponent(module.settings(), components);
        colorAnimation.setDirection(module.isState() ? FORWARDS : BACKWARDS);
        alphaAnimation.setDirection(module.isState() ? FORWARDS : BACKWARDS);
        colorAnimation.reset();
        alphaAnimation.reset();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean noSettings = module.settings().isEmpty();
        float headerHeight = noSettings ? 18 : 18;
        float nameY = noSettings ? 9.5F : 8;
        java.lang.String point = "• ";
        java.lang.String description = ModuleDescriptions.getDescription(module);
        float maxWidth = width - 25;
        float currentX = x + 10;
        float currentY = y + 15;
        java.lang.String[] words = description.split(" ");
        StringBuilder line = new StringBuilder();
        int lineCount = 1;

        for (java.lang.String word : words) {
            float wordWidth = Fonts.getSize(12, DEFAULT).getStringWidth(word + " ");
            if (currentX + wordWidth > x + maxWidth) {
                lineCount++;
                currentX = x + 10;
            }
            currentX += wordWidth;
        }

        float descHeight = lineCount == 1 ? lineCount * Fonts.getSize(12, DEFAULT).getStringHeight(" ") - 13 : lineCount * Fonts.getSize(12, DEFAULT).getStringHeight(" ") - 20;

        colorAnimation.setDirection(module.isState() ? FORWARDS : BACKWARDS);
        alphaAnimation.setDirection(module.isState() ? FORWARDS : BACKWARDS);
        int brightnessOffset = colorAnimation.getOutput().intValue();
        int alphaOffset = 150 + alphaAnimation.getOutput().intValue();

        blur.render(ShapeProperties.create(context.getMatrices(), x, y, width, height = getComponentHeight())
                .round(5)
                .color(new Color(0, 0, 0, 200).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, width, height = getComponentHeight())
                .round(5)
                .color(
                        new Color(23, 24, 25, 145).getRGB(),
                        new Color(Math.min(19 + brightnessOffset, 255), Math.min(19 + brightnessOffset, 255), Math.min(21 + brightnessOffset, 255), 255).getRGB(),
                        new Color(10, 12, 15, 145).getRGB(),
                        new Color(Math.min(19 + brightnessOffset, 255), Math.min(19 + brightnessOffset, 255), Math.min(21 + brightnessOffset, 255), 255).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y + descHeight + 25, width, 1)
                .color(new Color(25, 25, 40, 155).getRGB(), new Color(55, 55, 60, 155).getRGB(), new Color(55, 55, 60, 155).getRGB(), new Color(25, 25, 40, 155).getRGB())
                .build());

        if (!module.settings().isEmpty()) {
            Fonts.getSize(18, GUIICONS).drawString(context.getMatrices(), "A", x + 7, y + descHeight + 6F + 27f, new Color(128, 128, 128, 255).getRGB());
            Fonts.getSize(16, GUIICONS).drawString(context.getMatrices(), "B", x + 20, y + descHeight + 6F + 27.5f, new Color(128, 128, 128, 255).getRGB());
        } else {
            Fonts.getSize(18, GUIICONS).drawString(context.getMatrices(), "A", x + 7, y + descHeight + 6F + 27f, new Color(128, 128, 128, 255).getRGB());
        }

        statusRender.position(x + width - 16, y + descHeight + 5.5F + 25.5f)
                .setRunnable(module::switchState)
                .setState(module.isState())
                .render(context, mouseX, mouseY, delta);

        Fonts.getSize(15, DEFAULT).drawString(context.getMatrices(), point + module.getVisibleName(), x + 11, y + nameY - 1f, new Color(255, 255, 255, alphaOffset).getRGB());

        currentX = x + 10;
        currentY = y + 19;
        line = new StringBuilder();
        int currentLine = 1;

        for (java.lang.String word : words) {
            float wordWidth = Fonts.getSize(12, DEFAULT).getStringWidth(word + " ");
            if (currentX + wordWidth > x + maxWidth) {
                if (currentLine == 1) {
                    Fonts.getSize(14, GUIICONS).drawString(context.getMatrices(), "C", x + 6.5f, currentY + 0.5f, new Color(128, 128, 128, 255).getRGB());
                    Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), line.toString(), x + 15, currentY, new Color(128, 128, 128, 186).getRGB());
                } else {
                    Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), line.toString(), x + 5, currentY, new Color(128, 128, 128, 186).getRGB());
                }
                line = new StringBuilder();
                currentY += Fonts.getSize(12, DEFAULT).getStringHeight(word) - 7;
                currentX = x + 10;
                currentLine++;
            }
            line.append(word).append(" ");
            currentX += wordWidth;
        }

        if (!line.isEmpty()) {
            if (currentLine == 1) {
                Fonts.getSize(14, GUIICONS).drawString(context.getMatrices(), "C", x + 6.5f, currentY + 0.5f, new Color(128, 128, 128, 255).getRGB());
                Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), line.toString(), x + 15, currentY, new Color(128, 128, 128, 186).getRGB());
            } else {
                Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), line.toString(), x + 7, currentY, new Color(128, 128, 128, 186).getRGB());
            }
        }

        drawBind(context, descHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, width, getComponentHeight()) && button == 1 && !module.settings().isEmpty()) {
            if (MenuScreen.windowManager.getWindows().stream().noneMatch(w -> w instanceof ModuleSettingsWindow && ((ModuleSettingsWindow) w).module.equals(module))) {
                ModuleSettingsWindow settingsWindow = new ModuleSettingsWindow(module);
                settingsWindow.position(MenuScreen.INSTANCE.x + MenuScreen.INSTANCE.width + 24, MenuScreen.INSTANCE.y).size(160, settingsWindow.getComponentHeight());
                MenuScreen.windowManager.add(settingsWindow);
            }
            return true;
        }


        java.lang.String bindName = StringHelper.getBindName(module.getKey());
        java.lang.String description = ModuleDescriptions.getDescription(module);
        float maxWidth = width - 25;
        float currentX = x + 10;
        int lineCount = 1;
        java.lang.String[] words = description.split(" ");
        for (java.lang.String word : words) {
            float wordWidth = Fonts.getSize(12, DEFAULT).getStringWidth(word + " ");
            if (currentX + wordWidth > x + maxWidth) {
                lineCount++;
                currentX = x + 10;
            }
            currentX += wordWidth;
        }
        float descHeight = lineCount == 1 ? lineCount * Fonts.getSize(12, DEFAULT).getStringHeight(" ") - 13 : lineCount * Fonts.getSize(12, DEFAULT).getStringHeight(" ") - 20;
        float stringWidth = module.getKey() < 0 ? 10 : Fonts.getSize(12, DEFAULT).getStringWidth(bindName);
        float bindX = module.settings().isEmpty() ? x + width - 37.5f - stringWidth : x + width - 37.5f - stringWidth;
        float bindY = module.settings().isEmpty() ? y + descHeight + 5.5F + 27 : y + descHeight + 5.5F + 27;

        if (Calculate.isHovered(mouseX, mouseY, bindX, bindY, stringWidth + 6, 9) && button == 0) {
            binding = !binding;
        } else if (binding) {
            module.setKey(button);
            binding = false;
        }

        statusRender.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return Calculate.isHovered(mouseX, mouseY, x, y, width, height);
    }

    @Override
    public void tick() {
        components.forEach(AbstractComponent::tick);
        super.tick();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int key = keyCode == GLFW.GLFW_KEY_DELETE ? -1 : keyCode;
        if (binding) {
            module.setKey(key);
            binding = false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }

    public int getComponentHeight() {
        java.lang.String description = ModuleDescriptions.getDescription(module);
        float maxWidth = width - 25;
        float currentX = x + 10;
        int lineCount = 1;
        java.lang.String[] words = description.split(" ");
        for (java.lang.String word : words) {
            float wordWidth = Fonts.getSize(12, DEFAULT).getStringWidth(word + " ");
            if (currentX + wordWidth > x + maxWidth) {
                lineCount++;
                currentX = x + 10;
            }
            currentX += wordWidth;
        }
        float descHeight = lineCount == 1 ? lineCount * Fonts.getSize(12, DEFAULT).getStringHeight(" ") - 13 : lineCount * Fonts.getSize(12, DEFAULT).getStringHeight(" ") - 20;
        return (int) (module.settings().isEmpty() ? 45 + descHeight : 45 + descHeight);
    }

    private void drawBind(DrawContext context, float descHeight) {
        java.lang.String bindName = StringHelper.getBindName(module.getKey());
        java.lang.String name = binding ? "..." : bindName;
        float stringWidth = module.getKey() < 0 && !binding ? 10 : Fonts.getSize(12, DEFAULT).getStringWidth(name);
        float bindX = module.settings().isEmpty() ? x + width - 37.5f - stringWidth : x + width - 37.5f - stringWidth;
        float back = module.settings().isEmpty() ? y + descHeight + 6F + 23.75f : y + descHeight + 6F + 23.75f;

        rectangle.render(ShapeProperties.create(context.getMatrices(), bindX + 0.25f, back, stringWidth + 6, 10)
                .round(3f)
                .outlineColor(new Color(155, 155, 165, 255).getRGB())
                .color(
                        new Color(61, 67, 71, 80).getRGB(),
                        new Color(71, 77, 81, 80).getRGB(),
                        new Color(81, 87, 91, 80).getRGB(),
                        new Color(91, 97, 101, 80).getRGB())
                .build());

        int bindingColor = ColorHelper.getArgb(255, 135, 136, 148);
        float textX = module.settings().isEmpty() ? x + width - 34.5f - stringWidth : x + width - 34.5f - stringWidth;
        float textY = module.settings().isEmpty() ? y + descHeight + 6F + 28f : y + descHeight + 6F + 28f;

        if (module.getKey() < 0 && !binding) {
            Fonts.getSize(22, GUIICONS).drawString(context.getMatrices(), "G", x + width - 34.5f - 10, y + descHeight + 6F + 26f, new Color(128, 128, 128, 255).getRGB());
        } else {
            Fonts.getSize(12, DEFAULT).drawString(context.getMatrices(), name, textX, textY, bindingColor);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleComponent that = (ModuleComponent) o;
        return module.equals(that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module);
    }
}