package fun.rich.main.listener.impl;

import fun.rich.utils.interactions.inv.InventoryFlowManager;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.client.managers.api.draggable.AbstractDraggable;
import fun.rich.utils.client.packet.network.Network;
import fun.rich.Rich;
import fun.rich.main.listener.Listener;
import fun.rich.events.item.UsingItemEvent;
import fun.rich.events.packet.PacketEvent;
import fun.rich.events.player.TickEvent;

public class EventListener implements Listener {
    public static boolean serverSprint;
    public static int selectedSlot;

    @EventHandler
    public void onTick(TickEvent e) {
        Network.tick();
        Rich.getInstance().getAttackPerpetrator().tick();
        InventoryFlowManager.tick();
        Rich.getInstance().getDraggableRepository().draggable().forEach(AbstractDraggable::tick);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        switch (e.getPacket()) {
            case ClientCommandC2SPacket command -> serverSprint = switch (command.getMode()) {
                case ClientCommandC2SPacket.Mode.START_SPRINTING -> true;
                case ClientCommandC2SPacket.Mode.STOP_SPRINTING -> false;
                default -> serverSprint;
            };
            case UpdateSelectedSlotC2SPacket slot -> selectedSlot = slot.getSelectedSlot();
            default -> {}
        }
        Network.packet(e);
        Rich.getInstance().getAttackPerpetrator().onPacket(e);
        Rich.getInstance().getDraggableRepository().draggable().forEach(drag -> drag.packet(e));
    }

    @EventHandler
    public void onUsingItemEvent(UsingItemEvent e) {
        Rich.getInstance().getAttackPerpetrator().onUsingItem(e);
    }
}
