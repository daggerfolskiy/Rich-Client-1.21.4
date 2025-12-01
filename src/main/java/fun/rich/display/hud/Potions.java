package fun.rich.display.hud;

import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Formatting;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.common.animation.Animation;
import fun.rich.common.animation.Direction;
import fun.rich.common.animation.implement.Decelerate;
import fun.rich.utils.display.font.FontRenderer;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.display.shape.ShapeProperties;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.display.geometry.Render2D;
import fun.rich.events.packet.PacketEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.awt.*;

public class Potions extends AbstractDraggable {
    private final List<Potion> list = new ArrayList<>();
    private static final RegistryEntry<StatusEffect>[] NEGATIVE_EFFECTS = new RegistryEntry[] {
            StatusEffects.POISON, StatusEffects.WITHER, StatusEffects.NAUSEA, StatusEffects.BLINDNESS,
            StatusEffects.HUNGER, StatusEffects.SLOWNESS, StatusEffects.MINING_FATIGUE, StatusEffects.INSTANT_DAMAGE,
            StatusEffects.WEAKNESS, StatusEffects.LEVITATION, StatusEffects.UNLUCK, StatusEffects.BAD_OMEN
    };
    private long lastEffectChange = 0;
    private RegistryEntry<StatusEffect> currentRandomEffect = StatusEffects.SPEED;

    public Potions() {
        super("Potions", 200, 40, 80, 23, true);
    }

    @Override
    public boolean visible() {
        return !list.isEmpty() || PlayerInteractionHelper.isChat(mc.currentScreen);
    }

    @Override
    public void tick() {
        list.removeIf(p -> p.anim.isFinished(Direction.BACKWARDS));
        list.forEach(p -> p.effect.update(mc.player, null));
        if (list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastEffectChange >= 1000) {
                List<RegistryEntry<StatusEffect>> effects = new ArrayList<>();
                for (Identifier id : Registries.STATUS_EFFECT.getIds()) {
                    Registries.STATUS_EFFECT.getEntry(id).ifPresent(effects::add);
                }
                if (!effects.isEmpty()) {
                    currentRandomEffect = effects.get(new Random().nextInt(effects.size()));
                    lastEffectChange = currentTime;
                }
            }
        }
    }

    @Override
    public void packet(PacketEvent e) {
        switch (e.getPacket()) {
            case EntityStatusEffectS2CPacket effect -> {
                if (!PlayerInteractionHelper.nullCheck() && effect.getEntityId() == Objects.requireNonNull(mc.player).getId()) {
                    RegistryEntry<StatusEffect> effectId = effect.getEffectId();
                    list.stream().filter(p -> p.effect.getEffectType().getIdAsString().equals(effectId.getIdAsString())).forEach(s -> s.anim.setDirection(Direction.BACKWARDS));
                    list.add(new Potion(new StatusEffectInstance(effectId, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()), new Decelerate().setMs(150).setValue(1.0F)));
                }
            }
            case RemoveEntityStatusEffectS2CPacket effect -> list.stream().filter(s -> s.effect.getEffectType().getIdAsString().equals(effect.effect().getIdAsString())).forEach(s -> s.anim.setDirection(Direction.BACKWARDS));
            case PlayerRespawnS2CPacket p -> list.clear();
            case GameJoinS2CPacket p -> list.clear();
            default -> {}
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack matrix = context.getMatrices();
        FontRenderer font = Fonts.getSize(13, Fonts.Type.DEFAULT);
        FontRenderer fontPotion = Fonts.getSize(13, Fonts.Type.DEFAULT);
        FontRenderer icon = Fonts.getSize(17, Fonts.Type.ICONS);
        FontRenderer items = Fonts.getSize(12, Fonts.Type.DEFAULT);

        long activeEffects = list.stream().filter(p -> !p.anim.isFinished(Direction.BACKWARDS)).count();
        String effectCountText = String.valueOf(activeEffects);
        float textWidth = items.getStringWidth(effectCountText);
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

        items.drawString(matrix, "Active:", getX() + getWidth() - boxWidth - 22, getY() + 7, ColorAssist.getText());
        items.drawString(matrix, effectCountText, getX() + getWidth() - boxWidth - 3, getY() + 7, new Color(225, 225, 255, 255).getRGB());

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

        icon.drawString(matrix, "C", getX() + 5f, getY() + 6.5f, new Color(225, 225, 255, 255).getRGB());
        font.drawString(matrix, getName(), getX() + 22, getY() + 6.5f, ColorAssist.getText());

        float centerX = getX() + getWidth() / 2.0F;
        int offset = 23;
        int maxWidth = 95;

        if (list.isEmpty() && PlayerInteractionHelper.isChat(mc.currentScreen)) {
            float centerY = getY() + offset;
            String name = "Example effect";
            String duration = "**:**";
            int textColor = ColorAssist.getText();
            int textAlpha = 255;
            int colorWithAlpha = ColorAssist.rgba((textColor >> 16) & 255, (textColor >> 8) & 255, textColor & 255, textAlpha);
            int color = new Color(225, 225, 255, 255).getRGB();
            int colorWithAlphaRectangle = ColorAssist.rgba((textColor >> 16) & 205, (textColor >> 8) & 205, textColor & 205, textAlpha - 125);
            float durationWidth = fontPotion.getStringWidth(duration);
            float durationBoxWidth = durationWidth + 6;
            Calculate.scale(matrix, centerX, centerY, 1, 1, () -> {
                Render2D.drawSprite(matrix, mc.getStatusEffectSpriteManager().getSprite(currentRandomEffect), getX() + 3.5F, (int) centerY - 2, 8, 8, colorWithAlpha);
                rectangle.render(ShapeProperties.create(matrix, getX() + 14, centerY - 1, 0.5F, 7).color(colorWithAlphaRectangle).build());
                fontPotion.drawString(matrix, name, getX() + 18, centerY + 1, colorWithAlpha);
                fontPotion.drawString(matrix, duration, getX() + getWidth() - durationWidth - 8, centerY + 1, color);
            });
            int width = (int) fontPotion.getStringWidth(name + duration) + 30;
            maxWidth = Math.max(width, maxWidth);
            offset += 11;
        } else {
            for (Potion potion : list) {
                StatusEffectInstance effect = potion.effect;
                float animation = potion.anim.getOutput().floatValue();
                float centerY = getY() + offset;
                int amplifier = effect.getAmplifier();
                String name = effect.getEffectType().value().getName().getString();
                String duration = getDuration(effect);
                String lvl = amplifier > 0 ? Formatting.RED + " " + (amplifier + 1) + Formatting.RESET : "";
                boolean isBadEffect = isBadEffect(effect.getEffectType());
                int textColor = isBadEffect ? ColorAssist.rgba(255, 85, 75, 255) : ColorAssist.getText();
                int textAlpha = 255;
                if (effect.getDuration() <= 200 && effect.getDuration() > 0) {
                    double output = 0.5 + 0.5 * Math.cos(2 * Math.PI * (System.currentTimeMillis() % 700) / 700.0);
                    textAlpha = (int) (100 + (155 * output));
                } else if (effect.getDuration() == 0) {
                    textAlpha = 0;
                }
                int colorWithAlpha = isBadEffect ? ColorAssist.rgba(255, 85, 75, textAlpha) : ColorAssist.rgba((textColor >> 16) & 255, (textColor >> 8) & 255, textColor & 255, textAlpha);
                int color = new Color(225, 225, 255, 255).getRGB();
                int colorWithAlphaRectangle = isBadEffect ? ColorAssist.rgba(255, 85, 75, textAlpha - 125) : ColorAssist.rgba((textColor >> 16) & 205, (textColor >> 8) & 205, textColor & 205, textAlpha - 125);
                float durationWidth = fontPotion.getStringWidth(duration);
                float durationBoxWidth = durationWidth + 6;
                Calculate.scale(matrix, centerX, centerY, 1, animation, () -> {
                    Render2D.drawSprite(matrix, mc.getStatusEffectSpriteManager().getSprite(effect.getEffectType()), getX() + 3.5F, (int) centerY - 2, 8, 8, colorWithAlpha);
                    rectangle.render(ShapeProperties.create(matrix, getX() + 14, centerY - 1, 0.5F, 7).color(colorWithAlphaRectangle).build());
                    fontPotion.drawString(matrix, name, getX() + 18, centerY + 1, colorWithAlpha);
                    if (amplifier > 0) {
                        String level = " " + (amplifier + 1);
                        fontPotion.drawString(matrix, level, getX() + 18 + fontPotion.getStringWidth(name), centerY + 1, colorWithAlpha);
                    }
                    fontPotion.drawString(matrix, duration, getX() + getWidth() - durationWidth - 8, centerY + 1, color);
                });
                int width = (int) fontPotion.getStringWidth(name + lvl + duration) + 30;
                maxWidth = Math.max(width, maxWidth);
                offset += (int) (11 * animation);
            }
        }
        setWidth(maxWidth);
        setHeight(offset);
    }

    private String getDuration(StatusEffectInstance pe) {
        int var1 = pe.getDuration();
        int mins = var1 / 1200;
        return pe.isInfinite() || mins > 60 ? "**:**" : mins + ":" + String.format("%02d", (var1 % 1200) / 20);
    }

    private boolean isBadEffect(RegistryEntry<StatusEffect> effect) {
        for (RegistryEntry<StatusEffect> negativeEffect : NEGATIVE_EFFECTS) {
            if (effect == negativeEffect) {
                return true;
            }
        }
        return false;
    }

    private record Potion(StatusEffectInstance effect, Animation anim) {}
}