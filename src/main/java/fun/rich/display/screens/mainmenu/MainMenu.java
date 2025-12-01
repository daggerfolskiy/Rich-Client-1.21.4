package fun.rich.display.screens.mainmenu;

import antidaunleak.api.UserProfile;
import fun.rich.common.animation.Direction;
import fun.rich.display.screens.mainmenu.altscreen.AltScreen;
import fun.rich.utils.client.text.TextAnimation;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.geometry.Render2D;
import fun.rich.utils.display.gif.GifRender;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.common.animation.implement.Decelerate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.Color;

public class MainMenu extends Screen implements QuickImports {
    public static MainMenu INSTANCE = new MainMenu();
    public int x, y, width, height;
    private final TextAnimation textAnimation = new TextAnimation();
    private boolean altVisible = false;
    private final GifRender gifRender = new GifRender("minecraft:gif/backgrounds/mainmenutype1", 1);
    private final Decelerate altFadeAnimation = new Decelerate();
    private final Decelerate mainFadeAnimation = new Decelerate();
    private AltScreen altScreen;
    private long lastToggleTime = 0;
    private static final long TOGGLE_DELAY = 500;
    private static final UserProfile userProfile = UserProfile.getInstance();

    public MainMenu() {
        super(Text.of("MainMenu"));
        altFadeAnimation.setMs(250).setValue(1.0);
        mainFadeAnimation.setMs(250).setValue(1.0);
        mainFadeAnimation.setDirection(Direction.FORWARDS);
        altFadeAnimation.setDirection(Direction.BACKWARDS);
    }

    @Override
    public void tick() {
        super.tick();
        textAnimation.updateText();
        if (altScreen != null) altScreen.tick();

        if (altFadeAnimation.isFinished(Direction.BACKWARDS)) {
            altVisible = false;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        mc.options.getGuiScale().setValue(2);
        x = window.getScaledWidth();
        y = window.getScaledHeight();
        width = window.getScaledWidth() + 2;
        height = window.getScaledHeight() + 2;
        float cy = height / 2.0f, sy = cy - 25, sx = width / 2 - 50, bs = 21;

        gifRender.render(context.getMatrices(), 0, 0, width, height);
        image.setTexture("textures/mainmenu/backmenu.png").render(ShapeProperties.create(context.getMatrices(), 0, 0, width, height).color(-1).build());

        Double mainAlpha = mainFadeAnimation.getOutput();
        int mainAlphaInt = (int) (255 * mainAlpha);

        if (mainAlpha > 0.01f) {
            drawButton(context, sx, sy, 102, 18.5f, "Single Player", mainAlphaInt);
            drawButton(context, sx, sy + bs, 102, 18.5f, "Multi Player", mainAlphaInt);
            drawButton(context, sx, sy + bs * 2, 102, 18.5f, "Alt Screen", mainAlphaInt);
            drawButton(context, sx, sy + bs * 3, 50, 18.5f, "", mainAlphaInt);
            drawButton(context, sx + 52, sy + bs * 3, 50, 18.5f, "", mainAlphaInt);

            Fonts.getSize(21, Fonts.Type.ICONSTYPENEW).drawCenteredString(context.getMatrices(), "i", width / 2 - 24, sy + bs + 49, applyAlpha(ColorAssist.getText(0.35f), mainAlphaInt));
            Fonts.getSize(22, Fonts.Type.ICONSTYPENEW).drawCenteredString(context.getMatrices(), "s", width / 2 + 27, sy + bs + 49, applyAlpha(ColorAssist.getText(0.35f), mainAlphaInt));

            Fonts.getSize(45, Fonts.Type.ICONS).drawCenteredString(context.getMatrices(), "A", width / 2, sy - 70, applyAlpha(new Color(200, 200, 200).getRGB(), mainAlphaInt));

            Fonts.getSize(18, Fonts.Type.DEFAULT).drawCenteredString(context.getMatrices(), "Rich Client, you made the right choice.", width / 2, sy - 40, applyAlpha(new Color(200, 200, 200).getRGB(), mainAlphaInt));
            Fonts.getSize(12, Fonts.Type.DEFAULT).drawCenteredString(context.getMatrices(), textAnimation.getCurrentText(), width / 2, sy - 25, applyAlpha(new Color(200, 200, 200).getRGB(), mainAlphaInt));
            Fonts.getSize(12, Fonts.Type.DEFAULT).drawCenteredString(context.getMatrices(), "© 2025 RichClient. All rights reserved.", width / 2 + 2, height - 7, applyAlpha(ColorAssist.getText(0.35f), mainAlphaInt));

            rectangle.render(ShapeProperties.create(context.getMatrices(), 8, height - 27, 20, 20).thickness(2).round(10)
                    .outlineColor(applyAlpha(new Color(100, 100, 100, 95).getRGB(), mainAlphaInt))
                    .color(applyAlpha(new Color(50, 50, 50, 55).getRGB(), mainAlphaInt),
                            applyAlpha(new Color(50, 50, 50, 55).getRGB(), mainAlphaInt),
                            applyAlpha(new Color(80, 80, 80, 95).getRGB(), mainAlphaInt),
                            applyAlpha(new Color(80, 80, 80, 95).getRGB(), mainAlphaInt)).build());

            Render2D.drawTexture(context, Identifier.of("minecraft", "textures/mainmenu/steve.png"), 9.5f, height - 25.5f, 17, 7, 32, 32, 32, applyAlpha(new Color(0, 0, 0, 255).getRGB(), mainAlphaInt));

            rectangle.render(ShapeProperties.create(context.getMatrices(), 22, height - 13, 6, 6).thickness(2).round(3)
                    .outlineColor(applyAlpha(new Color(100, 100, 100, 95).getRGB(), mainAlphaInt))
                    .color(applyAlpha(new Color(50, 50, 50, 55).getRGB(), mainAlphaInt),
                            applyAlpha(new Color(50, 50, 50, 55).getRGB(), mainAlphaInt),
                            applyAlpha(new Color(80, 80, 80, 95).getRGB(), mainAlphaInt),
                            applyAlpha(new Color(80, 80, 80, 95).getRGB(), mainAlphaInt)).build());
            rectangle.render(ShapeProperties.create(context.getMatrices(), 23, height - 12, 4, 4).round(2)
                    .color(applyAlpha(new Color(1, 235, 1, 155).getRGB(), mainAlphaInt)).build());

            Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(context.getMatrices(), "Username ▸ " + userProfile.profile("username"), 35, height - 21.5f, applyAlpha(ColorAssist.getText(0.35f), mainAlphaInt));
            Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(context.getMatrices(), "Uid ▸ " + userProfile.profile("uid"), 35, height - 14.5f, applyAlpha(ColorAssist.getText(0.35f), mainAlphaInt));
            String text = "Build ▸ 0.3 alpha";
            float textWidth = Fonts.getSize(12, Fonts.Type.DEFAULT).getStringWidth(text);
            Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(context.getMatrices(), text, context.getScaledWindowWidth() - textWidth - 3, context.getScaledWindowHeight() - 5.5f, applyAlpha(ColorAssist.getText(0.35f), mainAlphaInt));
        }

        Double altAlpha = altFadeAnimation.getOutput();
        if (altVisible || altAlpha > 0.01f) {
            float centerX = width / 2f - 80;
            float centerY = height / 2f - 105;

            if (altScreen == null) {
                altScreen = new AltScreen(centerX, centerY);
            } else {
                altScreen.updatePosition(centerX, centerY);
            }

            int altAlphaInt = (int) (255 * altAlpha);
            Color buttonColor = new Color(50, 50, 50, (int)(55 * altAlpha));
            Color outlineColor = new Color(100, 100, 100, (int)(95 * altAlpha));
            Color gradientColor = new Color(80, 80, 80, (int)(95 * altAlpha));
            Color textColor = new Color(200, 200, 200, altAlphaInt);
            Color bgColor = new Color(30, 30, 30, (int)(255 * altAlpha));

            altScreen.render(context, buttonColor, outlineColor, gradientColor, textColor, bgColor);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawButton(DrawContext ctx, float x, float y, float w, float h, String label, int alpha) {
        rectangle.render(ShapeProperties.create(ctx.getMatrices(), x, y, w, h).thickness(2).round(4)
                .outlineColor(applyAlpha(new Color(100, 100, 100, 95).getRGB(), alpha))
                .color(applyAlpha(new Color(50, 50, 50, 55).getRGB(), alpha),
                        applyAlpha(new Color(50, 50, 50, 55).getRGB(), alpha),
                        applyAlpha(new Color(80, 80, 80, 95).getRGB(), alpha),
                        applyAlpha(new Color(80, 80, 80, 95).getRGB(), alpha)).build());

        rectangle.render(ShapeProperties.create(ctx.getMatrices(), x, y, w, 1).thickness(2).round(5)
                .outlineColor(applyAlpha(new Color(100, 100, 100, 95).getRGB(), alpha))
                .color(applyAlpha(new Color(50, 50, 50, 5).getRGB(), alpha),
                        applyAlpha(new Color(50, 50, 50, 255).getRGB(), alpha),
                        applyAlpha(new Color(80, 80, 80, 255).getRGB(), alpha),
                        applyAlpha(new Color(80, 80, 80, 5).getRGB(), alpha)).build());

        if (!label.isEmpty()) {
            Fonts.getSize(16, Fonts.Type.DEFAULT).drawCenteredString(ctx.getMatrices(), label, width / 2, y + 7, applyAlpha(new Color(200, 200, 200).getRGB(), alpha));
        }
    }

    private int applyAlpha(int color, int alpha) {
        Color c = new Color(color, true);
        int newAlpha = (int) ((c.getAlpha() / 255.0) * alpha);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.min(255, newAlpha)).getRGB();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        float cy = height / 2.0f, sy = cy - 25, sx = width / 2 - 50, bs = 21;

        if (altVisible && altFadeAnimation.getOutput() > 0.5 && altScreen != null) {
            return altScreen.mouseClicked(mx, my, btn);
        }

        if (mainFadeAnimation.getOutput() > 0.5 && btn == 0) {
            if (isIn(mx, my, sx, sy, 102, 18.5f)) { mc.setScreen(new SelectWorldScreen(this)); return true; }
            if (isIn(mx, my, sx, sy + bs, 102, 18.5f)) { mc.setScreen(new MultiplayerScreen(this)); return true; }
            if (isIn(mx, my, sx, sy + bs * 2, 102, 18.5f)) { toggleAlt(); return true; }
            if (isIn(mx, my, sx + 52, sy + bs * 3, 50, 18.5f)) { mc.setScreen(new OptionsScreen(this, mc.options)); return true; }
            if (isIn(mx, my, sx, sy + bs * 3, 50, 18.5f)) { mc.stop(); return true; }
        }

        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (altVisible && altFadeAnimation.getOutput() > 0.5 && altScreen != null) {
            return altScreen.mouseScrolled(mx, my, v);
        }
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (altVisible && altFadeAnimation.getOutput() > 0.5 && altScreen != null) {
            return altScreen.mouseDragged(mx, my, btn);
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (altScreen != null) altScreen.mouseReleased();
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean charTyped(char c, int m) {
        if (altVisible && altFadeAnimation.getOutput() > 0.5 && altScreen != null) {
            return altScreen.charTyped(c);
        }
        return super.charTyped(c, m);
    }

    @Override
    public boolean keyPressed(int k, int s, int m) {
        if (k == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            if (altVisible) {
                toggleAlt();
                return true;
            }
            return false;
        }

        if (altVisible && altFadeAnimation.getOutput() > 0.5 && altScreen != null && altScreen.keyPressed(k)) {
            return true;
        }

        return super.keyPressed(k, s, m);
    }

    private boolean isIn(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void toggleAlt() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToggleTime < TOGGLE_DELAY) {
            return;
        }
        lastToggleTime = currentTime;

        if (!altVisible) {
            altVisible = true;
            altFadeAnimation.setDirection(Direction.FORWARDS);
            altFadeAnimation.reset();
            mainFadeAnimation.setDirection(Direction.BACKWARDS);
            mainFadeAnimation.reset();
            if (altScreen != null) altScreen.reset();
        } else {
            altFadeAnimation.setDirection(Direction.BACKWARDS);
            altFadeAnimation.reset();
            mainFadeAnimation.setDirection(Direction.FORWARDS);
            mainFadeAnimation.reset();
            if (altScreen != null) altScreen.reset();
        }
    }
}