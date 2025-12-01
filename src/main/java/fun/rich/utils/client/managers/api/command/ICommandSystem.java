package fun.rich.utils.client.managers.api.command;

import fun.rich.utils.client.managers.api.command.argparser.IArgParserManager;

public interface ICommandSystem {
    IArgParserManager getParserManager();
}
