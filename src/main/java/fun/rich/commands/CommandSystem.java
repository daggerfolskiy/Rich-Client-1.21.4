package fun.rich.commands;

import fun.rich.utils.client.managers.api.command.ICommandSystem;
import fun.rich.utils.client.managers.api.command.argparser.IArgParserManager;
import fun.rich.commands.argparser.ArgParserManager;

public enum CommandSystem implements ICommandSystem {
    INSTANCE;

    @Override
    public IArgParserManager getParserManager() {
        return ArgParserManager.INSTANCE;
    }
}
