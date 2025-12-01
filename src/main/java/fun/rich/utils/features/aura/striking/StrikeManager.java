package fun.rich.utils.features.aura.striking;

import fun.rich.features.impl.movement.Blink;
import fun.rich.features.impl.movement.ElytraTarget;
import fun.rich.utils.client.Instance;
import fun.rich.utils.client.chat.ChatMessage;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.utils.client.managers.event.types.EventType;
import fun.rich.features.impl.combat.Aura;
import fun.rich.features.impl.combat.TriggerBot;
import fun.rich.utils.features.aura.warp.Turns;
import fun.rich.utils.features.aura.utils.MathAngle;
import fun.rich.utils.features.aura.utils.RaycastAngle;
import fun.rich.utils.features.aura.warp.TurnsConnection;
import fun.rich.utils.features.aura.utils.Pressing;
import fun.rich.features.impl.movement.AutoSprint;
import fun.rich.events.item.UsingItemEvent;
import fun.rich.events.packet.PacketEvent;
import fun.rich.main.listener.impl.EventListener;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.interactions.inv.InventoryFlowManager;
import fun.rich.utils.interactions.inv.InventoryTask;
import fun.rich.utils.interactions.simulate.PlayerSimulation;
import fun.rich.utils.math.calc.Calculate;
import fun.rich.utils.math.time.StopWatch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StrikeManager implements QuickImports {
    private final StopWatch attackTimer = new StopWatch(), shieldWatch = new StopWatch(), sprintCooldown = new StopWatch();;
    private final Pressing clickScheduler = new Pressing();
    private int count = 0;
    private boolean prevSprinting;

    void tick() {}

    void onPacket(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        if (packet instanceof HandSwingC2SPacket || packet instanceof UpdateSelectedSlotC2SPacket) {
            clickScheduler.recalculate();
        }
    }

    void onUsingItem(UsingItemEvent e) {
        if (e.getType() == EventType.START && !shieldWatch.finished(50)) {
            e.cancel();
        }
    }
    private ClientCommandC2SPacket.Mode lastSprintCommand = null;
    private boolean pendingStartSprint = false;
    private boolean pendingStopSprint = false;
    private boolean didStopSprint = false;
    private static final long SPRINT_COOLDOWN_MS = 200;
    void handleAttack(StrikerConstructor.AttackPerpetratorConfigurable config) {
        if (canAttack(config, 0)) preAttackEntity(config);

        boolean elytraMode = Aura.getInstance().getTarget() != null &&
                Aura.getInstance().getTarget().isGliding() &&
                mc.player.isGliding();

        if (elytraMode) {
            Vec3d targetVelocity = config.getTarget().getVelocity();
            double targetSpeed = targetVelocity.horizontalLength();
            float leadTicks = 0;
            if (ElytraTarget.shouldElytraTarget) {
                leadTicks = ElytraTarget.getInstance().elytraForward.getValue();
            }

            Vec3d predictedPos = config.getTarget().getPos().add(targetVelocity.multiply(leadTicks));
            Box predictedBox = new Box(
                    predictedPos.x - config.getTarget().getWidth() / 2,
                    predictedPos.y,
                    predictedPos.z - config.getTarget().getWidth() / 2,
                    predictedPos.x + config.getTarget().getWidth() / 2,
                    predictedPos.y + config.getTarget().getHeight(),
                    predictedPos.z + config.getTarget().getWidth() / 2
            );

            Vec3d eyePos = mc.player.getEyePos();
            Vec3d lookVec = TurnsConnection.INSTANCE.getRotation().toVector();
            if (!predictedBox.raycast(eyePos, eyePos.add(lookVec.multiply(config.getMaximumRange()))).isPresent()) {
                return;
            }

            if (!RaycastAngle.rayTrace(config) || !canAttack(config, 0)) return;
        } else {
            if (!RaycastAngle.rayTrace(config) || !canAttack(config, 0)) return;
        }

        String sprintMode = getSprintMode();
        if (sprintMode.equals("Legit") && !isSprinting()) {
            attackEntity(config);
        }

        if (sprintMode.equals("Packet")) {
            mc.player.setSprinting(false);
            mc.player.sendSprintingPacket();
            attackEntity(config);
        }
    }

    private String getSprintMode() {
        if (Aura.getInstance().isState()) {
            return Aura.getInstance().getSprintReset().getSelected();
        } else if (TriggerBot.getInstance().isState()) {
            return TriggerBot.getInstance().sprintReset.getSelected();
        }
        return "Legit";
    }

    void preAttackEntity(StrikerConstructor.AttackPerpetratorConfigurable config) {
        if (config.isShouldUnPressShield() && mc.player.isUsingItem() && mc.player.getActiveItem().getItem().equals(Items.SHIELD)) {
            mc.interactionManager.stopUsingItem(mc.player);
            shieldWatch.reset();
        }
        String sprintMode = getSprintMode();
        if (sprintMode.equals("Legit")) {
            if (mc.player.isSprinting() && getTargetDistance() <= getAttackRange()) {
                AutoSprint.tickStop = 2;
                mc.options.sprintKey.setPressed(false);
                mc.player.setSprinting(false);
                return;
            }
            return;
        }
    }

    void postAttackEntity(StrikerConstructor.AttackPerpetratorConfigurable config) {
    }

    void attackEntity(StrikerConstructor.AttackPerpetratorConfigurable config) {
        if (Aura.getInstance().isState() && Aura.getInstance().getAttackSetting().isSelected("Fake Lag")) {
            Aura.getInstance().tickStop = 1;
        }
        attack(config);
        breakShield(config);
        attackTimer.reset();
        count++;
    }

    private void breakShield(StrikerConstructor.AttackPerpetratorConfigurable config) {
        LivingEntity target = config.getTarget();
        Turns angleToPlayer = MathAngle.fromVec3d(mc.player.getBoundingBox().getCenter().subtract(target.getEyePos()));
        boolean targetOnShield = target.isUsingItem() && target.getActiveItem().getItem().equals(Items.SHIELD);
        boolean angle = Math.abs(TurnsConnection.computeAngleDifference(target.getYaw(), angleToPlayer.getYaw())) < 90;
        Slot axe = InventoryTask.getSlot(s -> s.getStack().getItem() instanceof AxeItem);

        if (config.isShouldBreakShield() && targetOnShield && axe != null && angle && InventoryFlowManager.script.isFinished()) {
            InventoryTask.swapHand(axe, Hand.MAIN_HAND, false);
            InventoryTask.closeScreen(true);
            attack(config);
            InventoryTask.swapHand(axe, Hand.MAIN_HAND, false, true);
            InventoryTask.closeScreen(true);
        }
    }

    private void attack(StrikerConstructor.AttackPerpetratorConfigurable config) {
        float chance = Calculate.getRandom(0, 100);
        if (Aura.getInstance().isState() && Aura.getInstance().getAttackSetting().isSelected("Hit Chance")) {
            if (chance < Aura.getInstance().getHitChance().getValue()) {
                mc.interactionManager.attackEntity(mc.player, config.getTarget());
            }
        } else if (TriggerBot.getInstance().isState() && TriggerBot.getInstance().attackSetting.isSelected("Hit Chance")) {
            if (chance < TriggerBot.getInstance().hitChance.getValue()) {
                mc.interactionManager.attackEntity(mc.player, config.getTarget());
            }
        } else {
            mc.interactionManager.attackEntity(mc.player, config.getTarget());
        }
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isSprinting() {
        return EventListener.serverSprint && !mc.player.isGliding() && !mc.player.isTouchingWater();
    }

    private float getAttackRange() {
        if (Aura.getInstance().isState()) {
            return Aura.getInstance().getAttackRange().getValue();
        } else if (TriggerBot.getInstance().isState()) {
            return TriggerBot.getInstance().attackRange.getValue();
        }
        return 3.0f;
    }

    private double getTargetDistance() {
        if (Aura.getInstance().isState() && Aura.getInstance().getTarget() != null) {
            return mc.player.distanceTo(Aura.getInstance().getTarget());
        } else if (TriggerBot.getInstance().isState() && TriggerBot.getInstance().target != null) {
            return mc.player.distanceTo(TriggerBot.getInstance().target);
        }
        return 0;
    }

    public boolean canAttack(StrikerConstructor.AttackPerpetratorConfigurable config, int ticks) {
        for (int i = 0; i <= ticks; i++) {
            if (canCrit(config, i)) {
                return true;
            }
        }
        return false;
    }
    public boolean canCrit(StrikerConstructor.AttackPerpetratorConfigurable config, int ticks) {
        if (mc.player.isUsingItem() && !mc.player.getActiveItem().getItem().equals(Items.SHIELD) && config.isEatAndAttack()) {
            return false;
        }

        if (!clickScheduler.isCooldownComplete(false, 1)) { return false; }

        PlayerSimulation simulated = PlayerSimulation.simulateLocalPlayer(ticks);
        boolean noRestrict = !hasMovementRestrictions(simulated);
        boolean critState = isPlayerInCriticalState(simulated, ticks);
        if (Aura.getInstance().getSmartCrits().isValue() && Aura.getInstance().isState()) {
            if (noRestrict) {
                return critState || simulated.onGround;
            } else {
                return true;
            }
        }
        if (TriggerBot.getInstance().smartCrits.isValue() && TriggerBot.getInstance().isState()) {
            if (noRestrict) {
                return critState || simulated.onGround;
            } else {
                return true;
            }
        }
        if (config.isOnlyCritical() && !hasMovementRestrictions(simulated)) {
            return isPlayerInCriticalState(simulated, ticks);
        }
        return true;
    }

    private boolean hasMovementRestrictions(PlayerSimulation simulated) {
        return simulated.hasStatusEffect(StatusEffects.BLINDNESS)
                || simulated.hasStatusEffect(StatusEffects.LEVITATION)
                || PlayerInteractionHelper.isBoxInBlock(simulated.boundingBox.expand(-1e-3), Blocks.COBWEB)
                || simulated.isSubmergedInWater()
                || simulated.isInLava()
                || simulated.isClimbing()
                || !PlayerInteractionHelper.canChangeIntoPose(EntityPose.STANDING, simulated.pos)
                || simulated.player.getAbilities().flying;
    }

    private boolean isPlayerInCriticalState(PlayerSimulation simulated, int ticks) {
        boolean fall = simulated.fallDistance > 0;
        return !simulated.onGround && (fall);
    }
}