package fun.rich.utils.features.autobuy;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.slot.SlotActionType;

public class ClientUpdateHandler {
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void handleUpdate() {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            int syncId = screen.getScreenHandler().syncId;
            mc.interactionManager.clickSlot(syncId, 49, 0, SlotActionType.QUICK_MOVE, mc.player);
        }
    }
}