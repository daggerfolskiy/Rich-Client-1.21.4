package fun.rich.display.hud;

import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.display.geometry.Render2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Inventory extends AbstractDraggable {
    List<ItemStack> stacks = new ArrayList<>();

    public Inventory() {
        super("Inventory", 385, 40, 123, 60, true);
    }

    @Override
    public boolean visible() {
        return !stacks.stream().filter(stack -> !stack.isEmpty()).toList().isEmpty() || PlayerInteractionHelper.isChat(mc.currentScreen);
    }

    @Override
    public void tick() {
        stacks = IntStream.range(9, 36).mapToObj(i -> mc.player.inventory.getStack(i)).toList();
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(14, Fonts.Type.DEFAULT);
        FontRenderer items = Fonts.getSize(12, Fonts.Type.DEFAULT);
        FontRenderer icon = Fonts.getSize(20, Fonts.Type.ICONS);

        long itemCount = stacks.stream().filter(stack -> !stack.isEmpty()).mapToInt(ItemStack::getCount).sum();
        String itemCountText = String.valueOf(itemCount);
        float textWidth = items.getStringWidth(itemCountText);
        float boxWidth = textWidth + 6;

        blur.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), 15.5F)
                .round(4, 0, 4, 0).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), 15.5F)
                .round(4, 0, 4, 0)
                .thickness(0.1f)
                .outlineColor(new Color(33, 33, 33, 255).getRGB())
                .color(
                        new Color(18, 19, 20, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(18, 19, 20, 75).getRGB())
                .build());

        items.drawString(matrix, "Items:", getX() + getWidth() - boxWidth - 21, getY() + 7, ColorAssist.getText());
        items.drawString(matrix, itemCountText, getX() + getWidth() - boxWidth - 2, getY() + 7, new Color(225, 225, 255, 255).getRGB());

        rectangle.render(ShapeProperties.create(matrix, getX() + 18, getY() + 5, 0.5f, 6).color(ColorAssist.getText(0.5F)).round(0F).build());

        blur.render(ShapeProperties.create(matrix, getX(), getY() + 16.40f, getWidth(), getHeight() - 15)
                .round(0, 4, 0, 4).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, getX(), getY() + 16.40f, getWidth(), getHeight() - 15)
                .round(0, 4, 0, 4)
                .thickness(0.1f)
                .outlineColor(new Color(33, 33, 33, 255).getRGB())
                .color(
                        new Color(18, 19, 20, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(18, 19, 20, 75).getRGB())
                .build());

        icon.drawString(matrix, "F", getX() + 4.5f, getY() + 6, new Color(225, 225, 255, 255).getRGB());
        font.drawString(matrix, getName(), getX() + 22, getY() + 6.5f, ColorAssist.getText());

        int offsetY = 20;
        int offsetX = 4;
        int itemsPerRow = 9;
        int itemIndex = 0;

        for (ItemStack stack : stacks) {
            float itemX = getX() + offsetX + 1;
            float itemY = getY() + offsetY + 1f;

            if (itemIndex % itemsPerRow != itemsPerRow - 1) {
                rectangle.render(ShapeProperties.create(matrix, itemX + 10, itemY, 0.5f, 9)
                        .color(ColorAssist.getText(0.1F))
                        .round(0F)
                        .build());
            }

            if (itemIndex < stacks.size() - itemsPerRow) {
                rectangle.render(ShapeProperties.create(matrix, itemX - 0.5f, itemY + 10, 9, 0.5f)
                        .color(ColorAssist.getText(0.1F))
                        .round(0F)
                        .build());
            }

            Render2D.defaultDrawStack(context, stack, itemX - 1f, itemY - 1f, false, true, 0.5F);

            offsetX += 13;
            itemIndex++;

            if (itemIndex % itemsPerRow == 0) {
                offsetY += 13;
                offsetX = 4;
            }
        }
    }
}