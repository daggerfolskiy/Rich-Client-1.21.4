package fun.rich.utils.client.managers.api.command.datatypes;

import fun.rich.utils.client.managers.api.command.exception.CommandException;
import fun.rich.utils.display.interfaces.QuickImports;

import java.util.stream.Stream;

public interface IDatatype extends QuickImports {
    Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException;
}
