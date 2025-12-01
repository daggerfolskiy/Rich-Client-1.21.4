package fun.rich.events.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import fun.rich.utils.client.managers.event.events.callables.EventCancellable;

@Getter
@Setter
@AllArgsConstructor
public class UsingItemEvent extends EventCancellable {
    byte type;
}
