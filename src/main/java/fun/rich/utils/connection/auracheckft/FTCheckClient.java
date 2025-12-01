package fun.rich.utils.connection.auracheckft;

import fun.rich.utils.client.chat.ChatMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;

public class FTCheckClient extends WebSocketClient {
    private JsonObject config = new JsonObject();
    private long lastWarningTime = 0;
    private static final long WARNING_INTERVAL = 5000;
    private boolean connected = false;

    public FTCheckClient(URI serverUri) {
        super(serverUri);
        this.setConnectionLostTimeout(10);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        connected = true;
        try {
            send("{\"type\":\"getConfig\"}");
        } catch (Exception e) {
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject data = JsonParser.parseString(message).getAsJsonObject();
            if (data.has("type") && data.get("type").getAsString().equals("config")) {
                config = data.getAsJsonObject("data");
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        connected = false;
    }

    @Override
    public void onError(Exception ex) {
        connected = false;
    }

    public boolean isConnected() {
        return connected && isOpen();
    }

    public boolean isFtFixed() {
        if (!isConnected()) return false;
        try {
            return config.has("ftfixed") && config.get("ftfixed").getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    public void checkAndWarnFunTime() {
        if (!isConnected()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWarningTime < WARNING_INTERVAL) return;

        try {
            if (isFtFixed()) {
                Text prefix = ChatMessage.brandmessage();
                Text message = Text.literal(" » Funtime исправил Киллауру. Не играйте с ней, иначе вас забанят! Мы уже усердно делаем новую — ожидайте.");
                mc.player.sendMessage(prefix.copy().append(message), false);
                lastWarningTime = currentTime;
            }
        } catch (Exception e) {
        }
    }
}