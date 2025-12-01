package fun.rich.display.screens.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.display.screens.clickgui.components.AbstractComponent;
import fun.rich.utils.math.calc.Calculate;

import java.awt.*;

import static fun.rich.common.animation.Direction.BACKWARDS;
import static fun.rich.common.animation.Direction.FORWARDS;

@Setter
@Accessors(chain = true)
public class CheckComponent extends AbstractComponent {
    private boolean state;
    private Runnable runnable;
    private final Animation alphaAnimation = new Decelerate().setMs(400).setValue(100);
    private final Animation stencilAnimation = new Decelerate().setMs(200).setValue(8);
    private final Animation sliderAnimation = new Decelerate().setMs(225).setValue(8);

    @Override
    public CheckComponent position(float x, float y) {
        this.x = x - 7;
        this.y = y + 2;
        return this;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();
        alphaAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        stencilAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        sliderAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        int stateColor = new Color(128, 128, 128, 255).getRGB();
        int opacity = alphaAnimation.getOutput().intValue();
        float sliderX = x + sliderAnimation.getOutput().floatValue();

        rectangle.render(ShapeProperties.create(matrix, x, y, 16, 8)
                .round(4).thickness(0).softness(0)
                .outlineColor(new Color(128, 128, 128, 255).getRGB())
                .color(new Color(128, 128, 128, 40).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, x, y, 16, 8)
                .round(4).thickness(0).softness(0)
                .outlineColor(new Color(128, 128, 128, 255).getRGB())
                .color(Calculate.applyOpacity(stateColor, opacity))
                .build());

        rectangle.render(ShapeProperties.create(matrix, sliderX - 0.5f, y - 0.5f, 9, 9)
                .round(4.5f).thickness(2).softness(1)
                .outlineColor(new Color(155, 155, 165, 255).getRGB())
                .color(
                        new Color(61, 67, 71, 255).getRGB(),
                        new Color(71, 77, 81, 255).getRGB(),
                        new Color(81, 87, 91, 255).getRGB(),
                        new Color(91, 97, 101, 255).getRGB())
                .build());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (Calculate.isHovered(mouseX, mouseY, x, y, 16, 8) && button == 0) {
            runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
