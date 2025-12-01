package fun.rich.events.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import fun.rich.utils.client.managers.event.events.Event;

public record BlockBreakingEvent(BlockPos blockPos, Direction direction) implements Event {}
