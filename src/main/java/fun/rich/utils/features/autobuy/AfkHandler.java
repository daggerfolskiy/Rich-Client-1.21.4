package fun.rich.utils.features.autobuy;

import fun.rich.utils.math.time.TimerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

public class AfkHandler {
    private TimerUtil afkActionTimer = TimerUtil.create();
    private boolean wasInAfk = false;
    private boolean performingAfkAction = false;
    private int afkActionStep = 0;

    public void resetTimers() {
        afkActionTimer.resetCounter();
        wasInAfk = false;
        performingAfkAction = false;
        afkActionStep = 0;
    }

    public void handle(MinecraftClient mc) {
        boolean currentlyInAfk = isInAfkMode(mc);

        if (currentlyInAfk && !wasInAfk) {
            performingAfkAction = true;
            afkActionStep = 0;
            afkActionTimer.resetCounter();
        }

        wasInAfk = currentlyInAfk;

        if (performingAfkAction) {
            if (afkActionTimer.hasTimeElapsed(100)) {
                processAfkAction(mc);
            }
        }
    }

    private void processAfkAction(MinecraftClient mc) {
        switch (afkActionStep) {
            case 0:
                mc.options.forwardKey.setPressed(true);
                afkActionStep++;
                afkActionTimer.resetCounter();
                break;
            case 1:
                mc.options.forwardKey.setPressed(false);
                afkActionStep++;
                afkActionTimer.resetCounter();
                break;
            case 2:
                float currentYaw = mc.player.getYaw();
                mc.player.setYaw(currentYaw + 45);
                afkActionStep++;
                afkActionTimer.resetCounter();
                break;
            case 3:
                performingAfkAction = false;
                afkActionStep = 0;
                break;
        }
    }

    private boolean isInAfkMode(MinecraftClient mc) {
        if (mc.inGameHud == null) return false;
        return mc.inGameHud.getBossBarHud().bossBars.values().stream()
                .map(bar -> bar.getName().getString().toLowerCase())
                .anyMatch(text -> text.contains("afk"));
    }

    public void resetMovementKeys(GameOptions options) {
        if (options != null) {
            options.forwardKey.setPressed(false);
            options.backKey.setPressed(false);
            options.leftKey.setPressed(false);
            options.rightKey.setPressed(false);
        }
    }

    public boolean isPerformingAction() {
        return performingAfkAction;
    }
}