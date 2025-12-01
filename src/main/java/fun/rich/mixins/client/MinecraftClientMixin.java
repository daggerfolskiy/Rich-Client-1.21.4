package fun.rich.mixins.client;

import fun.rich.features.impl.misc.SelfDestruct;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fun.rich.utils.client.managers.event.EventManager;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.Rich;
import fun.rich.utils.client.managers.file.exception.FileProcessingException;
import fun.rich.utils.display.font.Fonts;
import fun.rich.utils.client.logs.Logger;
import fun.rich.events.container.SetScreenEvent;
import fun.rich.events.player.HotBarUpdateEvent;
import fun.rich.features.impl.combat.NoInteract;
import fun.rich.utils.client.window.WindowStyle;
import fun.rich.utils.client.window.WindowTitleAnimation;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements QuickImports {
    @Shadow @Nullable public abstract ClientPlayNetworkHandler getNetworkHandler();
    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Final public GameRenderer gameRenderer;
    @Shadow @Nullable public Screen currentScreen;
    private final WindowTitleAnimation titleUtil = WindowTitleAnimation.getInstance();

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onInit(RunArgs args, CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;
        Fonts.init();
        MinecraftClient.getInstance().getWindow().setTitle(titleUtil.getCurrentTitle());
    }

    @Inject(at = @At("HEAD"), method = "stop")
    private void stop(CallbackInfo ci) {
        Logger.info("Stopping for MinecraftClient");
        if (Rich.getInstance().isInitialized()) {
            try {
                Rich.getInstance().getFileController().saveFiles();
            } catch (FileProcessingException e) {
                Logger.error("Error occurred while saving files: " + e.getMessage() + " " + e.getCause());
            } finally {
                Rich.getInstance().getFileController().stopAutoSave();
            }
        }
    }

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"), cancellable = true)
    public void doItemUseHook(CallbackInfo ci) {
        if (NoInteract.getInstance().isState()) {
            for (Hand hand : Hand.values()) {
                if (player.getStackInHand(hand).isEmpty()) continue;
                ActionResult result = interactionManager.interactItem(player, hand);
                if (result.isAccepted()) {
                    if (result instanceof ActionResult.Success success && success.swingSource().equals(ActionResult.SwingSource.CLIENT)) {
                        gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                        player.swingHand(hand);
                    }
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "setScreen", at = @At(value = "HEAD"), cancellable = true)
    public void setScreenHook(Screen screen, CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        SetScreenEvent event = new SetScreenEvent(screen);
        EventManager.callEvent(event);
        Rich.getInstance().getDraggableRepository().draggable().forEach(drag -> drag.setScreen(event));
        Screen eventScreen = event.getScreen();
        if (screen != eventScreen) {
            mc.setScreen(eventScreen);
            ci.cancel();
        }
    }

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void applyDarkMode(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        WindowStyle.setDarkMode(client.getWindow().getHandle());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        titleUtil.updateTitle();
        MinecraftClient.getInstance().getWindow().setTitle(titleUtil.getCurrentTitle());
    }

    @Inject(method = "updateWindowTitle", at = @At("HEAD"), cancellable = true)
    private void onUpdateWindowTitle(CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        MinecraftClient.getInstance().getWindow().setTitle(titleUtil.getCurrentTitle());
        ci.cancel();
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"), cancellable = true)
    public void handleInputEventsHook(CallbackInfo ci) {
        HotBarUpdateEvent event = new HotBarUpdateEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }
}