package fun.rich.utils.features.aura.utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.ItemStack;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.utils.client.packet.network.Network;
import fun.rich.Rich;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Pressing implements QuickImports {
    private final int[] funTimeTicks = new int[]{10, 11, 10, 13}, spookyTicks = new int[]{11, 10, 13, 10, 12, 11, 12}, defaultTicks = new int[]{10, 11};
    long lastClickTime = System.currentTimeMillis();
    private static final long MINIMUM_COOLDOWN_MS = 500;


    public boolean isCooldownComplete(boolean dynamicCooldown, int ticks) {
        boolean isMace = isHoldingMace();

        boolean cooldownReady = isMace || mc.player.getAttackCooldownProgress(ticks) > 0.9F;
        boolean minimumDelayPassed = lastClickPassed() >= 500;

        return cooldownReady && minimumDelayPassed;
    }

    public boolean hasTicksElapsedSinceLastClick(int ticks) {
        return lastClickPassed() >= (ticks * 50L * (20F / Network.TPS));
    }

    public long lastClickPassed() {
        return System.currentTimeMillis() - lastClickTime;
    }

    public void recalculate() {
        lastClickTime = System.currentTimeMillis();
    }

    int tickCount() {
        int count = Rich.getInstance().getAttackPerpetrator().getAttackHandler().getCount();
        return switch (Network.server) {

            default -> defaultTicks[count % defaultTicks.length];
        };
    }

    private boolean isHoldingMace() {
        ItemStack mainHand = mc.player.getMainHandStack();

        return mainHand.getItem().getTranslationKey().toLowerCase().contains("mace");
    }
}