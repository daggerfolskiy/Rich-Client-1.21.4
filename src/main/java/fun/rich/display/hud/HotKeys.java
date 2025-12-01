package fun.rich.display.hud;

import fun.rich.common.animation.Direction;
import fun.rich.features.impl.render.Hud;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.features.module.Module;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.client.chat.StringHelper;
import fun.rich.Rich;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.*;

public class HotKeys extends AbstractDraggable {
    private List<Module> keysList = new ArrayList<>();
    private long lastKeyChange = 0;
    private java.lang.String currentRandomKey = "NONE";

    public HotKeys() {
        super("Hot Keys", 300, 40, 80, 23, true);
    }

    @Override
    public boolean visible() {
        return Hud.getInstance().interfaceSettings.isSelected("Hot Keys") && (!keysList.isEmpty() || PlayerInteractionHelper.isChat(mc.currentScreen));
    }

    @Override
    public void tick() {
        keysList = Rich.getInstance().getModuleProvider().getModules().stream()
                .filter(module -> module.getAnimation().getOutput().floatValue() != 0 && module.getKey() != -1)
                .toList();
        if (keysList.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastKeyChange >= 1000) {
                List<java.lang.String> availableKeys = List.of("A", "B", "C", "D", "E");
                currentRandomKey = availableKeys.get(new Random().nextInt(availableKeys.size()));
                lastKeyChange = currentTime;
            }
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(13, Fonts.Type.DEFAULT);
        FontRenderer fontModule = Fonts.getSize(13, Fonts.Type.DEFAULT);
        FontRenderer icon = Fonts.getSize(23, Fonts.Type.ICONS);
        FontRenderer items = Fonts.getSize(12, Fonts.Type.DEFAULT);
        FontRenderer categoryIcon = Fonts.getSize(16, Fonts.Type.ICONSCATEGORY);

        long activeModules = keysList.stream().filter(m -> !m.getAnimation().isFinished(Direction.BACKWARDS)).count();
        java.lang.String moduleCountText = java.lang.String.valueOf(activeModules);
        float textWidth = items.getStringWidth(moduleCountText);
        float boxWidth = textWidth + 6;

        blur.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), 15.5F)
                .round(4,0,4,0).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), 15.5F)
                .round(4,0,4,0)
                .thickness(0.1f)
                .outlineColor(new Color(33, 33, 33, 255).getRGB())
                .color(
                        new Color(18, 19, 20, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(18, 19, 20, 75).getRGB())
                .build());

        items.drawString(matrix, "Active:", getX() + getWidth() - boxWidth - 21, getY() + 7, ColorAssist.getText());
        items.drawString(matrix, moduleCountText, getX() + getWidth() - boxWidth - 2, getY() + 7, new Color(225, 225, 255, 255).getRGB());

        rectangle.render(ShapeProperties.create(matrix, getX() + 18, getY() + 5, 0.5f, 6)
                .color(ColorAssist.getText(0.5F)).round(0F).build());

        blur.render(ShapeProperties.create(matrix, getX(), getY() + 16.5F, getWidth(), getHeight() - 17)
                .round(0,4,0,4).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, getX(), getY() + 16.5F, getWidth(), getHeight() - 17)
                .round(0,4,0,4)
                .thickness(0.1f)
                .outlineColor(new Color(33, 33, 33, 255).getRGB())
                .color(
                        new Color(18, 19, 20, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(18, 19, 20, 75).getRGB())
                .build());

        icon.drawString(matrix, "B", getX() + 4f, getY() + 5f, new Color(225, 225, 255, 255).getRGB());
        font.drawString(matrix, getName(), getX() + 22, getY() + 6.5f, ColorAssist.getText());

        float centerX = getX() + getWidth() / 2F;
        int offset = 23;
        int maxWidth = 80;

        if (keysList.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen)) {
            float centerY = getY() + offset;
            java.lang.String name = "Example Module";
            java.lang.String bind = "[" + currentRandomKey + "]";
            java.lang.String iconChar = "A";
            int textColor = ColorAssist.getText();
            int textAlpha = 255;
            int colorWithAlpha = ColorAssist.rgba((textColor >> 16) & 255, (textColor >> 8) & 255, textColor & 255, textAlpha);
            int color = new Color(225, 225, 255, 255).getRGB();
            float bindWidth = fontModule.getStringWidth(bind);
            float bindBoxWidth = bindWidth + 6;
            Calculate.scale(matrix, centerX, centerY, 1, 1, () -> {
                categoryIcon.drawString(matrix, iconChar, getX() + 4.5f, centerY + 1.5f, color);
                rectangle.render(ShapeProperties.create(matrix, getX() + 15F, centerY - 1, 0.5F, 7).color(ColorAssist.getOutline(1, 0.5F)).build());
                fontModule.drawString(matrix, name, getX() + 19, centerY + 1, colorWithAlpha);
                fontModule.drawString(matrix, bind, getX() + getWidth() - bindWidth - 8, centerY + 1, color);
            });
            int width = (int) fontModule.getStringWidth(name + bind) + 25;
            maxWidth = Math.max(width, maxWidth);
            offset += 11;
        } else {
            for (Module module : keysList) {
                java.lang.String bind = "[" + StringHelper.getBindName(module.getKey()) + "]";
                float centerY = getY() + offset;
                float animation = module.getAnimation().getOutput().floatValue();
                java.lang.String iconChar;
                switch (module.getCategory()) {
                    case COMBAT -> iconChar = "A";
                    case MOVEMENT -> iconChar = "B";
                    case RENDER -> iconChar = "C";
                    case PLAYER -> iconChar = "D";
                    case MISC -> iconChar = "E";
                    case CONFIGS -> iconChar = "F";
                    default -> iconChar = module.getCategory().getReadableName().substring(0, 1);
                }
                int textColor = ColorAssist.getText();
                int textAlpha = 255;
                int colorWithAlpha = ColorAssist.rgba((textColor >> 16) & 255, (textColor >> 8) & 255, textColor & 255, textAlpha);
                int color = new Color(225, 225, 255, 255).getRGB();
                float bindWidth = fontModule.getStringWidth(bind);
                float bindBoxWidth = bindWidth + 6;
                Calculate.scale(matrix, centerX, centerY, 1, animation, () -> {
                    categoryIcon.drawString(matrix, iconChar, getX() + 4.5f, centerY + 1.5f, color);
                    rectangle.render(ShapeProperties.create(matrix, getX() + 15F, centerY - 1, 0.5F, 7).color(ColorAssist.getOutline(1, 0.5F)).build());
                    fontModule.drawString(matrix, module.getName(), getX() + 19, centerY + 1, colorWithAlpha);
                    fontModule.drawString(matrix, bind, getX() + getWidth() - bindWidth - 8, centerY + 1, color);
                });
                float width = fontModule.getStringWidth(module.getName() + bind) + 25;
                maxWidth = (int) Math.max(width, maxWidth);
                offset += (int) (animation * 11);
            }
        }
        setWidth(maxWidth + 10);
        setHeight(offset);
    }
}