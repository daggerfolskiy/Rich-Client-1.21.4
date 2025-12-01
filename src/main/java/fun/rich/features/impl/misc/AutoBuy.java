package fun.rich.features.impl.misc;

import fun.rich.features.impl.misc.AutoBuy;
import fun.rich.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import fun.rich.display.screens.clickgui.components.implement.autobuy.util.AuctionUtils;
import fun.rich.display.screens.clickgui.components.implement.autobuy.autobuyui.PurchaseHistoryWindow;
import fun.rich.events.chat.ChatEvent;
import fun.rich.events.player.TickEvent;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BooleanSetting;
import fun.rich.features.module.setting.implement.SelectSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.math.time.TimerUtil;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import fun.rich.display.screens.clickgui.components.implement.autobuy.manager.AutoBuyManager;
import fun.rich.utils.client.chat.ChatMessage;
import fun.rich.events.packet.PacketEvent;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import fun.rich.utils.features.autobuy.*;

public class AutoBuy extends Module {
    private SelectSetting leaveType = new SelectSetting("Тип обхода", "Проверяющий").value("Проверяющий", "Покупающий");
    private SliderSettings timer2 = new SliderSettings("Таймер обновления аукциона", "").setValue(350).range(350, 750);
    private BooleanSetting bypassDelay = new BooleanSetting("Обход задержки 1.16.5 анках", "");
    private BooleanSetting bypassDelay1214 = new BooleanSetting("Обход задержки 1.21.4 анках", "");
    private BooleanSetting autoStorage = new BooleanSetting("Автоскладирование", "");

    private AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
    private NetworkManager networkManager;
    private ServerManager serverManager;
    private StorageManager storageManager;
    private AuctionHandler auctionHandler;
    private AfkHandler afkHandler;

    private TimerUtil openTimer = TimerUtil.create();
    private TimerUtil updateTimer = TimerUtil.create();
    private TimerUtil buyTimer = TimerUtil.create();
    private TimerUtil switchTimer = TimerUtil.create();
    private TimerUtil enterDelayTimer = TimerUtil.create();
    private TimerUtil ahSpamTimer = TimerUtil.create();
    private TimerUtil connectionCheckTimer = TimerUtil.create();
    private TimerUtil auctionRequestTimer = TimerUtil.create();

    private boolean open = false;
    private boolean serverInAuction = false;
    private boolean justEntered = false;
    private boolean spammingAh = false;
    private boolean waitingForAuctionOpen = false;
    private List<AutoBuyableItem> cachedEnabledItems = new ArrayList<>();

    public AutoBuy() {
        super("Auto Buy", "Auto Buy", ModuleCategory.MISC);

        timer2.visible(() -> leaveType.isSelected("Покупающий"));
        bypassDelay.visible(() -> leaveType.isSelected("Покупающий"));
        bypassDelay1214.visible(() -> leaveType.isSelected("Покупающий"));
        autoStorage.visible(() -> leaveType.isSelected("Покупающий"));

        setup(leaveType, timer2, bypassDelay, bypassDelay1214, autoStorage);

        networkManager = new NetworkManager();
        serverManager = new ServerManager(bypassDelay, bypassDelay1214);
        storageManager = new StorageManager(autoStorage);
        auctionHandler = new AuctionHandler(autoBuyManager);
        afkHandler = new AfkHandler();
    }

    @Override
    public void activate() {
        super.activate();
        resetTimers();
        resetState();

        if (leaveType.isSelected("Покупающий") && (bypassDelay.isValue() || bypassDelay1214.isValue())) {
            mc.options.pauseOnLostFocus = false;
        }

        cacheEnabledItems();
        networkManager.start(leaveType.getSelected());
    }

    @Override
    public void deactivate() {
        super.deactivate();
        networkManager.stop();
        serverManager.reset();
        storageManager.reset();
        afkHandler.resetMovementKeys(mc.options);
    }

    private void resetTimers() {
        openTimer.resetCounter();
        updateTimer.resetCounter();
        buyTimer.resetCounter();
        switchTimer.resetCounter();
        enterDelayTimer.resetCounter();
        ahSpamTimer.resetCounter();
        connectionCheckTimer.resetCounter();
        auctionRequestTimer.resetCounter();
        serverManager.resetTimers();
        storageManager.resetTimers();
        afkHandler.resetTimers();
    }

    private void resetState() {
        open = false;
        serverInAuction = false;
        justEntered = false;
        spammingAh = false;
        waitingForAuctionOpen = false;
        cachedEnabledItems.clear();
        networkManager.clearQueues();
        auctionHandler.clear();
    }

    private void cacheEnabledItems() {
        cachedEnabledItems.clear();
        for (AutoBuyableItem item : autoBuyManager.getAllItems()) {
            if (item.isEnabled()) {
                cachedEnabledItems.add(item);
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof GameMessageS2CPacket gameMessage) {
            Text content = gameMessage.content();
            String message = content.getString();

            if (message.contains("Вы уже подключены к этому серверу!")) {
                serverManager.switchToNextServer(mc.player, networkManager, leaveType.isSelected("Покупающий"));
                return;
            }

            if (leaveType.isSelected("Покупающий")) {
                PurchaseHandler.handlePurchaseMessage(message, autoBuyManager);
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;
        if (!autoBuyManager.isEnabled()) return;

        handleConnectionStatus();
        afkHandler.handle(mc);
        storageManager.handle(mc, open);

        if (storageManager.isActive()) {
            return;
        }

        if (storageManager.handlePostStorage(mc, enterDelayTimer, ahSpamTimer)) {
            justEntered = true;
        }

        boolean wasInHub = serverManager.isInHub();
        serverManager.updateHubStatus(mc.world);

        if (serverManager.shouldJoinAnarchy(bypassDelay.isValue(), bypassDelay1214.isValue())) {
            serverManager.joinAnarchyFromHub(mc.player);
        }

        if (wasInHub && !serverManager.isInHub()) {
            handleServerSwitch();
        }

        if (serverManager.isWaitingForServerLoad() || ServerSwitchHandler.isWaitingForServerLoad()) {
            if (ServerSwitchHandler.hasTimedOut() || (!wasInHub && !serverManager.isInHub())) {
                serverManager.setWaitingForServerLoad(false);
                ServerSwitchHandler.setWaitingForServerLoad(false);
                handleServerSwitch();
            }
        }

        handleAhSpam();
        handleAuction();
        handleServerAutoSwitch();
        handleCheckerAuctionRequest();
    }

    private void handleConnectionStatus() {
        if (leaveType.isSelected("Проверяющий")) {
            if (connectionCheckTimer.hasTimeElapsed(5000)) {
                if (!networkManager.isConnectedToServer()) {
                    networkManager.start(leaveType.getSelected());
                }
                connectionCheckTimer.resetCounter();
            }
        }
    }

    private void handleServerSwitch() {
        justEntered = true;
        enterDelayTimer.resetCounter();
        switchTimer.resetCounter();
        storageManager.resetMaxShulkers();
        waitingForAuctionOpen = false;
        auctionRequestTimer.resetCounter();
    }

    private void handleAhSpam() {
        if ((bypassDelay.isValue() || bypassDelay1214.isValue())) {
            if (justEntered && enterDelayTimer.hasTimeElapsed(2000)) {
                if (!spammingAh) {
                    spammingAh = true;
                    ahSpamTimer.resetCounter();
                }
            }
            if (spammingAh && !afkHandler.isPerformingAction()) {
                if (ahSpamTimer.hasTimeElapsed(1250)) {
                    if (mc.player.networkHandler != null) {
                        CommandSender.sendCommand(mc.player, "/ah");
                    }
                    ahSpamTimer.resetCounter();
                }
            }
        }
    }

    private void handleCheckerAuctionRequest() {
        if (leaveType.isSelected("Проверяющий")) {
            if (!open && !waitingForAuctionOpen) {
                if (auctionRequestTimer.hasTimeElapsed(3000)) {
                    if (networkManager.isConnectedToServer()) {
                        CommandSender.openAuction();
                        waitingForAuctionOpen = true;
                        auctionRequestTimer.resetCounter();
                    }
                }
            }
            if (waitingForAuctionOpen && auctionRequestTimer.hasTimeElapsed(5000)) {
                waitingForAuctionOpen = false;
                auctionRequestTimer.resetCounter();
            }
        }
    }

    private void handleAuction() {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString();
            int syncId = screen.getScreenHandler().syncId;
            List<Slot> slots = screen.getScreenHandler().slots;

            if (title.contains("Аукцион") || title.contains("Аукционы") || title.contains("Поиск")) {
                if (!open) {
                    enterAuction();
                    return;
                }

                storageManager.handleAuctionEnter();

                if (leaveType.isSelected("Покупающий")) {
                    handleBuyerMode(screen, syncId, slots);
                } else if (leaveType.isSelected("Проверяющий")) {
                    handleCheckerMode(slots);
                }
            } else if (title.contains("Подозрительная цена")) {
                auctionHandler.handleSuspiciousPrice(mc, syncId, slots);
                openTimer.resetCounter();
                buyTimer.resetCounter();
            } else {
                exitAuction();
            }
        } else {
            exitAuction();
        }
    }

    private void enterAuction() {
        open = true;
        openTimer.resetCounter();
        updateTimer.resetCounter();
        buyTimer.resetCounter();
        storageManager.notifyAuctionEnter();
        serverInAuction = true;
        auctionHandler.clear();
        justEntered = false;
        spammingAh = false;
        waitingForAuctionOpen = false;
        storageManager.clearStorageCompleted();
        if (!storageManager.getPostStorageTimer().hasTimeElapsed(2000)) {
            storageManager.disableStartStorage();
        }
        cacheEnabledItems();
        if (leaveType.isSelected("Проверяющий")) {
            networkManager.notifyAuctionEnter();
        }
        if (leaveType.isSelected("Покупающий")) {
            networkManager.requestAuctionOpen();
        }
    }

    private void exitAuction() {
        if (open) {
            open = false;
            serverInAuction = false;
            auctionHandler.clear();
            if (leaveType.isSelected("Проверяющий")) {
                networkManager.notifyAuctionLeave();
            }
        }
    }

    private void handleBuyerMode(GenericContainerScreen screen, int syncId, List<Slot> slots) {
        long clientCount = networkManager.getClientInAuctionCount();

        if (networkManager.getQueueSize() > 30) {
            auctionHandler.updateAuction(mc, syncId);
            networkManager.sendUpdateToClients();
            updateTimer.resetCounter();
            networkManager.clearQueues();
            return;
        }

        if (!storageManager.hasReachedMaxShulkers()) {
            BuyRequest request = networkManager.pollRequest();
            if (request != null) {
                auctionHandler.handleBuyRequest(mc, syncId, slots, request, networkManager);
            }
        }

        if (auctionHandler.shouldUpdate()) {
            auctionHandler.updateAuction(mc, syncId);
            networkManager.sendUpdateToClients();
            updateTimer.resetCounter();
            networkManager.clearQueues();
        }

        if (updateTimer.hasTimeElapsed((long) timer2.getValue()) && serverInAuction &&
                clientCount > 0 && networkManager.isQueuesEmpty()) {
            auctionHandler.updateAuction(mc, syncId);
            networkManager.sendUpdateToClients();
            updateTimer.resetCounter();
        }
    }

    private void handleCheckerMode(List<Slot> slots) {
        List<Slot> bestSlots = auctionHandler.findMatchingSlots(slots, cachedEnabledItems);
        if (!bestSlots.isEmpty()) {
            auctionHandler.processBestSlots(bestSlots, networkManager);
            buyTimer.resetCounter();
        }
    }

    private void handleServerAutoSwitch() {
        if (leaveType.isSelected("Покупающий") && (bypassDelay.isValue() || bypassDelay1214.isValue())) {
            if (!serverManager.isInHub() && switchTimer.hasTimeElapsed(60000)) {
                serverManager.switchToNextServer(mc.player, networkManager, true);
            }
        }
    }
}