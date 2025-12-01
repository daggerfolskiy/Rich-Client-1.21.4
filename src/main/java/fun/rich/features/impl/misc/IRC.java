package fun.rich.features.impl.misc;

import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.Rich;
import fun.rich.utils.client.chat.ChatMessage;

public class IRC extends Module {
    public IRC() {
        super("IRC", ModuleCategory.MISC);
    }

    @Override
    public void setState(boolean state) {
        super.setState(state);
        if (state) {
            activate();
        } else {
            deactivate();
        }
    }

    @Override
    public void activate() {
        Rich.getInstance().setShowIrcMessages(true);
        Rich.getInstance().getIrcManager().connect();
    }

    @Override
    public void deactivate() {
        Rich.getInstance().setShowIrcMessages(false);
        Rich.getInstance().getIrcManager().disconnect();
    }

    public void sendMessage(String message) {
        if (!isState()) {
            ChatMessage.ircmessageWithRed("Модуль IRC выключен");
            return;
        }
        if (Rich.getInstance().getIrcManager().getClient() != null && Rich.getInstance().getIrcManager().getClient().isOpen()) {
            Rich.getInstance().getIrcManager().getClient().sendMessage(message);
        }
    }
}