package fun.rich.events.block;

import net.minecraft.block.entity.BlockEntity;
import fun.rich.utils.client.managers.event.events.Event;

public record BlockEntityProgressEvent(BlockEntity blockEntity, Type type) implements Event {
    public enum Type {
        ADD, REMOVE
    }
}
