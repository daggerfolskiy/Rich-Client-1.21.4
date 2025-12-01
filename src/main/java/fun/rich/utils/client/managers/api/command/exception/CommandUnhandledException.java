package fun.rich.utils.client.managers.api.command.exception;

import fun.rich.utils.client.managers.api.command.ICommand;
import fun.rich.utils.client.managers.api.command.argument.ICommandArgument;
import fun.rich.utils.display.interfaces.QuickLogger;

import java.util.List;

public class CommandUnhandledException extends RuntimeException implements ICommandException, QuickLogger {

    public CommandUnhandledException(String message) {
        super(message);
    }

    public CommandUnhandledException(Throwable cause) {
        super(cause);
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args) {
    }
}
