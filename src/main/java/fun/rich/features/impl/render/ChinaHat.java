package fun.rich.features.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.ColorSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.events.render.WorldRenderEvent;
import fun.rich.utils.display.color.ColorAssist;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChinaHat extends Module {

    ColorSetting color = new ColorSetting("Color", "Hat color").value(ColorAssist.getColor(0, 0, 0, 255));
    SliderSettings transparency = new SliderSettings("Transparency", "Overall hat transparency").setValue(0.5f).range(0.1f, 1.0f);

    public ChinaHat() {
        super("ChinaHat", ModuleCategory.RENDER);
        setup(color, transparency);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.player == null || mc.options.getPerspective().isFirstPerson()) {
            return;
        }

        MatrixStack stack = event.getStack();
        float partialTicks = event.getPartialTicks();
        Vec3d playerPos = mc.player.getLerpedPos(partialTicks);
        float yOffset = (float) (playerPos.y + getYOffset(mc.player)) + 1.7f;

        stack.push();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        stack.translate(playerPos.x, yOffset, playerPos.z);
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((System.currentTimeMillis() % 36000) / 100f));

        BufferBuilder cone = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        float radiusValue = 0.60f;
        float heightValue = 0.30f;
        int rgb = color.getColor();
        int adjustedColor = ColorAssist.multAlpha(rgb, transparency.getValue());

        cone.vertex(stack.peek().getPositionMatrix(), 0, heightValue, 0).color(ColorAssist.multAlpha(ColorAssist.multBright(rgb, 0.86f), transparency.getValue()));

        float steps = 64;
        double angleStep = 2 * Math.PI / steps;
        for (int i = 0; i <= steps; i++) {
            float x = (float) (Math.cos(i * angleStep) * radiusValue);
            float z = (float) (Math.sin(i * angleStep) * radiusValue);
            cone.vertex(stack.peek().getPositionMatrix(), x, 0, z).color(adjustedColor);
        }

        BufferRenderer.drawWithGlobalProgram(cone.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthFunc(GL11.GL_LESS);
        stack.pop();
    }

    private float getYOffset(Entity entity) {
        float offset = 0.15f;
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).getItem() instanceof ArmorItem) {
                offset -= 0.065f;
            }
        }
        return offset;
    }
}