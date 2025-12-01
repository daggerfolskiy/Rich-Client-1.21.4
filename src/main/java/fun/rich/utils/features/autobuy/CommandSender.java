package fun.rich.utils.features.autobuy;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import java.time.Instant;
import java.util.BitSet;

public class CommandSender {
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void sendCommand(ClientPlayerEntity player, String command) {
        if (player != null && player.networkHandler != null) {
            player.networkHandler.sendPacket(new ChatMessageC2SPacket(command, Instant.now(), 0L, null, new LastSeenMessageList.Acknowledgment(0, new BitSet())));
        }
    }

    public static void handleServerSwitch(String cmd) {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new ChatMessageC2SPacket(cmd, Instant.now(), 0L, null, new LastSeenMessageList.Acknowledgment(0, new BitSet())));
            ServerSwitchHandler.setWaitingForServerLoad(true);
            ServerSwitchHandler.setServerSwitchTime(System.currentTimeMillis());
        }
    }

    public static void openAuction() {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendPacket(new ChatMessageC2SPacket("/ah", Instant.now(), 0L, null, new LastSeenMessageList.Acknowledgment(0, new BitSet())));
        }
    }
}