package fun.rich.events.block;

import net.minecraft.util.math.BlockPos;
import fun.rich.utils.client.managers.event.events.Event;

public record BreakBlockEvent(BlockPos blockPos) implements Event {}
