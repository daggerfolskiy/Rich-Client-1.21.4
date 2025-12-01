package fun.rich.features.impl.movement;

import fun.rich.events.player.TickEvent;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.math.time.StopWatch;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Box;
import fun.rich.utils.display.color.ColorAssist;
import fun.rich.utils.display.geometry.Render3D;
import fun.rich.events.packet.PacketEvent;
import fun.rich.events.render.WorldRenderEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Blink extends Module {
    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();
    private Box box;
    public static int tickStop = -1;
    private int flushCooldown = 0;
    @NonFinal
    StopWatch timer = new StopWatch();

    public Blink() {
        super("Blink", ModuleCategory.MOVEMENT);
        setup();
    }

    @Override
    public void activate() {
        box = mc.player.getBoundingBox();
    }

    @Override
    public void deactivate() {
        packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
        packets.clear();
    }

    @EventHandler
    public void tick(TickEvent e) {
        if (PlayerInteractionHelper.nullCheck()) return;
        tickStop--;
        if (tickStop >= 0 && !packets.isEmpty()) {
            box = mc.player.getBoundingBox();
            packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
            packets.clear();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (PlayerInteractionHelper.nullCheck()) return;
        switch (e.getPacket()) {
            case PlayerRespawnS2CPacket respawn -> setState(false);
            case GameJoinS2CPacket join -> setState(false);
            case ClientStatusC2SPacket status when status.getMode().equals(ClientStatusC2SPacket.Mode.PERFORM_RESPAWN) ->
                    setState(false);
            default -> {
                if (e.isSend() && tickStop < 0) {
                    packets.add(e.getPacket());
                    e.cancel();
                }
            }
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (box != null) {
            Render3D.drawBox(box, ColorAssist.getClientColor(), 1);
        }
    }
}