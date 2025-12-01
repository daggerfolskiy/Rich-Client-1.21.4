package fun.rich.features.impl.movement;

import antidaunleak.api.annotation.Native;
import fun.rich.utils.client.chat.ChatMessage;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.interactions.inv.InventoryFlowManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.util.InputUtil;
import fun.rich.features.module.setting.implement.SelectSetting;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.interactions.simulate.Simulations;

import fun.rich.utils.interactions.inv.InventoryTask;
import fun.rich.events.container.CloseScreenEvent;
import fun.rich.events.item.ClickSlotEvent;
import fun.rich.events.packet.PacketEvent;
import fun.rich.events.player.TickEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;

import java.util.*;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryMove extends Module {
    private final List<Packet<?>> packets = new ArrayList<>();
    private final SelectSetting mode = new SelectSetting("Режим", "Выберите режим передвижения в инвентаре")
            .value("Normal", "Legit")
            .selected("Legit");

    enum MovePhase { READY, SLOWING_DOWN, ALLOW_MOVEMENT, SPEEDING_UP, SEND_PACKETS, FINISHED }
    MovePhase movePhase = MovePhase.READY;
    long actionStartTime = 0L;
    boolean playerFullyStopped = false;
    boolean wasForwardPressed, wasBackPressed, wasLeftPressed, wasRightPressed, wasJumpPressed;
    boolean keysOverridden = false;
    boolean inventoryOpened = false;
    boolean packetsHeld = false;

    public InventoryMove() {
        super("InventoryMove", "Inventory Move", ModuleCategory.MOVEMENT);
        setup(mode);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (mode.isSelected("Legit")) {
            switch (e.getPacket()) {
                case ClickSlotC2SPacket slot when (packetsHeld || Simulations.hasPlayerMovement()) && InventoryFlowManager.shouldSkipExecution() -> {
                    packets.add(slot);
                    e.cancel();
                    packetsHeld = true;
                }
                case CloseScreenS2CPacket screen when screen.getSyncId() == 0 -> e.cancel();
                default -> {
                }
            }
        }
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (mode.isSelected("Legit")) {
            processLegitMovement();
        } else {
            if (!InventoryTask.isServerScreen() && InventoryFlowManager.shouldSkipExecution()) {
                InventoryFlowManager.updateMoveKeys();
            }
        }
    }

    private void processLegitMovement() {
        boolean hasOpenScreen = mc.currentScreen != null;

        if (hasOpenScreen && !inventoryOpened && movePhase == MovePhase.READY) {
            startLegitMovement();
            inventoryOpened = true;
        }

        if (!hasOpenScreen && inventoryOpened) {
            if (packetsHeld && movePhase == MovePhase.ALLOW_MOVEMENT) {
                movePhase = MovePhase.SLOWING_DOWN;
                actionStartTime = System.currentTimeMillis();
            } else if (!packetsHeld) {
                resetState();
            }
            inventoryOpened = false;
            return;
        }

        if (movePhase != MovePhase.READY) {
            handleMovementStates();
        }
    }

    private void startLegitMovement() {
        wasForwardPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        wasBackPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        wasLeftPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        wasRightPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
        wasJumpPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

        movePhase = MovePhase.ALLOW_MOVEMENT;
        keysOverridden = false;
        packetsHeld = false;
    }

    private void handleMovementStates() {
        long elapsed = System.currentTimeMillis() - actionStartTime;

        switch (movePhase) {
            case SLOWING_DOWN -> {
                if (mc.player != null && mc.player.input != null) {
                    mc.player.input.movementForward = 0;
                    mc.player.input.movementSideways = 0;
                }

                if (!keysOverridden) {
                    mc.options.forwardKey.setPressed(false);
                    mc.options.backKey.setPressed(false);
                    mc.options.leftKey.setPressed(false);
                    mc.options.rightKey.setPressed(false);
                    mc.options.jumpKey.setPressed(false);
                    keysOverridden = true;
                }

                if (elapsed > 1) {
                    movePhase = MovePhase.SEND_PACKETS;
                    actionStartTime = System.currentTimeMillis();
                }
            }

            case ALLOW_MOVEMENT -> {
                if (!InventoryTask.isServerScreen() && InventoryFlowManager.shouldSkipExecution()) {
                    InventoryFlowManager.updateMoveKeys();
                }
            }

            case SEND_PACKETS -> {
                if (!packets.isEmpty()) {
                    packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
                    packets.clear();
                    InventoryTask.updateSlots();
                }
                packetsHeld = false;
                movePhase = MovePhase.SPEEDING_UP;
                actionStartTime = System.currentTimeMillis();
            }

            case SPEEDING_UP -> {
                long speedupElapsed = System.currentTimeMillis() - actionStartTime;
                float speedupProgress = Math.min(1.0f, speedupElapsed / 1.0f);

                if (keysOverridden) {
                    restoreKeyStates();
                }

                if (mc.player != null && mc.player.input != null) {
                    boolean forward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
                    float targetForward = forward ? 1.0f : 0;
                    mc.player.input.movementForward = lerp(mc.player.input.movementForward, targetForward * speedupProgress, 0.4f);

                    if (speedupProgress > 0.5f && forward && !mc.player.isSprinting()) {
                        mc.player.setSprinting(true);
                    }
                }

                if (speedupElapsed > 1) {
                    movePhase = MovePhase.FINISHED;
                }
            }

            case FINISHED -> {
                resetState();
            }
        }
    }

    private void restoreKeyStates() {
        boolean currentForward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        boolean currentBack = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        boolean currentLeft = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        boolean currentRight = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
        boolean currentJump = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

        mc.options.forwardKey.setPressed(wasForwardPressed && currentForward);
        mc.options.backKey.setPressed(wasBackPressed && currentBack);
        mc.options.leftKey.setPressed(wasLeftPressed && currentLeft);
        mc.options.rightKey.setPressed(wasRightPressed && currentRight);
        mc.options.jumpKey.setPressed(wasJumpPressed && currentJump);
        keysOverridden = false;
    }

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    private void resetState() {
        if (keysOverridden) {
            restoreKeyStates();
        }
        movePhase = MovePhase.READY;
        playerFullyStopped = false;
        inventoryOpened = false;
        packetsHeld = false;
        packets.clear();
    }

    @EventHandler
    public void onClickSlot(ClickSlotEvent e) {

        if (mode.isSelected("Legit")) {
            SlotActionType actionType = e.getActionType();
            if ((packetsHeld || Simulations.hasPlayerMovement()) && ((e.getButton() == 1 && !actionType.equals(SlotActionType.SWAP) && !actionType.equals(SlotActionType.THROW)) || actionType.equals(SlotActionType.PICKUP_ALL))) {
                e.cancel();
            }
        }
    }

    @EventHandler
    public void onCloseScreen(CloseScreenEvent e) {
        if (mode.isSelected("Legit") && packetsHeld && movePhase == MovePhase.ALLOW_MOVEMENT) {
            movePhase = MovePhase.SLOWING_DOWN;
            actionStartTime = System.currentTimeMillis();
        }
    }
}