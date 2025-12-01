package fun.rich.common.logger;

import net.minecraft.text.Text;

public interface Logger {
    void log(Object message);
    void minecraftLog(Text... components);

}
