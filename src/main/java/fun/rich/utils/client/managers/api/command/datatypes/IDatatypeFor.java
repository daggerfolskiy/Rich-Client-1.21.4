package fun.rich.utils.client.managers.api.command.datatypes;

import fun.rich.utils.client.managers.api.command.exception.CommandException;

public interface IDatatypeFor<T> extends IDatatype  {
    T get(IDatatypeContext datatypeContext) throws CommandException;
}
