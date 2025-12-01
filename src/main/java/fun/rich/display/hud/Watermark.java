package fun.rich.display.hud;

import antidaunleak.api.UserProfile;
import com.google.common.base.Suppliers;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.utils.display.atlasfont.msdf.MsdfFont;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.systemrender.builders.Builder;
import fun.rich.Rich;
import fun.rich.utils.display.color.ColorAssist;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import java.awt.Color;
import java.util.function.Supplier;

public class Watermark extends AbstractDraggable {
    private int fpsCount = 0;
    private static final Supplier<MsdfFont> ICONS_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("icons").data("icons").build());
    private static final Supplier<MsdfFont> ICONS_FONT_1 = Suppliers.memoize(() -> MsdfFont.builder().atlas("clienticon1").data("clienticon1").build());
    private static final Supplier<MsdfFont> BOLD_FONT = Suppliers.memoize(() -> MsdfFont.builder().atlas("medium").data("medium").build());
    private static final Supplier<MsdfFont> ICONS = Suppliers.memoize(() -> MsdfFont.builder().atlas("medium").data("medium").build());

    public Watermark() {
        super("Watermark", 10, 10, 92, 16, true);
    }

    @Override
    public void tick() {
        fpsCount = mc.getCurrentFps();
    }

    @Override
    public void drawDraggable(DrawContext e) {
        MatrixStack matrix = e.getMatrices();
        Matrix4f matrix4f = matrix.peek().getPositionMatrix();
        String offset = "";
        String name = Rich.getInstance().getClientInfoProvider().clientName() + offset;
        String icon = "A ";
        String point = " • ";
        String username = UserProfile.getInstance().profile("username");
        String fps = String.valueOf(fpsCount);
        String serverIp = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";
        String title = UserProfile.getInstance().profile("role");;

        float iconWidth = ICONS_FONT.get().getWidth(icon, 13);
        float titleWidth = Fonts.getSize(12, Fonts.Type.DEFAULT).getStringWidth(title);
        float fpsWidth = Fonts.getSize(12, Fonts.Type.BOLD).getStringWidth(fps);
        float serverIpWidth = Fonts.getSize(12, Fonts.Type.BOLD).getStringWidth(serverIp);
        float icon2Width = ICONS_FONT_1.get().getWidth("D", 11);
        float pointWidth = Fonts.getSize(12, Fonts.Type.BOLD).getStringWidth(point);
        float totalWidth = iconWidth + titleWidth + fpsWidth + serverIpWidth + icon2Width + (pointWidth * 3) + 22;

        setWidth((int) totalWidth + 14);

        blur.render(ShapeProperties.create(matrix, getX(), getY(), totalWidth + 14, getHeight() + 4)
                .round(5f).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, getX(), getY(), totalWidth + 14, getHeight() + 4)
                .round(5f)
                .thickness(0.1f)
                .outlineColor(new Color(18, 19, 20, 35).getRGB())
                .color(
                        new Color(18, 19, 20, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(18, 19, 20, 75).getRGB())
                .build());

        Builder.text()
                .font(ICONS_FONT.get())
                .text(icon)
                .size(13)
                .color(new Color(225, 225, 255, 255).getRGB())
                .build()
                .render(matrix4f, getX() + 5f, getY() + 1);

        float currentX = getX() + iconWidth + 7;

        rectangle.render(ShapeProperties.create(matrix, currentX, getY() + 6.5f, 0.5F, getHeight() - 8)
                .round(0)
                .color(ColorAssist.getText(0.5F))
                .build());

        currentX += 4;


        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, title, currentX + 2.5f, getY() + 9f, new Color(255, 255, 255, 255).getRGB());

        currentX += titleWidth + 9;

        rectangle.render(ShapeProperties.create(matrix, currentX, getY() + 6.5f, 0.5F, getHeight() - 8)
                .round(0f)
                .color(ColorAssist.getText(0.5F))
                .build());

        currentX += 5;

        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, fps, currentX, getY() + 9f, new Color(255, 255, 255, 255).getRGB());

        currentX += fpsWidth + 2;

        Fonts.getSize(17, Fonts.Type.ICONSTYPENEW).drawString(matrix, "w", currentX, getY() + 9f, new Color(225, 225, 255, 255).getRGB());

        currentX += 10;

        rectangle.render(ShapeProperties.create(matrix, currentX, getY() + 6.5f, 0.5F, getHeight() - 8)
                .round(0)
                .color(ColorAssist.getText(0.5F))
                .build());

        currentX += 5;

        Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(matrix, serverIp, currentX, getY() + 9f, new Color(255, 255, 255, 255).getRGB());

        currentX += serverIpWidth;

        Builder.text()
                .font(ICONS_FONT_1.get())
                .text("D")
                .size(11)
                .color(new Color(225, 225, 255, 255).getRGB())
                .build()
                .render(matrix4f, currentX + 2.5f, getY() + 2f);
    }
}