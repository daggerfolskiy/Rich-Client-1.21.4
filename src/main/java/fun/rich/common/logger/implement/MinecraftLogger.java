package fun.rich.common.logger.implement;

import antidaunleak.api.annotation.Native;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import fun.rich.common.logger.Logger;
import fun.rich.utils.display.interfaces.QuickImports;

import java.util.Arrays;

public class MinecraftLogger implements Logger, QuickImports {
    @Override
    public void log(Object message) {

    }

    @Override

    public void minecraftLog(Text... components) {
        if (mc.player != null) {
            MutableText component = Text.literal("");
            Arrays.asList(components).forEach(component::append);
            mc.inGameHud.getChatHud().addMessage(component);
        }
    }
}
