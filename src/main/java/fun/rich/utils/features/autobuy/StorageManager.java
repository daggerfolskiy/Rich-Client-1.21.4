package fun.rich.utils.features.autobuy;

import fun.rich.features.module.setting.implement.BooleanSetting;
import fun.rich.utils.math.time.TimerUtil;
import fun.rich.display.screens.clickgui.components.implement.autobuy.util.AuctionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private static final int MAX_SHULKERS = 3;

    private BooleanSetting autoStorage;

    private TimerUtil storageTimer = TimerUtil.create();
    private TimerUtil storageActionTimer = TimerUtil.create();
    private TimerUtil auctionEnterTimer = TimerUtil.create();
    private TimerUtil postStorageTimer = TimerUtil.create();

    private boolean storageActive = false;
    private int storageStep = 0;
    private int storageAttempts = 0;
    private boolean waitingForAuctionClose = false;
    private boolean searchingShulker = false;
    private boolean buyingShulker = false;
    private int currentShulkerIndex = 0;
    private List<Integer> shulkerSlots = new ArrayList<>();
    private boolean reachedMaxShulkers = false;
    private boolean canStartStorage = false;
    private boolean storageCompleted = false;

    public StorageManager(BooleanSetting autoStorage) {
        this.autoStorage = autoStorage;
    }

    public void resetTimers() {
        storageTimer.resetCounter();
        storageActionTimer.resetCounter();
        auctionEnterTimer.resetCounter();
        postStorageTimer.resetCounter();
    }

    public void reset() {
        storageActive = false;
        storageStep = 0;
        storageAttempts = 0;
        waitingForAuctionClose = false;
        searchingShulker = false;
        buyingShulker = false;
        currentShulkerIndex = 0;
        shulkerSlots.clear();
        reachedMaxShulkers = false;
        canStartStorage = false;
        storageCompleted = false;
    }

    public void handle(MinecraftClient mc, boolean inAuction) {
        if (!autoStorage.isValue()) return;
        if (reachedMaxShulkers) return;
        if (!canStartStorage) return;

        if (!storageActive) {
            int freeSlots = getFreeInventorySlots(mc);
            if (freeSlots <= 9 && hasResourcesInInventory(mc)) {
                startStorage();
            }
            return;
        }

        if (!storageActionTimer.hasTimeElapsed(300)) {
            return;
        }

        processStorageStep(mc);
    }

    private void startStorage() {
        storageActive = true;
        storageStep = 0;
        storageAttempts = 0;
        waitingForAuctionClose = false;
        searchingShulker = false;
        buyingShulker = false;
        currentShulkerIndex = 0;
        shulkerSlots.clear();
        storageCompleted = false;
        storageTimer.resetCounter();
        storageActionTimer.resetCounter();
    }

    private void processStorageStep(MinecraftClient mc) {
        switch (storageStep) {
            case 0:
                handleStep0(mc);
                break;
            case 1:
                handleStep1(mc);
                break;
            case 15:
                handleStep15();
                break;
            case 2:
                handleStep2(mc);
                break;
            case 20:
                handleStep20(mc);
                break;
            case 201:
                handleStep201(mc);
                break;
            case 21:
                handleStep21(mc);
                break;
            case 22:
                handleStep22(mc);
                break;
            case 23:
                handleStep23(mc);
                break;
            case 24:
                handleStep24(mc);
                break;
            case 25:
                handleStep25(mc);
                break;
            case 26:
                handleStep26(mc);
                break;
            case 100:
                handleStep100(mc);
                break;
            case 101:
                handleStep101(mc);
                break;
            case 102:
                handleStep102();
                break;
            case 103:
                handleStep103(mc);
                break;
            case 104:
                handleStep104(mc);
                break;
            case 105:
                handleStep105();
                break;
        }
    }

    private void handleStep0(MinecraftClient mc) {
        if (mc.currentScreen instanceof GenericContainerScreen) {
            mc.player.closeHandledScreen();
            waitingForAuctionClose = true;
            storageAttempts = 0;
            storageTimer.resetCounter();
            storageStep = 1;
        } else {
            storageStep = 2;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep1(MinecraftClient mc) {
        if (!(mc.currentScreen instanceof GenericContainerScreen)) {
            waitingForAuctionClose = false;
            storageTimer.resetCounter();
            storageStep = 15;
        } else if (storageTimer.hasTimeElapsed(5000)) {
            waitingForAuctionClose = false;
            storageTimer.resetCounter();
            storageStep = 15;
        } else {
            storageAttempts++;
            if (storageAttempts > 3) {
                mc.player.closeHandledScreen();
                storageTimer.resetCounter();
            }
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep15() {
        if (storageTimer.hasTimeElapsed(500)) {
            storageStep = 2;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep2(MinecraftClient mc) {
        currentShulkerIndex = 0;
        shulkerSlots.clear();
        for (int i = 0; i < 36; i++) {
            if (isShulkerBox(mc.player.getInventory().getStack(i))) {
                shulkerSlots.add(i);
            }
        }

        if (shulkerSlots.isEmpty()) {
            storageStep = 100;
        } else {
            storageStep = 20;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep20(MinecraftClient mc) {
        if (mc.currentScreen == null) {
            mc.setScreen(new InventoryScreen(mc.player));
            storageTimer.resetCounter();
            storageStep = 21;
        } else if (mc.currentScreen instanceof InventoryScreen) {
            storageTimer.resetCounter();
            storageStep = 21;
        } else {
            mc.player.closeHandledScreen();
            storageTimer.resetCounter();
            storageStep = 201;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep201(MinecraftClient mc) {
        if (storageTimer.hasTimeElapsed(500)) {
            mc.setScreen(new InventoryScreen(mc.player));
            storageTimer.resetCounter();
            storageStep = 21;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep21(MinecraftClient mc) {
        if (!(mc.currentScreen instanceof InventoryScreen)) {
            if (storageTimer.hasTimeElapsed(2000)) {
                if (mc.currentScreen != null) {
                    mc.player.closeHandledScreen();
                }
                storageTimer.resetCounter();
                storageStep = 201;
            }
            storageActionTimer.resetCounter();
            return;
        }

        if (storageTimer.hasTimeElapsed(800)) {
            storageTimer.resetCounter();
            storageStep = 22;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep22(MinecraftClient mc) {
        if (!(mc.currentScreen instanceof InventoryScreen)) {
            storageStep = 20;
            storageActionTimer.resetCounter();
            return;
        }

        if (storageTimer.hasTimeElapsed(300)) {
            if (currentShulkerIndex >= shulkerSlots.size()) {
                if (!hasResourcesInInventory(mc)) {
                    finishStorage(mc);
                } else {
                    storageStep = 100;
                }
            } else {
                int shulkerInventorySlot = shulkerSlots.get(currentShulkerIndex);
                int slotId = getSlotId(shulkerInventorySlot);

                if (slotId == -1) {
                    currentShulkerIndex++;
                    storageTimer.resetCounter();
                    return;
                }

                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 1, SlotActionType.PICKUP, mc.player);
                storageTimer.resetCounter();
                storageStep = 23;
            }
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep23(MinecraftClient mc) {
        if (mc.currentScreen instanceof ShulkerBoxScreen) {
            storageTimer.resetCounter();
            storageStep = 24;
        } else if (storageTimer.hasTimeElapsed(2000)) {
            currentShulkerIndex++;
            storageTimer.resetCounter();
            storageStep = 22;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep24(MinecraftClient mc) {
        if (!(mc.currentScreen instanceof ShulkerBoxScreen)) {
            currentShulkerIndex++;
            storageTimer.resetCounter();
            storageStep = 22;
            storageActionTimer.resetCounter();
            return;
        }

        if (storageTimer.hasTimeElapsed(500)) {
            storageStep = 25;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep25(MinecraftClient mc) {
        if (!(mc.currentScreen instanceof ShulkerBoxScreen shulkerScreen)) {
            currentShulkerIndex++;
            storageTimer.resetCounter();
            storageStep = 22;
            storageActionTimer.resetCounter();
            return;
        }

        List<Slot> shulkerSlotsList = shulkerScreen.getScreenHandler().slots;

        if (isShulkerFull(shulkerSlotsList)) {
            currentShulkerIndex++;
            storageTimer.resetCounter();
            storageStep = 26;
            storageActionTimer.resetCounter();
            return;
        }

        boolean itemMoved = false;
        for (int i = 27; i < shulkerSlotsList.size(); i++) {
            Slot slot = shulkerSlotsList.get(i);
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && !isShulkerBox(stack) && !isBag(stack)) {
                int syncId = shulkerScreen.getScreenHandler().syncId;
                mc.interactionManager.clickSlot(syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                itemMoved = true;
                storageTimer.resetCounter();
                break;
            }
        }

        if (!itemMoved) {
            currentShulkerIndex++;
            storageTimer.resetCounter();
            storageStep = 26;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep26(MinecraftClient mc) {
        if (storageTimer.hasTimeElapsed(300)) {
            if (currentShulkerIndex >= shulkerSlots.size()) {
                if (!hasResourcesInInventory(mc)) {
                    if (mc.currentScreen instanceof ShulkerBoxScreen) {
                        mc.player.closeHandledScreen();
                    }
                    finishStorage(mc);
                } else {
                    if (mc.currentScreen instanceof ShulkerBoxScreen) {
                        mc.player.closeHandledScreen();
                    }
                    storageTimer.resetCounter();
                    storageStep = 100;
                }
            } else {
                int nextShulkerInventorySlot = shulkerSlots.get(currentShulkerIndex);
                int slotId = getSlotId(nextShulkerInventorySlot);

                if (slotId == -1) {
                    currentShulkerIndex++;
                    storageTimer.resetCounter();
                    return;
                }

                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 1, SlotActionType.PICKUP, mc.player);
                storageTimer.resetCounter();
                storageStep = 23;
            }
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep100(MinecraftClient mc) {
        int totalShulkers = countTotalShulkers(mc);
        if (totalShulkers >= MAX_SHULKERS) {
            if (mc.currentScreen != null) {
                mc.player.closeHandledScreen();
            }
            reachedMaxShulkers = true;
            finishStorage(mc);
        } else {
            if (mc.currentScreen != null) {
                mc.player.closeHandledScreen();
            }
            storageTimer.resetCounter();
            storageStep = 101;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep101(MinecraftClient mc) {
        if (storageTimer.hasTimeElapsed(500)) {
            if (!searchingShulker) {
                CommandSender.sendCommand(mc.player, "/ah search Шалкер пустой");
                searchingShulker = true;
                storageTimer.resetCounter();
            }
            if (mc.currentScreen instanceof GenericContainerScreen) {
                storageTimer.resetCounter();
                storageStep = 102;
            } else if (storageTimer.hasTimeElapsed(6000)) {
                searchingShulker = false;
                storageStep = 101;
            }
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep102() {
        if (storageTimer.hasTimeElapsed(3000)) {
            storageStep = 103;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep103(MinecraftClient mc) {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            List<Slot> slots = screen.getScreenHandler().slots;
            Slot cheapestShulker = null;
            int lowestPrice = 100001;
            for (int i = 0; i <= 44; i++) {
                Slot slot = slots.get(i);
                ItemStack stack = slot.getStack();
                if (isShulkerBox(stack)) {
                    int price = AuctionUtils.getPrice(stack);
                    if (price > 0 && price <= 100000 && price < lowestPrice) {
                        cheapestShulker = slot;
                        lowestPrice = price;
                    }
                }
            }
            if (cheapestShulker != null) {
                int syncId = screen.getScreenHandler().syncId;
                mc.interactionManager.clickSlot(syncId, cheapestShulker.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                buyingShulker = true;
                storageTimer.resetCounter();
                storageStep = 104;
            } else {
                int syncId = screen.getScreenHandler().syncId;
                mc.interactionManager.clickSlot(syncId, 49, 0, SlotActionType.QUICK_MOVE, mc.player);
                storageTimer.resetCounter();
            }
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep104(MinecraftClient mc) {
        if (storageTimer.hasTimeElapsed(2500)) {
            if (mc.currentScreen instanceof GenericContainerScreen) {
                mc.player.closeHandledScreen();
            }
            storageTimer.resetCounter();
            storageStep = 105;
        }
        storageActionTimer.resetCounter();
    }

    private void handleStep105() {
        if (storageTimer.hasTimeElapsed(1000)) {
            searchingShulker = false;
            buyingShulker = false;
            storageStep = 2;
        }
        storageActionTimer.resetCounter();
    }

    private void finishStorage(MinecraftClient mc) {
        storageActive = false;
        storageCompleted = true;
        postStorageTimer.resetCounter();
        canStartStorage = false;
        storageStep = 0;
    }

    private int getFreeInventorySlots(MinecraftClient mc) {
        int freeSlots = 0;
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                freeSlots++;
            }
        }
        return freeSlots;
    }

    private boolean isShulkerBox(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof ShulkerBoxBlock;
        }
        return false;
    }

    private boolean isBag(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof BundleItem;
    }

    private int countTotalShulkers(MinecraftClient mc) {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isShulkerBox(stack)) {
                count++;
            }
        }
        return count;
    }

    private boolean isShulkerFull(List<Slot> slots) {
        for (int i = 0; i < 27; i++) {
            if (i < slots.size() && slots.get(i).getStack().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasResourcesInInventory(MinecraftClient mc) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && !isShulkerBox(stack) && !isBag(stack)) {
                return true;
            }
        }
        return false;
    }

    private int getSlotId(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot < 9) {
            return inventorySlot + 36;
        } else if (inventorySlot >= 9 && inventorySlot < 36) {
            return inventorySlot;
        }
        return -1;
    }

    public boolean isActive() {
        return storageActive;
    }

    public void notifyAuctionEnter() {
        auctionEnterTimer.resetCounter();
    }

    public void handleAuctionEnter() {
        if (autoStorage.isValue() && !canStartStorage) {
            if (auctionEnterTimer.hasTimeElapsed(5000)) {
                canStartStorage = true;
            }
        }
    }

    public boolean handlePostStorage(MinecraftClient mc, TimerUtil enterDelayTimer, TimerUtil ahSpamTimer) {
        if (storageCompleted && postStorageTimer.hasTimeElapsed(1500)) {
            storageCompleted = false;
            canStartStorage = false;
            enterDelayTimer.resetCounter();
            ahSpamTimer.resetCounter();
            if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                CommandSender.sendCommand(mc.player, "/ah");
            }
            return true;
        }
        return false;
    }

    public TimerUtil getPostStorageTimer() {
        return postStorageTimer;
    }

    public void clearStorageCompleted() {
        storageCompleted = false;
    }

    public void disableStartStorage() {
        canStartStorage = false;
    }

    public boolean hasReachedMaxShulkers() {
        return reachedMaxShulkers;
    }

    public void resetMaxShulkers() {
        reachedMaxShulkers = false;
        canStartStorage = false;
    }
}