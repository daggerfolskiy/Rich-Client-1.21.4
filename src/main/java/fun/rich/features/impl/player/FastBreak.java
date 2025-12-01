package fun.rich.features.impl.player;

import antidaunleak.api.annotation.Native;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.events.block.BlockBreakingEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FastBreak extends Module {

    public FastBreak() {super("FastBreak", "Fast Break", ModuleCategory.PLAYER);}

    @EventHandler

    public void onBlockBreaking(BlockBreakingEvent e) {
        BlockPos blockPos = e.blockPos();
        Direction direction = e.direction();
        if (mc.interactionManager.currentBreakingProgress >= 0.5) {
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, blockPos, direction));
        }
    }

}
