package fun.rich.utils.client.sound;

import antidaunleak.api.annotation.Native;
import fun.rich.utils.interactions.interact.PlayerInteractionHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;


import fun.rich.utils.display.interfaces.QuickImports;

@Setter
@Getter
@UtilityClass
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SoundManager implements QuickImports {
    public SoundEvent OPEN_GUI = SoundEvent.of(Identifier.of("minecraft:gui_open"));
    public SoundEvent CLOSE_GUI = SoundEvent.of(Identifier.of("minecraft:gui_close"));
    public SoundEvent ENABLE_MODULE = SoundEvent.of(Identifier.of("minecraft:module_enable"));
    public SoundEvent DISABLE_MODULE = SoundEvent.of(Identifier.of("minecraft:module_disable"));
    public SoundEvent CATEGORY_CLICK = SoundEvent.of(Identifier.of("minecraft:guicategory_select"));
    public SoundEvent ORTHODOX = SoundEvent.of(Identifier.of("minecraft:kolokolnia_kill"));


    public void init() {
        Registry.register(Registries.SOUND_EVENT, OPEN_GUI.id(), OPEN_GUI);
        Registry.register(Registries.SOUND_EVENT, CLOSE_GUI.id(), CLOSE_GUI);
        Registry.register(Registries.SOUND_EVENT, ENABLE_MODULE.id(), ENABLE_MODULE);
        Registry.register(Registries.SOUND_EVENT, DISABLE_MODULE.id(), DISABLE_MODULE);
        Registry.register(Registries.SOUND_EVENT, CATEGORY_CLICK.id(), CATEGORY_CLICK);
        Registry.register(Registries.SOUND_EVENT, ORTHODOX.id(), ORTHODOX);
    }

    public void playSound(SoundEvent sound) {
        playSound(sound, 1, 1);
    }

    public void playSound(SoundEvent sound, float volume, float pitch) {
        if (!PlayerInteractionHelper.nullCheck()) {
            mc.world.playSound(mc.player, mc.player.getBlockPos(), sound, SoundCategory.BLOCKS, volume, pitch);
        }
    }
}