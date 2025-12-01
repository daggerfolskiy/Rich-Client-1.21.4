package fun.rich.utils.connection.irc;

import antidaunleak.api.UserProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fun.rich.utils.client.text.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import fun.rich.utils.client.chat.ChatMessage;
import fun.rich.commands.defaults.IRCCommand;
import fun.rich.Rich;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class IRCWebSocketClient extends WebSocketClient {
    private boolean isMuted = false;
    private final String clientId = UserProfile.getInstance().profile("uid");
    private final Set<String> processedMessages = new HashSet<>();

    public boolean isMuted() {
        return isMuted;
    }

    public IRCWebSocketClient(URI serverUri) {
        super(serverUri);
        this.addHeader("Sec-WebSocket-Key", clientId);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        sendGetPrefix();
        Rich.getInstance().getIrcManager().isConnecting.set(false);
    }

    @Override
    public void onMessage(String message) {
        String decodedMessage = cypher(message);
        if (processedMessages.add(decodedMessage)) {
            handleMessage(decodedMessage);
        }
    }

    private void handleMessage(String jsonMessage) {
        try {
            JsonObject json = JsonParser.parseString(jsonMessage).getAsJsonObject();
            if (!json.has("type")) return;
            String type = json.get("type").getAsString();
            if (type.equals("text")) {
                if (!json.has("message") || !json.has("author") || !json.has("prefix")) return;
                String author = json.get("author").getAsString();
                String content = json.get("message").getAsString();
                String prefix = json.get("prefix").getAsString();
                String formattedMessage = author + " -> " + content;
                if (Rich.getInstance().isShowIrcMessages()) {
                    displayMessage(prefix, formattedMessage, author, content);
                }
            } else if (type.equals("mute") || type.equals("mute_attempt")) {
                isMuted = true;
                if (Rich.getInstance().isShowIrcMessages() && json.has("reason") && json.has("duration_minutes")) {
                    String reason = json.get("reason").getAsString();
                    int duration = json.get("duration_minutes").getAsInt();
                    String reasonTranslated = reason.equals("Спам") ? "спам" : reason.equals("Мат") ? "мат" : reason.equals("Капс") ? "злоупотребление капсом или символами" : "отправку ссылок";
                    ChatMessage.ircmessageWithRed("Вы замучены за " + reasonTranslated + " на " + duration + " минут!");
                }
            } else if (type.equals("unmute")) {
                isMuted = false;
                if (Rich.getInstance().isShowIrcMessages()) {
                    ChatMessage.ircmessageWithGreen("Вы размучены!");
                }
            } else if (type.equals("prefix_info")) {
                String prefix = json.get("prefix").getAsString();
                IRCCommand.setSelectedPrefix(prefix.isEmpty() ? null : prefix);
            } else if (type.equals("prefix_updated")) {
                String prefix = json.get("prefix").getAsString();
                IRCCommand.setSelectedPrefix(prefix.isEmpty() ? null : prefix);
            } else if (type.equals("system")) {
                if (!json.has("message")) return;
                String systemMessage = json.get("message").getAsString();
                if (Rich.getInstance().isShowIrcMessages()) {
                    ChatMessage.ircmessage(systemMessage);
                }
            }
        } catch (Exception ignored) {}
    }

    void displayMessage(String prefix, String message, String author, String content) {
        if (MinecraftClient.getInstance().player == null || !Rich.getInstance().isShowIrcMessages()) return;
        Text prefixText;
        if (author.equals("HZeed") && author.equals("Silv4ik") && author.equals("Raze")) {
            prefixText = ChatMessage.ircprefixDeveloper("");
        } else {
            switch (prefix) {
                case "pikmi":
                    prefixText = ChatMessage.ircprefixPikmi("");
                    break;
                case "labuba":
                    prefixText = ChatMessage.ircprefixLabuba("");
                    break;
                case "zapen":
                    prefixText = ChatMessage.ircprefixZapen("");
                    break;
                case "boost":
                    prefixText = ChatMessage.ircprefixBoost("");
                    break;
                case "rich":
                    prefixText = ChatMessage.ircprefixRich("");
                    break;
                case "panda":
                    prefixText = ChatMessage.ircprefixPanda("");
                    break;
                case "smiley":
                    prefixText = ChatMessage.ircprefixSmiley("");
                    break;
                case "bibi":
                    prefixText = ChatMessage.ircprefixBibi("");
                    break;
                case "benena":
                    prefixText = ChatMessage.ircprefixBenena("");
                    break;
                case "blyabuba":
                    prefixText = ChatMessage.ircprefixBlyabuba("");
                    break;
                default:
                    prefixText = Text.literal("");
                    break;
            }
        }
        Text ircPrefix = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
        Text username = Text.literal(author + " ").setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        Text arrow = Text.literal("-> ").setStyle(Style.EMPTY.withColor(Formatting.GRAY));
        Text contentText = Text.literal(content).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        Text formattedText = ircPrefix.copy().append(prefixText).append(username).append(arrow).append(contentText);
        MinecraftClient.getInstance().player.sendMessage(formattedText, false);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Rich.getInstance().getIrcManager().isConnecting.set(false);
    }

    @Override
    public void onError(Exception ex) {
        if (Rich.getInstance().isShowIrcMessages()) {
            ChatMessage.ircmessageWithRed("Ошибка подключения к IRC");
        }
        Rich.getInstance().getIrcManager().isConnecting.set(false);
    }

    public void sendMessage(String message) {
        if (!this.isOpen()) {
            return;
        }
        if (isMuted) {
            if (Rich.getInstance().isShowIrcMessages()) {
                ChatMessage.ircmessageWithRed("Вы замучены и не можете отправлять сообщения");
            }
            return;
        }
        String sender = UserProfile.getInstance().profile("username");
        JsonObject json = new JsonObject();
        json.addProperty("type", "text");
        json.addProperty("message", message);
        json.addProperty("author", sender);
        json.addProperty("clientId", clientId);
        this.send(cypher(json.toString()));
    }

    public void sendSetPrefix(String newPrefix) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "set_prefix");
        json.addProperty("new_prefix", newPrefix);
        json.addProperty("clientId", clientId);
        json.addProperty("author", UserProfile.getInstance().profile("username"));
        this.send(cypher(json.toString()));
    }

    private void sendGetPrefix() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "get_prefix");
        json.addProperty("clientId", clientId);
        this.send(cypher(json.toString()));
    }

    private String cypher(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= 0x15;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}