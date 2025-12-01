package fun.rich.utils.features.autobuy;

import fun.rich.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import fun.rich.display.screens.clickgui.components.implement.autobuy.manager.AutoBuyManager;
import fun.rich.display.screens.clickgui.components.implement.autobuy.util.AuctionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionHandler {
    private Set<String> notFoundItems = ConcurrentHashMap.newKeySet();
    private Set<String> processedItems = ConcurrentHashMap.newKeySet();
    private Set<String> sentItems = ConcurrentHashMap.newKeySet();
    private Map<String, Long> lastMessageTime = new ConcurrentHashMap<>();
    private int failedCount = 0;

    private AutoBuyManager autoBuyManager;

    public AuctionHandler(AutoBuyManager autoBuyManager) {
        this.autoBuyManager = autoBuyManager;
    }

    public void clear() {
        notFoundItems.clear();
        processedItems.clear();
        sentItems.clear();
        lastMessageTime.clear();
        failedCount = 0;
    }

    public void handleBuyRequest(MinecraftClient mc, int syncId, List<Slot> slots, BuyRequest request, NetworkManager networkManager) {
        Slot targetSlot = findSlotByItemAndPrice(slots, request.itemName, request.price);
        if (targetSlot != null) {
            mc.interactionManager.clickSlot(syncId, targetSlot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
            failedCount = 0;
        } else {
            String itemKey = request.itemName + "|" + request.price;
            if (!notFoundItems.contains(itemKey)) {
                notFoundItems.add(itemKey);
            }
            failedCount++;
        }
    }

    public boolean shouldUpdate() {
        return failedCount > 3;
    }

    public void updateAuction(MinecraftClient mc, int syncId) {
        mc.interactionManager.clickSlot(syncId, 49, 0, SlotActionType.QUICK_MOVE, mc.player);
        notFoundItems.clear();
        failedCount = 0;
    }

    public void handleSuspiciousPrice(MinecraftClient mc, int syncId, List<Slot> slots) {
        Slot confirmSlot = slots.stream()
                .filter(slot -> !slot.getStack().isEmpty())
                .filter(slot -> slot.getStack().getItem() == Items.GREEN_STAINED_GLASS_PANE)
                .findFirst()
                .orElse(null);
        if (confirmSlot != null) {
            mc.interactionManager.clickSlot(syncId, confirmSlot.id, 0, SlotActionType.PICKUP, mc.player);
        }
    }

    public List<Slot> findMatchingSlots(List<Slot> slots, List<AutoBuyableItem> cachedEnabledItems) {
        List<Slot> matching = new ArrayList<>();

        for (int i = 0; i <= 44; i++) {
            Slot slot = slots.get(i);
            if (slot.getStack().isEmpty()) continue;
            ItemStack stack = slot.getStack();

            if (AuctionUtils.isArmorItem(stack) && AuctionUtils.hasThornsEnchantment(stack)) {
                continue;
            }

            int price = AuctionUtils.getPrice(stack);
            if (price <= 0) continue;

            for (AutoBuyableItem item : cachedEnabledItems) {
                int maxPrice = item.getSettings().getBuyBelow();
                if (price > maxPrice) continue;

                if (item.getSettings().isCanHaveQuantity()) {
                    int stackCount = stack.getCount();
                    if (stackCount < item.getSettings().getMinQuantity()) continue;
                }

                if (AuctionUtils.compareItem(stack, item.createItemStack())) {
                    matching.add(slot);
                    break;
                }
            }
        }

        matching.sort(Comparator.comparingInt(slot -> AuctionUtils.getPrice(slot.getStack())));
        return matching;
    }

    public void processBestSlots(List<Slot> bestSlots, NetworkManager networkManager) {
        Map<String, Integer> itemCounts = new HashMap<>();
        for (Slot bestSlot : bestSlots) {
            ItemStack stack = bestSlot.getStack();
            String itemName = stack.getName().getString();
            String cleanName = AuctionUtils.funTimePricePattern.matcher(itemName).replaceAll("").trim();
            int price = AuctionUtils.getPrice(stack);
            String itemKey = cleanName + "|" + price;

            itemCounts.put(cleanName, itemCounts.getOrDefault(cleanName, 0) + 1);

            if (!sentItems.contains(itemKey)) {
                sentItems.add(itemKey);
                networkManager.sendBuy(cleanName, price);
            }
        }

        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            String itemName = entry.getKey();
            Long lastTime = lastMessageTime.get(itemName);
            if (lastTime == null || currentTime - lastTime > 2000) {
                lastMessageTime.put(itemName, currentTime);
            }
        }
    }

    private Slot findSlotByItemAndPrice(List<Slot> slots, String itemName, int expectedPrice) {
        for (int i = 0; i <= 44; i++) {
            Slot slot = slots.get(i);
            if (slot.getStack().isEmpty()) continue;
            ItemStack stack = slot.getStack();

            if (AuctionUtils.isArmorItem(stack) && AuctionUtils.hasThornsEnchantment(stack)) {
                continue;
            }

            String stackName = stack.getName().getString();
            stackName = AuctionUtils.funTimePricePattern.matcher(stackName).replaceAll("").trim();
            int price = AuctionUtils.getPrice(stack);
            if (stackName.equals(itemName) && price == expectedPrice) {
                return slot;
            }
        }
        return null;
    }
}