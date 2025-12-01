package fun.rich.features.impl.misc;

import antidaunleak.api.annotation.Native;
import fun.rich.events.player.TickEvent;
import fun.rich.features.impl.movement.AutoSprint;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.features.module.setting.implement.BindSetting;
import fun.rich.features.module.setting.implement.SelectSetting;
import fun.rich.utils.client.chat.ChatMessage;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.utils.interactions.inv.InventoryResult;
import fun.rich.utils.interactions.inv.InventoryToolkit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ClickPearl extends Module {

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    private final SelectSetting modeSetting = new SelectSetting("Режим", "Способ броска")
            .value("Default", "Legit")
            .selected("Default");
    private final BindSetting keySetting = new BindSetting("Кнопка", "Кнопка для использования");

    private boolean prevKeyPressed = false;
    private long lastThrowTime = 0L;
    private int packetSequence = 0;

    private int savedSlot = -1;
    private int pearlSlot = -1;
    private long actionStartTime = 0L;
    private boolean keysOverridden = false;
    private boolean wasForwardPressed, wasBackPressed, wasLeftPressed, wasRightPressed, wasJumpPressed;

    private enum Phase { READY, SLOWING_DOWN, PREPARE, AWAIT_SWITCH, THROW, SPEEDING_UP, FINISH }
    private Phase phase = Phase.READY;

    public ClickPearl() {
        super("ClickPearl", "Click Pearl", ModuleCategory.MISC);
        setup(modeSetting, keySetting);
    }

    @EventHandler
    @Native(type = Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (MC.player == null || MC.world == null) {
            resetState();
            return;
        }

        boolean keyDown = isBindActive();
        if (!prevKeyPressed && keyDown && System.currentTimeMillis() - lastThrowTime > 100 && phase == Phase.READY) {
            lastThrowTime = System.currentTimeMillis();
            startPearlProcess();
        }
        prevKeyPressed = keyDown;

        if (phase != Phase.READY) {
            execute();
        }
    }

    private void startPearlProcess() {
        if (MC.currentScreen != null) return;

        savedSlot = MC.player.getInventory().selectedSlot;
        InventoryResult hotbar = InventoryToolkit.findItemInHotBar(Items.ENDER_PEARL);
        if (hotbar.found()) {
            pearlSlot = hotbar.slot();
            InventoryToolkit.switchTo(pearlSlot);
            phase = Phase.AWAIT_SWITCH;
            return;
        }

        InventoryResult inv = InventoryToolkit.findItemInInventory(Items.ENDER_PEARL);
        if (inv.found()) {
            pearlSlot = inv.slot();
            if (modeSetting.getSelected().equals("Legit")) {
                wasForwardPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.forwardKey.getDefaultKey().getCode());
                wasBackPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.backKey.getDefaultKey().getCode());
                wasLeftPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.leftKey.getDefaultKey().getCode());
                wasRightPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.rightKey.getDefaultKey().getCode());
                wasJumpPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.jumpKey.getDefaultKey().getCode());

                phase = Phase.SLOWING_DOWN;
                actionStartTime = System.currentTimeMillis();
            } else {
                phase = Phase.PREPARE;
            }
        } else {
            ChatMessage.brandmessage("Нету жемчуга");
            resetState();
        }
    }

    private void execute() {
        if (MC.player == null || MC.currentScreen != null) {
            resetState();
            return;
        }

        long elapsed = System.currentTimeMillis() - actionStartTime;

        switch (phase) {
            case SLOWING_DOWN -> {
                MC.player.input.movementForward = 0;
                MC.player.input.movementSideways = 0;
                if (MC.player.isSprinting()) {
                    MC.player.setSprinting(false);
                    AutoSprint.tickStop = 1;
                }
                if (!keysOverridden) {
                    MC.options.forwardKey.setPressed(false);
                    MC.options.backKey.setPressed(false);
                    MC.options.leftKey.setPressed(false);
                    MC.options.rightKey.setPressed(false);
                    MC.options.jumpKey.setPressed(false);
                    keysOverridden = true;
                }
                if (elapsed > 1) {
                    phase = Phase.PREPARE;
                }
            }
            case PREPARE -> {
                int quickSwapSlot = MC.player.getInventory().selectedSlot;
                InventoryToolkit.clickSlot(pearlSlot, quickSwapSlot, SlotActionType.SWAP);
                InventoryToolkit.switchTo(quickSwapSlot);
                phase = Phase.AWAIT_SWITCH;
            }
            case AWAIT_SWITCH -> {
                if (MC.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
                    phase = Phase.THROW;
                }
            }
            case THROW -> {
                InventoryToolkit.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, packetSequence++, MC.player.getYaw(), MC.player.getPitch()));
                MC.player.swingHand(Hand.MAIN_HAND);

                boolean fromInventory = pearlSlot >= 9 && pearlSlot <= 35;
                if (fromInventory) {
                    int quickSwapSlot = MC.player.getInventory().selectedSlot;
                    InventoryToolkit.clickSlot(pearlSlot, quickSwapSlot, SlotActionType.SWAP);
                }
                InventoryToolkit.switchTo(savedSlot);

                if (modeSetting.getSelected().equals("Legit") && fromInventory) {
                    restoreKeyStates();
                    actionStartTime = System.currentTimeMillis();
                    phase = Phase.SPEEDING_UP;
                } else {
                    phase = Phase.FINISH;
                }
            }
            case SPEEDING_UP -> {
                long speedupElapsed = System.currentTimeMillis() - actionStartTime;
                float speedupProgress = Math.min(1.0f, speedupElapsed / 20.0f);
                if (MC.player.input != null) {
                    boolean forward = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.forwardKey.getDefaultKey().getCode());
                    float targetForward = forward ? 1.0f : 0;
                    MC.player.input.movementForward = lerp(MC.player.input.movementForward, targetForward * speedupProgress, 0.4f);
                    if (speedupProgress > 0.4f && forward && !MC.player.isSprinting()) {
                        MC.player.setSprinting(true);
                    }
                }
                if (speedupElapsed > 25) {
                    phase = Phase.FINISH;
                }
            }
            case FINISH -> resetState();
        }
    }

    private float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    private void restoreKeyStates() {
        if (!keysOverridden) return;
        boolean currentForward = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.forwardKey.getDefaultKey().getCode());
        boolean currentBack = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.backKey.getDefaultKey().getCode());
        boolean currentLeft = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.leftKey.getDefaultKey().getCode());
        boolean currentRight = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.rightKey.getDefaultKey().getCode());
        boolean currentJump = InputUtil.isKeyPressed(MC.getWindow().getHandle(), MC.options.jumpKey.getDefaultKey().getCode());

        MC.options.forwardKey.setPressed(wasForwardPressed && currentForward);
        MC.options.backKey.setPressed(wasBackPressed && currentBack);
        MC.options.leftKey.setPressed(wasLeftPressed && currentLeft);
        MC.options.rightKey.setPressed(wasRightPressed && currentRight);
        MC.options.jumpKey.setPressed(wasJumpPressed && currentJump);
        keysOverridden = false;
    }

    private void resetState() {
        if (keysOverridden) {
            restoreKeyStates();
        }
        pearlSlot = -1;
        savedSlot = -1;
        actionStartTime = 0L;
        phase = Phase.READY;
    }

    private boolean isBindActive() {
        long window = MC.getWindow().getHandle();
        int key = keySetting.getKey();

        if (key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_8) {
            return GLFW.glfwGetMouseButton(window, key) == GLFW.GLFW_PRESS;
        }
        return InputUtil.isKeyPressed(window, key);
    }
}