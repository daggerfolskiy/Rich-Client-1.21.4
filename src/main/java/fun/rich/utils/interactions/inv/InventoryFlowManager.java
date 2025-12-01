package fun.rich.utils.interactions.inv;

import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import fun.rich.utils.interactions.simulate.Simulations;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import fun.rich.features.module.Module;
import fun.rich.features.module.ModuleCategory;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.utils.math.task.TaskPriority;
import fun.rich.utils.math.script.Script;
import fun.rich.utils.client.packet.network.Network;
import fun.rich.events.player.InputEvent;
import fun.rich.utils.features.aura.utils.MathAngle;
import fun.rich.utils.features.aura.warp.TurnsConfig;
import fun.rich.utils.features.aura.warp.TurnsConnection;
import fun.rich.display.screens.clickgui.MenuScreen;

import java.util.List;

@UtilityClass
public class InventoryFlowManager implements QuickImports {
    public final List<KeyBinding> moveKeys = List.of(mc.options.forwardKey, mc.options.backKey, mc.options.leftKey, mc.options.rightKey, mc.options.jumpKey);
    public static final Script script = new Script(), postScript = new Script();
    public boolean canMove = true;

    public void tick() {
        script.update();
    }

    public void postMotion() {
        postScript.update();
    }

    public void input(InputEvent e) {
        if (!canMove) e.inputNone();
    }

    public void addTask(Runnable task) {
        if (script.isFinished() && Simulations.hasPlayerMovement()) {
            switch (Network.server) {
                case "FunTime" -> {
                    script.cleanup().addTickStep(0, () -> {
                        InventoryFlowManager.disableMoveKeys();
                        InventoryFlowManager.rotateToCamera();
                    }).addTickStep(1, () -> {
                        task.run();
                        enableMoveKeys();
                    });
                    return;
                }
                case "ReallyWorld" -> {
                    if (mc.player.isOnGround()) {
                        script.cleanup().addTickStep(0, InventoryFlowManager::disableMoveKeys).addTickStep(2, InventoryFlowManager::rotateToCamera).addTickStep(3, task::run)
                                .addTickStep(4, InventoryFlowManager::enableMoveKeys);
                        return;
                    }
                }
                case "SpookyTime", "CopyTime" -> {
                    script.cleanup().addTickStep(0, ()-> {
                                InventoryFlowManager.disableMoveKeys();
                                InventoryFlowManager.rotateToCamera();
                            }).addTickStep(1, task::run)
                            .addTickStep(2, InventoryFlowManager::enableMoveKeys);
                    return;
                }
            }
        }
        script.addTickStep(0, InventoryFlowManager::rotateToCamera);
        postScript.cleanup().addTickStep(0, () -> {
            task.run();
            InventoryTask.closeScreen(true);
        });
    }

    private void rotateToCamera() {
        Module module = new Module("InventoryComponent","Inventory Component", ModuleCategory.PLAYER);
        module.state = true;
        TurnsConnection.INSTANCE.rotateTo(MathAngle.cameraAngle(), TurnsConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_3, module);
    }

    public void disableMoveKeys() {
        canMove = false;
        unPressMoveKeys();
    }

    public void enableMoveKeys() {
        InventoryTask.closeScreen(true);
        canMove = true;
        updateMoveKeys();
    }

    public void unPressMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(false));
    }

    public void updateMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBinding.getDefaultKey().getCode())));
    }

    public boolean shouldSkipExecution() {
        return mc.currentScreen != null && !PlayerInteractionHelper.isChat(mc.currentScreen) && !(mc.currentScreen instanceof SignEditScreen) && !(mc.currentScreen instanceof AnvilScreen)
                && !(mc.currentScreen instanceof AbstractCommandBlockScreen) && !(mc.currentScreen instanceof StructureBlockScreen) && !(mc.currentScreen instanceof MenuScreen);
    }
}