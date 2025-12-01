package fun.rich.features.impl.render;

import com.mojang.authlib.GameProfile;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BooleanSetting;
import fun.rich.features.module.setting.implement.SelectSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.client.sound.SoundManager;
import fun.rich.events.player.EntityDeathEvent;
import fun.rich.events.render.WorldRenderEvent;
import fun.rich.utils.display.geometry.Render3D;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KillEffect extends Module {
    private final SliderSettings volume = new SliderSettings("Volume", "Volume").setValue(100).range(0, 100);
    private final BooleanSetting playSound = new BooleanSetting("Play Sound", "Play Sound").setValue(true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", "Mobs").setValue(false);
    private final SelectSetting effectType = new SelectSetting("Effect Type", "Effect Type").value("Cross", "Soul").selected("Soul");
    private final Map<Entity, EntityRenderData> renderEntities = new ConcurrentHashMap<>();

    public KillEffect() {
        super("KillEffect", "Kill Effect", ModuleCategory.RENDER);
        setup(volume, playSound, mobs, effectType);
    }

    private static class EntityRenderData {
        private final long timestamp;
        private final float yaw;
        private final Vec3d startPos;
        private final Entity entity;
        private final GameProfile gameProfile;
        private final EntityPose pose;
        private final OtherClientPlayerEntity fakePlayer;

        public EntityRenderData(long timestamp, float yaw, Vec3d startPos, Entity entity, OtherClientPlayerEntity fakePlayer) {
            this.timestamp = timestamp;
            this.yaw = yaw;
            this.startPos = startPos;
            this.entity = entity;
            this.gameProfile = entity instanceof PlayerEntity ? ((PlayerEntity) entity).getGameProfile() : null;
            this.pose = entity.getPose();
            this.fakePlayer = fakePlayer;
        }

        public long getTimestamp() { return timestamp; }
        public float getYaw() { return yaw; }
        public Vec3d getStartPos() { return startPos; }
        public Entity getEntity() { return entity; }
        public GameProfile getGameProfile() { return gameProfile; }
        public EntityPose getPose() { return pose; }
        public OtherClientPlayerEntity getFakePlayer() { return fakePlayer; }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (mc.world == null || mc.player == null) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;
        if (!mobs.isValue() && !(entity instanceof PlayerEntity)) return;
        if (entity == mc.player || renderEntities.containsKey(entity)) return;
        if (playSound.isValue()) {
            mc.world.playSound(mc.player, entity.getBlockPos(), SoundManager.ORTHODOX, SoundCategory.BLOCKS, volume.getValue() / 100f, 1f);
        }
        OtherClientPlayerEntity fakePlayer = null;
        if (effectType.isSelected("Soul") && entity instanceof PlayerEntity) {
            fakePlayer = new OtherClientPlayerEntity(mc.world, ((PlayerEntity) entity).getGameProfile());
            fakePlayer.setPitch(-30.0f);
            fakePlayer.setYaw(entity.getYaw());
            fakePlayer.headYaw = entity.getYaw();
            fakePlayer.bodyYaw = entity.getYaw();
            fakePlayer.setCustomNameVisible(false);
            fakePlayer.setCustomName(Text.literal("Ghost_" + ((PlayerEntity) entity).getGameProfile().getId()));
            mc.world.addEntity(fakePlayer);
        }
        renderEntities.put(entity, new EntityRenderData(System.currentTimeMillis(), entity.getYaw(), entity.getPos(), entity, fakePlayer));
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack stack = e.getStack();
        float tickDelta = e.getPartialTicks();
        List<Entity> entitiesToRemove = new ArrayList<>();
        renderEntities.forEach((entity, data) -> {
            if (System.currentTimeMillis() - data.getTimestamp() > 3000) {
                entitiesToRemove.add(entity);
                if (data.getFakePlayer() != null) {
                    mc.world.removeEntity(data.getFakePlayer().getId(), Entity.RemovalReason.DISCARDED);
                }
            } else {
                float timeProgress = (System.currentTimeMillis() - data.getTimestamp()) / 3000.0f;
                if (effectType.isSelected("Cross")) {
                    int color = new Color(255, 255, 255, (int) (150 * (1 - timeProgress))).getRGB();
                    float yaw = (float) Math.toRadians(data.getYaw() + 95);
                    Vec3d pos = data.getStartPos();
                    Render3D.drawLine(pos.add(0, 0, 0), pos.add(0, 3, 0), color, 5, true);
                    float armLength = 1.0f;
                    float yOffset = 2.3f;
                    Vec3d start = pos.add(-armLength * Math.sin(yaw), yOffset, armLength * Math.cos(yaw));
                    Vec3d end = pos.add(armLength * Math.sin(yaw), yOffset, -armLength * Math.cos(yaw));
                    Render3D.drawLine(start, end, color, 5, true);
                } else if (effectType.isSelected("Soul")) {
                    float yOffset = timeProgress * 3.0f;
                    int alpha = (int) (255 * (1 - timeProgress));
                    Vec3d soulPos = data.getStartPos().add(0, yOffset, 0);
                    Entity renderEntity = data.getEntity();
                    if (data.getFakePlayer() != null) {
                        renderEntity = data.getFakePlayer();
                        renderEntity.setPos(soulPos.x, soulPos.y, soulPos.z);
                    }
                    Render3D.drawEntity(renderEntity, soulPos, data.getYaw(), alpha, stack, tickDelta);
                }
            }
        });
        entitiesToRemove.forEach(renderEntities::remove);
    }
}