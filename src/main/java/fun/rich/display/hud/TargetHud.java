package fun.rich.display.hud;
import com.nimbusds.jose.crypto.impl.MACProvider;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.math.time.StopWatch;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.features.impl.combat.Aura;
import fun.rich.features.impl.render.Hud;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.Rich;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.interactions.item.ItemTask;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.display.geometry.Render2D;
import fun.rich.utils.display.scissor.ScissorAssist;
import fun.rich.utils.client.packet.network.Network;
import java.awt.*;

public class TargetHud extends AbstractDraggable {
    private final Animation animation = new Decelerate().setMs(650).setValue(1);
    private final Animation faceAlphaAnimation = new Decelerate().setMs(125).setValue(1);
    private final StopWatch stopWatch = new StopWatch();
    private final StopWatch distanceUpdateTimer = new StopWatch();
    private LivingEntity lastTarget;
    private Item lastItem = Items.AIR;
    private float health;
    private float absorption;
    private float displayedDistance;

    public TargetHud() {
        super("Target Hud", 10, 80, 100, 36, true);
    }

    @Override
    public boolean visible() {
        return scaleAnimation.isDirection(Direction.FORWARDS);
    }

    @Override
    public void tick() {
        LivingEntity auraTarget = Aura.getInstance().getTarget();
        if (auraTarget != null) {
            lastTarget = auraTarget;
            startAnimation();
            faceAlphaAnimation.setDirection(Direction.FORWARDS);
        } else if (PlayerInteractionHelper.isChat(mc.currentScreen)) {
            lastTarget = mc.player;
            startAnimation();
            faceAlphaAnimation.setDirection(Direction.FORWARDS);
        } else if (stopWatch.finished(500)) {
            stopAnimation();
            faceAlphaAnimation.setDirection(Direction.BACKWARDS);
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (Hud.getInstance().interfaceSettings.isSelected("Target Hud") && Hud.getInstance().state) {
            if (lastTarget != null) {
                MatrixStack matrix = context.getMatrices();
//                drawUsingItem(context, matrix);
                drawMain(context, matrix);
                drawArmor(context, matrix);
                drawFace(context);
            }
        }
    }

    private void drawMain(DrawContext context, MatrixStack matrix) {

        FontRenderer font = Fonts.getSize(18, Fonts.Type.REGULAR);
        FontRenderer distancefont = Fonts.getSize(12, Fonts.Type.SEMI);
        float hp = PlayerInteractionHelper.getHealth(lastTarget);
        String stringHp = (lastTarget.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime()) ? " ??" : PlayerInteractionHelper.getHealthString(hp);
        health = MathHelper.clamp(Calculate.interpolateSmooth(1, health, hp / lastTarget.getMaxHealth() * 360), 0, 360);
        float absorptionAmount = lastTarget.getAbsorptionAmount();
        absorption = MathHelper.clamp(Calculate.interpolateSmooth(1, absorption, absorptionAmount / 20.0F * 360), 0, 360);
        float actualDistance = mc.player.distanceTo(lastTarget);
        float roundedDistance = Math.round(actualDistance * 2) / 2.0f;
        if (distanceUpdateTimer.finished(10)) {
            displayedDistance = MathHelper.clamp(Calculate.interpolateSmooth(0.5f, displayedDistance, roundedDistance), 0, 100);
            distanceUpdateTimer.reset();
        }
        String distanceText = String.format("%.1f", displayedDistance);

        float nameWidth = font.getStringWidth(lastTarget.getName().getString());
        float baseWidth = Math.max(34 + 36 + 10, 100);
        setWidth((int) baseWidth + 10);
        setHeight((int) 41);

        blur.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), getHeight() - 10)
                .round(6).quality(12)
                .color(new Color(0, 0, 0, 150).getRGB())
                .build());

        rectangle.render(ShapeProperties.create(matrix, getX(), getY(), getWidth(), getHeight() - 10)
                .round(6)
                .thickness(0.1f)
                .outlineColor(new Color(33, 33, 33, 255).getRGB())
                .color(
                        new Color(18, 19, 20, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(0, 2, 5, 75).getRGB(),
                        new Color(18, 19, 20, 75).getRGB())
                .build());

        arc.render(ShapeProperties.create(matrix, getX() + getWidth() - 28.5f, getY() + 2.5f, 26, 26).round(0.26F).thickness(0.30f).end(361)
                .color(new Color(255,255,255,25).getRGB()).build());
        arc.render(ShapeProperties.create(matrix, getX() + getWidth() - 28.5f, getY() + 2.5f, 26, 26).round(0.26F).thickness(0.30f).end(health)
                .color(ColorAssist.fade(0), ColorAssist.fade(200), ColorAssist.fade(0), ColorAssist.fade(200)).build());
        if (absorption > 0 && !Network.isFunTime()) {
            arc.render(ShapeProperties.create(matrix, getX() + getWidth() - 28.5f, getY() + 2.5f, 26, 26).round(0.26F).thickness(0.30f)
                    .end(absorption - 2.5f)
                    .color(new Color(255, 215, 0, 255).getRGB(), new Color(255, 128, 0, 255).getRGB(), new Color(255, 215, 0, 255).getRGB(), new Color(255, 128, 0, 255).getRGB())
                    .build());
        }

        if (nameWidth > 50) {
            ScissorAssist scissorManager = Rich.getInstance().getScissorManager();
            scissorManager.push(matrix.peek().getPositionMatrix(), getX(), getY(), getWidth() - 29, getHeight());
            font.drawGradientString(matrix, lastTarget.getName().getString(), getX() + 29, getY() + 9f, ColorAssist.getText(), ColorAssist.getText(0.15F));
            distancefont.drawString(matrix, "Distance: " + distanceText, getX() + 29, getY() + 19f, new Color(225, 225, 255, 255).getRGB());
            scissorManager.pop();
        } else {
            font.drawString(matrix, lastTarget.getName().getString(), getX() + 29, getY() + 9f, ColorAssist.getText());
            distancefont.drawString(matrix, "Distance: " + distanceText, getX() + 29, getY() + 19f, new Color(225, 225, 255, 255).getRGB());
        }

        float arcCenterX = getX() + getWidth() - 30.5f / 2.0F;
        float arcCenterY = getY() + 30 / 2.0F;
        Fonts.getSize(11, Fonts.Type.BOLD).drawCenteredString(matrix, stringHp, arcCenterX, arcCenterY, new Color(255,255,255,225).getRGB());
    }

    private void drawArmor(DrawContext context, MatrixStack matrix) {
        ItemStack[] slots = new ItemStack[] {
                lastTarget.getMainHandStack(),
                lastTarget.getOffHandStack(),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET)
        };
        float x = getX() + 21f;
        float y = getY() + 17;
        float slotSize = 16 * 0.5F + 2;
        matrix.push();
        matrix.translate(x, y, 0);
        for (int i = 0; i < 6; i++) {
            float currentX = i * 11.5f;

            blur.render(ShapeProperties.create(matrix, currentX - 0.5f, 15F, slotSize + 1, slotSize + 1)
                    .round(3).quality(12)
                    .color(new Color(0, 0, 0, 150).getRGB())
                    .build());

            rectangle.render(ShapeProperties.create(matrix, currentX - 0.5f, 15F, slotSize + 1, slotSize + 1)
                    .round(3)
                    .thickness(0.1f)
                    .outlineColor(new Color(33, 33, 33, 255).getRGB())
                    .color(
                            new Color(18, 19, 20, 75).getRGB(),
                            new Color(0, 2, 5, 75).getRGB(),
                            new Color(0, 2, 5, 75).getRGB(),
                            new Color(18, 19, 20, 75).getRGB())
                    .build());

            if (!slots[i].isEmpty()) {
                Render2D.defaultDrawStack(context, slots[i], currentX, 15.5F, false, false, 0.5F);
            } else {
                String xText = "x";
                FontRenderer font = Fonts.getSize(12, Fonts.Type.DEFAULT);
                float textWidth = font.getStringWidth(xText);
                float textHeight = font.getStringHeight(xText);
                float textX = currentX + (slotSize - textWidth) / 2.0F;
                float textY = 15.5F + (slotSize - textHeight) / 2.0F;
                font.drawString(matrix, xText, textX, textY + 6.25f, new Color(225, 225, 255, 255).getRGB());
            }
        }
        matrix.pop();
    }

    private void drawUsingItem(DrawContext context, MatrixStack matrix) {
        animation.setDirection(lastTarget.isUsingItem() ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!lastTarget.getActiveItem().isEmpty() && lastTarget.getActiveItem().getCount() != 0) {
            lastItem = lastTarget.getActiveItem().getItem();
        }
        if (!animation.isFinished(Direction.BACKWARDS) && !lastItem.equals(Items.AIR)) {
            int size = 24;
            float anim = animation.getOutput().floatValue();
            float progress = (lastTarget.getItemUseTime() + tickCounter.getTickDelta(false)) / ItemTask.maxUseTick(lastItem) * 360;
            float x = getX() - (size + 5) * anim;
            float y = getY() + 4;
            ScissorAssist scissorManager = Rich.getInstance().getScissorManager();
            scissorManager.push(matrix.peek().getPositionMatrix(), getX() - 50, getY(), 50, getHeight());
            Calculate.setAlpha(anim, () -> {
                blur.render(ShapeProperties.create(matrix, x, y, size, size).quality(5)
                        .round(12).softness(1).thickness(2).outlineColor(ColorAssist.getOutline(0)).color(ColorAssist.getRect(0.7F)).build());
                arc.render(ShapeProperties.create(matrix, x, y, size, size).round(0.38F).thickness(0.30f).end(progress)
                        .color(ColorAssist.fade(0), ColorAssist.fade(200), ColorAssist.fade(0), ColorAssist.fade(200)).build());
                Render2D.defaultDrawStack(context, lastItem.getDefaultStack(), x + 3f, y + 3f, false, false, 1f);
            });
            scissorManager.pop();
        }
    }

    private void drawFace(DrawContext context) {
        EntityRenderer<? super LivingEntity, ?> baseRenderer = mc.getEntityRenderDispatcher().getRenderer(lastTarget);
        if (!(baseRenderer instanceof LivingEntityRenderer<?, ?, ?>)) {
            return;
        }
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?> renderer = (LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>) baseRenderer;
        LivingEntityRenderState state = renderer.getAndUpdateRenderState(lastTarget, tickCounter.getTickDelta(false));
        Identifier textureLocation = renderer.getTexture(state);
        float alpha = faceAlphaAnimation.getOutput().floatValue();
        Calculate.setAlpha(alpha, () -> {
            Render2D.drawTexture(context, textureLocation, getX() + 5, getY() + 5.5F, 20, 4, 8, 8, 64, ColorAssist.getRect(1), ColorAssist.multRed(-1, 1 + lastTarget.hurtTime / 4F));
        });
    }
}