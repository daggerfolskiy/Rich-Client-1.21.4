package fun.rich.mixins.client.display.title;

import fun.rich.display.screens.mainmenu.MainMenu;
import fun.rich.features.impl.misc.SelfDestruct;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class TitleScreenMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void replaceTitleScreen(Screen screen, CallbackInfo ci) {
        if (SelfDestruct.unhooked) return;

        MinecraftClient client = (MinecraftClient)(Object)this;
        if (screen instanceof TitleScreen || (screen == null && client.world == null)) {
            client.setScreen(new MainMenu());
            ci.cancel();
        }
    }
}