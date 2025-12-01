package fun.rich.events.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import fun.rich.utils.client.managers.event.events.callables.EventCancellable;
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class AspectRatioEvent extends EventCancellable {
    float ratio;
}
