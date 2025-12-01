package fun.rich.features.impl.combat;

import antidaunleak.api.annotation.Native;
import fun.rich.events.player.TickEvent;
import fun.rich.features.impl.movement.AutoSprint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BindSetting;
import fun.rich.features.module.setting.implement.SelectSetting;
import fun.rich.utils.interactions.inv.InventoryTask;
import fun.rich.events.keyboard.KeyEvent;

import java.util.Comparator;
import java.util.function.Predicate;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoSwap extends Module {

    final SelectSetting modeSetting = new SelectSetting("Режим", "Способ обхода")
            .value("Default", "Legit")
            .selected("Default");

    final BindSetting bind = new BindSetting("Кнопка использования предмета", "Использует элемент при нажатии");

    final SelectSetting firstItem = new SelectSetting("Основной предмет", "Выберите первый предмет для обмена.")
            .value("Totem of Undying", "Player Head", "Golden Apple", "Shield");

    final SelectSetting secondItem = new SelectSetting("Вторичный предмет", "Выберите второй предмет для обмена.")
            .value("Totem of Undying", "Player Head", "Golden Apple", "Shield");

    enum SwapPhase { READY, SLOWING_DOWN, WAITING_STOP, SWAP, SPEEDING_UP, FINISHED }
    SwapPhase swapPhase = SwapPhase.READY;
    Slot targetSlot = null;
    long actionStartTime = 0L;
    boolean playerFullyStopped = false;
    boolean wasForwardPressed, wasBackPressed, wasLeftPressed, wasRightPressed, wasJumpPressed;
    boolean keysOverridden = false;

    public AutoSwap() {
        super("AutoSwap", "Auto Swap", ModuleCategory.COMBAT);
        setup(modeSetting, firstItem, secondItem, bind);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(bind.getKey()) && swapPhase == SwapPhase.READY) {
            if (modeSetting.getSelected().equals("Default")) {
                executeDefaultSwap();
            } else {
                Slot hotbarSlot = findValidSlot(s -> s.id >= 36 && s.id <= 44);
                    if (hotbarSlot != null) {
                        startLegitSwap(hotbarSlot);
                } else {
                    Slot inventorySlot = findValidSlot(s -> s.id >= 0 && s.id <= 35);
                    if (inventorySlot != null) {
                        startLegitSwap(inventorySlot);
                    }
                }
            }
        }
    }

    @EventHandler

    public void onTick(TickEvent e) {
        if (modeSetting.getSelected().equals("Legit") && swapPhase != SwapPhase.READY) {
            processLegitSwap();
        }
    }

    private void startLegitSwap(Slot slotToSwap) {
        targetSlot = slotToSwap;
        if (targetSlot == null) return;

        wasForwardPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
        wasBackPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode());
        wasLeftPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode());
        wasRightPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode());
        wasJumpPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode());

        swapPhase = SwapPhase.SLOWING_DOWN;
        actionStartTime = System.currentTimeMillis();
        playerFullyStopped = false;
        keysOverridden = false;
    }

    private void processLegitSwap() {
        if (mc.player == null || mc.currentScreen != null) {
            resetState();
            return;
        }

        long elapsed = System.currentTimeMillis() - actionStartTime;

        switch (swapPhase) {
            case SLOWING_DOWN -> {
                mc.player.input.movementForward = 0;
                mc.player.input.movementSideways = 0;
                if (mc.player.isSprinting()) {
                    mc.player.setSprinting(false);
                    AutoSprint.tickStop = 5;
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
                    swapPhase = SwapPhase.WAITING_STOP;
                }
            }
            case WAITING_STOP -> {
                mc.player.input.movementForward = 0;
                mc.player.input.movementSideways = 0;
                double velocityX = Math.abs(mc.player.getVelocity().x);
                double velocityZ = Math.abs(mc.player.getVelocity().z);
                if (velocityX < 0.001 && velocityZ < 0.001 || elapsed > 15) {
                    playerFullyStopped = true;
                    swapPhase = SwapPhase.SWAP;
                }
            }
            case SWAP -> {
                if (playerFullyStopped) {
                    if (targetSlot != null) {
                        InventoryTask.moveItem(targetSlot, 45, false, false);
                    }
                    swapPhase = SwapPhase.SPEEDING_UP;
                    actionStartTime = System.currentTimeMillis();

                    if (keysOverridden) {
                        restoreKeyStates();
                    }
                }
            }
            case SPEEDING_UP -> {
                long speedupElapsed = System.currentTimeMillis() - actionStartTime;
                float speedupProgress = Math.min(1.0f, speedupElapsed / 20.0f);
                if (mc.player.input != null) {
                    boolean forward = InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode());
                    float targetForward = forward ? 1.0f : 0;
                    mc.player.input.movementForward = lerp(mc.player.input.movementForward, targetForward * speedupProgress, 0.4f);
                    if (speedupProgress > 0.4f && forward && !mc.player.isSprinting()) {
                        mc.player.setSprinting(true);
                    }
                }
                if (speedupElapsed > 25) {
                    swapPhase = SwapPhase.FINISHED;
                }
            }
            case FINISHED -> resetState();
        }
    }

    private void executeDefaultSwap() {
        Slot validSlot = findValidSlot(s -> true);
        if (validSlot != null) {
            InventoryTask.swapHand(validSlot, Hand.OFF_HAND, true, true);
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

    private Slot findValidSlot(Predicate<Slot> slotPredicate) {
        Predicate<Slot> combinedPredicate = s -> s.id != 45 && slotPredicate.test(s);

        Item firstType = getItemByType(firstItem.getSelected());
        Item secondType = getItemByType(secondItem.getSelected());
        Item offHandItem = mc.player.getOffHandStack().getItem();
        String offHandItemName = mc.player.getOffHandStack().getName().getString();

        if (offHandItem == firstType) {
            Slot second = InventoryTask.getSlot(secondType,
                    Comparator.comparing(s -> s.getStack().hasEnchantments()),
                    combinedPredicate.and(s -> s.getStack().getItem() == secondType && !s.getStack().getName().getString().equals(offHandItemName))
            );
            if (second != null) return second;
        }

        if (offHandItem == secondType) {
            Slot first = InventoryTask.getSlot(firstType,
                    Comparator.comparing(s -> s.getStack().hasEnchantments()),
                    combinedPredicate.and(s -> s.getStack().getItem() == firstType && !s.getStack().getName().getString().equals(offHandItemName))
            );
            if (first != null) return first;
        }

        if (offHandItem != firstType && offHandItem != secondType) {
            Slot first = InventoryTask.getSlot(firstType,
                    Comparator.comparing(s -> s.getStack().hasEnchantments()),
                    combinedPredicate.and(s -> s.getStack().getItem() == firstType && !s.getStack().getName().getString().equals(offHandItemName))
            );
            if (first != null) return first;

            Slot second = InventoryTask.getSlot(secondType,
                    Comparator.comparing(s -> s.getStack().hasEnchantments()),
                    combinedPredicate.and(s -> s.getStack().getItem() == secondType && !s.getStack().getName().getString().equals(offHandItemName))
            );
            if (second != null) return second;
        }

        return null;
    }


    private void resetState() {
        if (keysOverridden) {
            restoreKeyStates();
        }
        swapPhase = SwapPhase.READY;
        targetSlot = null;
        playerFullyStopped = false;
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Totem of Undying" -> Items.TOTEM_OF_UNDYING;
            case "Player Head" -> Items.PLAYER_HEAD;
            case "Golden Apple" -> Items.GOLDEN_APPLE;
            case "Shield" -> Items.SHIELD;
            default -> Items.AIR;
        };
    }
}