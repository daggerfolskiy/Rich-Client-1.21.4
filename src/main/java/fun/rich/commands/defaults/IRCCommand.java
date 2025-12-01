package fun.rich.commands.defaults;

import fun.rich.utils.client.managers.api.command.Command;
import fun.rich.utils.client.managers.api.command.argument.IArgConsumer;
import fun.rich.utils.client.managers.api.command.exception.CommandException;
import fun.rich.utils.client.managers.api.command.helpers.TabCompleteHelper;
import fun.rich.features.impl.misc.IRC;
import fun.rich.Rich;
import fun.rich.utils.client.chat.ChatMessage;
import fun.rich.utils.client.text.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class IRCCommand extends Command {
    private static String selectedPrefix = null;

    protected IRCCommand() {
        super("irc");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        IRC ircModule = Rich.getInstance().getModuleRepository().modules().stream()
                .filter(module -> module instanceof IRC)
                .map(module -> (IRC) module)
                .findFirst()
                .orElse(null);
        if (ircModule == null) {
            ChatMessage.ircmessageWithRed("Модуль IRC не найден");
            return;
        }
        if (!args.hasAny()) {
            sendUsage();
            return;
        }
        String action = args.getString().toLowerCase();
        switch (action) {
            case "prefix":
                if (args.hasAny()) {
                    String sub = args.getString().toLowerCase();
                    if (sub.equals("list")) {
                        displayPrefixList();
                    } else {
                        handlePrefixSelection(sub);
                    }
                } else {
                    ChatMessage.ircmessageWithRed("Укажите номер префикса: .irc prefix <1-10> или .irc prefix list");
                }
                break;
            case "clear":
                selectedPrefix = null;
                sendSetPrefix("");
                ChatMessage.ircmessage("Префикс сброшен");
                break;
            default:
                if (!ircModule.isState()) {
                    ChatMessage.ircmessageWithRed("Модуль IRC отключен");
                    return;
                }
                if (MinecraftClient.getInstance().player == null) {
                    ChatMessage.ircmessageWithRed("Игрок не инициализирован");
                    return;
                }
                String message = action + (args.hasAny() ? " " + args.rawRest() : "");
                ircModule.sendMessage(message);
                break;
        }
    }

    @Override
    public String getShortDesc() {
        return "Команда для управления IRC-чатом: отправка сообщений.";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Эта команда позволяет управлять IRC-чатом, включая отправку сообщений, включение/выключение модуля и выбор префиксов для сообщений.",
                "",
                "Использование:",
                "> irc <сообщение> - Отправляет сообщение в IRC-чат (например: .irc Привет, мир!).",
                "> irc prefix <1-10> - Выбирает префикс для сообщений (например: .irc prefix 1).",
                "> irc prefix list - Отображает список доступных префиксов.",
                "> irc clear - Сбрасывает выбранный префикс."
        );
    }

    private void handlePrefixSelection(String prefixNumber) {
        String[] prefixes = {"pikmi", "labuba", "zapen", "boost", "rich", "panda", "smiley", "bibi", "benena", "blyabuba"};
        String[] displayNames = {"Пикми", "Лабуба", "Запен", "Буст", "Рич", "Панда", "(●'◡'●)", "Биби...!", "Бэнена", "Блябуба"};
        try {
            int index = Integer.parseInt(prefixNumber) - 1;
            if (index >= 0 && index < prefixes.length) {
                selectedPrefix = prefixes[index];
                if (MinecraftClient.getInstance().player != null) {
                    Text ircPrefix = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
                    Text messageText = Text.literal("Успешно установлен префикс ").setStyle(net.minecraft.text.Style.EMPTY.withColor(Formatting.WHITE));
                    Text prefixText = getPrefixText(displayNames[index], "");
                    Text fullMessage = ircPrefix.copy().append(messageText).append(prefixText);
                    MinecraftClient.getInstance().player.sendMessage(fullMessage, false);
                }
                sendSetPrefix(selectedPrefix);
            } else {
                ChatMessage.ircmessageWithRed("Неверный номер префикса. Используйте .irc prefix list (1-10)");
            }
        } catch (NumberFormatException e) {
            ChatMessage.ircmessageWithRed("Неверный ввод. Используйте число от 1 до 10");
        }
    }

    private void displayPrefixList() {
        ChatMessage.ircmessage("Список префиксов:");
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.literal("1. ").append(ChatMessage.ircprefixPikmi("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("2. ").append(ChatMessage.ircprefixLabuba("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("3. ").append(ChatMessage.ircprefixZapen("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("4. ").append(ChatMessage.ircprefixBoost("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("5. ").append(ChatMessage.ircprefixRich("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("6. ").append(ChatMessage.ircprefixPanda("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("7. ").append(ChatMessage.ircprefixSmiley("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("8. ").append(ChatMessage.ircprefixBibi("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("9. ").append(ChatMessage.ircprefixBenena("")), false);
            MinecraftClient.getInstance().player.sendMessage(Text.literal("10. ").append(ChatMessage.ircprefixBlyabuba("")), false);
        }
    }

    public void sendUsage() {
        ChatMessage.helpmessage("Пример использования команды .irc:");
        ChatMessage.brandmessage(".irc <сообщение> - Отправить сообщение в IRC-чат (например: .irc Привет, мир!)");
        ChatMessage.brandmessage(".irc prefix <1-10> - Выбрать префикс для сообщений (например: .irc prefix 1)");
        ChatMessage.brandmessage(".irc prefix list - Показать список доступных префиксов");
        ChatMessage.brandmessage(".irc clear - Сбросить выбранный префикс");
    }

    private Text getPrefixText(String prefixName, String message) {
        switch (prefixName) {
            case "Пикми":
                return ChatMessage.ircprefixPikmi(message);
            case "Лабуба":
                return ChatMessage.ircprefixLabuba(message);
            case "Запен":
                return ChatMessage.ircprefixZapen(message);
            case "Буст":
                return ChatMessage.ircprefixBoost(message);
            case "Рич":
                return ChatMessage.ircprefixRich(message);
            case "Панда":
                return ChatMessage.ircprefixPanda(message);
            case "(●'◡'●)":
                return ChatMessage.ircprefixSmiley(message);
            case "Биби...!":
                return ChatMessage.ircprefixBibi(message);
            case "Бэнена":
                return ChatMessage.ircprefixBenena(message);
            case "Блябуба":
                return ChatMessage.ircprefixBlyabuba(message);
            default:
                return Text.literal(message);
        }
    }

    private void sendSetPrefix(String prefix) {
        if (Rich.getInstance().getIrcManager().getClient() != null && Rich.getInstance().getIrcManager().getClient().isOpen()) {
            Rich.getInstance().getIrcManager().getClient().sendSetPrefix(prefix);
        }
    }

    public static void setSelectedPrefix(String prefix) {
        selectedPrefix = prefix;
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            return new TabCompleteHelper().sortAlphabetically().prepend("prefix", "clear").stream();
        } else if (args.hasAny() && args.hasExactlyOne()) {
            String partial = args.getString().toLowerCase();
            return new TabCompleteHelper().sortAlphabetically().prepend("prefix", "clear").filterPrefix(partial).stream();
        } else if (args.hasAny()) {
            String arg = args.getString().toLowerCase();
            if (arg.equals("prefix")) {
                if (args.hasAny()) {
                    String partial = args.getString().toLowerCase();
                    return new TabCompleteHelper().sortAlphabetically().prepend("list", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10").filterPrefix(partial).stream();
                } else {
                    return new TabCompleteHelper().sortAlphabetically().prepend("list", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10").stream();
                }
            }
        }
        return Stream.empty();
    }
}