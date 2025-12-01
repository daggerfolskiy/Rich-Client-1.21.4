package fun.rich.common.discord.callbacks;

import com.sun.jna.Callback;
import fun.rich.common.discord.utils.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser var1);
}