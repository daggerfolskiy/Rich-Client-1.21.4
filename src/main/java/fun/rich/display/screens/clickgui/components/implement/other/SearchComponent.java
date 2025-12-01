package fun.rich.display.screens.clickgui.components.implement.other;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.display.scissor.ScissorAssist;
import fun.rich.Rich;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.display.screens.clickgui.MenuScreen;
import java.awt.*;

public class SearchComponent extends AbstractComponent {
    public static boolean typing = false;
    private boolean dragging;
    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private long lastClickTime = 0;
    private float xOffset = 0;
    @Getter
    private String text = "";

    public void setText(String text) {
        this.text = text;
        cursorPosition = 0;
        clearSelection();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(12);
        updateXOffset(font, cursorPosition);
        width = 80;
        height = 15;

        blur.render(ShapeProperties.create(matrix, x, y, width, height).round(3).quality(64)
                .color(new Color(0, 0, 0, 200).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(3)
                .softness(2)
                .thickness(0.5f)
                .outlineColor(new Color(18, 19, 20, 225).getRGB())
                .color(
                        new Color(18, 19, 20, 155).getRGB(),
                        new Color(5, 6, 7, 155).getRGB(),
                        new Color(5, 6, 7, 155).getRGB(),
                        new Color(18, 19, 20, 155).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, x + 65.5f, y + 4, 0.5f, height - 8)
                .color(new Color(155, 155, 155, 55).getRGB()).build());

        Fonts.getSize(25, Fonts.Type.ICONS).drawString(context.getMatrices(), "U", x + width - 14, y + 3.5f, typing ? -1 : 0xFF878894);
        String displayText = text.equalsIgnoreCase("") && !typing ? "Search" : text;
        ScissorAssist scissor = Rich.getInstance().getScissorManager();
        scissor.push(matrix.peek().getPositionMatrix(), x + 1, y, width - 3, height);
        if (typing && selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd) {
            int start = Math.max(0, Math.min(getStartOfSelection(), text.length()));
            int end = Math.max(0, Math.min(getEndOfSelection(), text.length()));
            if (start < end) {
                float selectionXStart = x + 4 - xOffset + font.getStringWidth(text.substring(0, start));
                float selectionXEnd = x + 4 - xOffset + font.getStringWidth(text.substring(0, end));
                float selectionWidth = selectionXEnd - selectionXStart;
                rectangle.render(ShapeProperties.create(matrix, selectionXStart, y + (height / 2) - 4, selectionWidth, 8).color(0xFF5585E8).build());
            }
        }
        font.drawString(context.getMatrices(), displayText, x + 4 - xOffset, y + (height / 2) - 1.0F, typing ? -1 : 0xFF878894);
        scissor.pop();
        long currentTime = System.currentTimeMillis();
        boolean focused = typing && (currentTime % 1000 < 500);
        if (focused && (selectionStart == -1 || selectionStart == selectionEnd)) {
            float cursorX = font.getStringWidth(text.substring(0, cursorPosition));
            rectangle.render(ShapeProperties.create(matrix, x + 4 - xOffset + cursorX, y + (height / 2) - 3.5F, 0.5F, 7).color(-1).build());
        }
        if (dragging) {
            double[] transformed = transformMouseCoords(mouseX, mouseY);
            cursorPosition = getCursorIndexAt(transformed[0]);
            if (selectionStart == -1) {
                selectionStart = cursorPosition;
            }
            selectionEnd = cursorPosition;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double[] transformed = transformMouseCoords(mouseX, mouseY);
        boolean isHovered = Calculate.isHovered(transformed[0], transformed[1], x, y, width, height);

        if (isHovered && button == 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 250) {
                selectionStart = 0;
                selectionEnd = text.length();
            } else {
                typing = true;
                dragging = true;
                lastClickTime = currentTime;
                cursorPosition = getCursorIndexAt(transformed[0]);
                selectionStart = cursorPosition;
                selectionEnd = cursorPosition;
            }
        } else if (!isHovered) {
            typing = false;
            clearSelection();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (typing && Fonts.getSize(12).getStringWidth(text) < 55) {
            deleteSelectedText();
            text = text.substring(0, cursorPosition) + chr + text.substring(cursorPosition);
            cursorPosition++;
            clearSelection();
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typing) {
            if (Screen.hasControlDown()) {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_A -> selectAllText();
                    case GLFW.GLFW_KEY_V -> pasteFromClipboard();
                    case GLFW.GLFW_KEY_C -> copyToClipboard();
                }
            } else {
                switch (keyCode) {
                    case GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_ENTER -> handleTextModification(keyCode);
                    case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_RIGHT -> moveCursor(keyCode);
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private double[] transformMouseCoords(double mouseX, double mouseY) {
        MenuScreen menu = MenuScreen.INSTANCE;
        float scale = menu.getScaleAnimation();
        float centerX = menu.x + menu.width / 2f;
        float centerY = menu.y + menu.height / 2f;
        double transformedX = (mouseX - centerX) / scale + centerX;
        double transformedY = (mouseY - centerY) / scale + centerY;
        return new double[]{transformedX, transformedY};
    }

    private void pasteFromClipboard() {
        String clipboardText = GLFW.glfwGetClipboardString(window.getHandle());
        if (clipboardText != null) {
            deleteSelectedText();
            replaceText(cursorPosition, cursorPosition, clipboardText);
        }
    }

    private void copyToClipboard() {
        if (hasSelection()) {
            GLFW.glfwSetClipboardString(window.getHandle(), getSelectedText());
        }
    }

    private void selectAllText() {
        selectionStart = 0;
        selectionEnd = text.length();
        cursorPosition = text.length();
    }

    private void handleTextModification(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (hasSelection()) {
                replaceText(getStartOfSelection(), getEndOfSelection(), "");
            } else if (cursorPosition > 0) {
                replaceText(cursorPosition - 1, cursorPosition, "");
            }
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            typing = false;
            clearSelection();
        }
    }

    private void moveCursor(int keyCode) {
        if (Screen.hasShiftDown()) {
            if (selectionStart == -1) {
                selectionStart = cursorPosition;
            }
        } else {
            clearSelection();
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT && cursorPosition > 0) {
            cursorPosition--;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT && cursorPosition < text.length()) {
            cursorPosition++;
        }

        if (Screen.hasShiftDown()) {
            selectionEnd = cursorPosition;
        }
    }

    private void replaceText(int start, int end, String replacement) {
        if (start < 0) start = 0;
        if (end > text.length()) end = text.length();
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        text = text.substring(0, start) + replacement + text.substring(end);
        cursorPosition = start + replacement.length();
        clearSelection();
    }

    private boolean hasSelection() {
        return selectionStart != -1 && selectionEnd != -1 && selectionStart != selectionEnd;
    }

    private String getSelectedText() {
        return text.substring(getStartOfSelection(), getEndOfSelection());
    }

    private int getStartOfSelection() {
        return Math.min(selectionStart, selectionEnd);
    }

    private int getEndOfSelection() {
        return Math.max(selectionStart, selectionEnd);
    }

    private void clearSelection() {
        selectionStart = -1;
        selectionEnd = -1;
    }

    private int getCursorIndexAt(double mouseX) {
        FontRenderer font = Fonts.getSize(12);
        float relativeX = (float) mouseX - x - 4 + xOffset;
        int position = 0;
        while (position < text.length()) {
            float charWidth = font.getStringWidth(text.substring(position, position + 1));
            float textWidth = font.getStringWidth(text.substring(0, position));
            if (textWidth + charWidth / 2 > relativeX) {
                break;
            }
            position++;
        }
        return Math.max(0, Math.min(position, text.length()));
    }

    private void updateXOffset(FontRenderer font, int cursorPosition) {
        float cursorX = font.getStringWidth(text.substring(0, Math.min(cursorPosition, text.length())));
        float visibleWidth = width - 8;
        if (cursorX < xOffset) {
            xOffset = Math.max(0, cursorX - 10);
        } else if (cursorX - xOffset > visibleWidth) {
            xOffset = cursorX - visibleWidth + 10;
        }
        if (xOffset < 0) xOffset = 0;
    }

    private void deleteSelectedText() {
        if (hasSelection()) {
            replaceText(getStartOfSelection(), getEndOfSelection(), "");
        }
    }
}