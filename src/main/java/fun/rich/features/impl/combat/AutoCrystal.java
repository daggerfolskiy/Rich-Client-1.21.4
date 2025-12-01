package fun.rich.features.impl.combat;

import antidaunleak.api.annotation.Native;
import fun.rich.common.repository.friend.FriendUtils;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.MultiSelectSetting;
import fun.rich.features.module.setting.implement.SliderSettings;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.interactions.inv.InventoryFlowManager;
import fun.rich.utils.interactions.inv.InventoryTask;
import fun.rich.utils.math.script.Script;
import fun.rich.events.packet.PacketEvent;
import fun.rich.events.player.EntitySpawnEvent;
import fun.rich.events.player.TickEvent;
import fun.rich.utils.client.managers.event.EventHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoCrystal extends Module {
    private final Script script = new Script();
    private BlockPos obsPosition;

    private final MultiSelectSetting protections = new MultiSelectSetting("Защита", "Что не взрывать")
            .value("Себя", "Друзей", "Ресурсы")
            .selected("Себя", "Друзей", "Ресурсы");

    private final SliderSettings itemRange = new SliderSettings("Дистанция до ресурсов", "Минимальное расстояние до ресурсов")
            .range(1.0f, 12.0f)
            .setValue(6.0f);

    public AutoCrystal() {
        super("AutoCrystal", "Auto Crystal", ModuleCategory.COMBAT);
        setup(protections, itemRange);
    }

    @Override
    public void activate() {
        obsPosition = null;
        super.activate();
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof PlayerInteractBlockC2SPacket interact && interact.getSequence() != 0 && script.isFinished() && InventoryFlowManager.script.isFinished())
            script.addTickStep(0, () -> {
                BlockPos interactPos = interact.getBlockHitResult().getBlockPos();
                BlockPos spawnPos = interactPos.offset(interact.getBlockHitResult().getSide());
                BlockPos blockPos = mc.world.getBlockState(spawnPos).getBlock().equals(Blocks.OBSIDIAN) ? spawnPos : mc.world.getBlockState(interactPos).getBlock().equals(Blocks.OBSIDIAN) ? interactPos : null;
                Slot crystal = InventoryTask.getSlot(Items.END_CRYSTAL);

                if (blockPos != null && crystal != null && isSafePosition(blockPos)) {
                    InventoryFlowManager.addTask(() -> {
                        obsPosition = blockPos;
                        InventoryTask.swapHand(crystal, Hand.MAIN_HAND, false);
                        PlayerInteractionHelper.sendSequencedPacket(i -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(blockPos.toCenterPos(), Direction.UP, blockPos, false), i));
                        InventoryTask.swapHand(crystal, Hand.MAIN_HAND, false, true);
                        script.cleanup().addTickStep(6, () -> obsPosition = null);
                    });
                }
            });
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (e.getEntity() instanceof EndCrystalEntity crystal && obsPosition != null && obsPosition.equals(crystal.getBlockPos().down())) {
            if (isSafeToDamage(crystal)) {
                mc.interactionManager.attackEntity(mc.player, crystal);
            }
            obsPosition = null;
            script.cleanup();
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        script.update();
    }

    private boolean isSafePosition(BlockPos pos) {
        if (protections.isSelected("Себя")) {
            if (mc.player.getY() > pos.getY()) {
                return false;
            }
        }

        if (protections.isSelected("Друзей")) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;
                if (FriendUtils.isFriend(player)) {
                    if (player.getY() > pos.getY()) {
                        return false;
                    }
                }
            }
        }

        if (protections.isSelected("Ресурсы")) {
            Vec3d crystalPos = pos.up().toCenterPos();
            double range = itemRange.getValue();
            Box box = new Box(crystalPos.x - range, crystalPos.y - range, crystalPos.z - range, crystalPos.x + range, crystalPos.y + range, crystalPos.z + range);
            List<Entity> entities = mc.world.getOtherEntities(mc.player, box);

            for (Entity entity : entities) {
                if (entity instanceof ItemEntity) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isSafeToDamage(EndCrystalEntity crystal) {
        BlockPos crystalBlock = crystal.getBlockPos().down();

        if (protections.isSelected("Себя")) {
            if (mc.player.getY() > crystalBlock.getY()) {
                return false;
            }
        }

        if (protections.isSelected("Друзей")) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;
                if (FriendUtils.isFriend(player)) {
                    if (player.getY() > crystalBlock.getY()) {
                        return false;
                    }
                }
            }
        }

        if (protections.isSelected("Ресурсы")) {
            Vec3d crystalPos = crystal.getPos();
            double range = itemRange.getValue();
            Box box = new Box(crystalPos.x - range, crystalPos.y - range, crystalPos.z - range, crystalPos.x + range, crystalPos.y + range, crystalPos.z + range);
            List<Entity> entities = mc.world.getOtherEntities(mc.player, box);

            for (Entity entity : entities) {
                if (entity instanceof ItemEntity) {
                    return false;
                }
            }
        }

        return true;
    }
}