package fun.rich.common.repository.macro;

import antidaunleak.api.annotation.Native;
import fun.rich.utils.client.managers.event.EventManager;
import fun.rich.utils.client.managers.event.EventHandler;
import fun.rich.events.keyboard.KeyEvent;
import fun.rich.utils.display.interfaces.QuickImports;
import fun.rich.utils.display.interfaces.QuickLogger;

import java.util.ArrayList;
import java.util.List;

public class MacroRepository implements QuickImports, QuickLogger {
    public MacroRepository(EventManager eventManager) {
        eventManager.register(this);
    }

    public List<Macro> macroList = new ArrayList<>();

    public void addMacro(String name, String message, int key) {
        macroList.add(new Macro(name, message, key));
    }

    public boolean hasMacro(String text) {
        return macroList.stream().anyMatch(macro -> macro.name().equalsIgnoreCase(text));
    }

    public void deleteMacro(String text) {
        macroList.removeIf(macro -> macro.name().equalsIgnoreCase(text));
    }

    public void clearList() {
        macroList.clear();
    }

    @EventHandler

    public void onKey(KeyEvent e) {
        if (mc.player != null && e.action() == 0 && mc.currentScreen == null) macroList.stream().filter(macro -> macro.key() == e.key())
                .findFirst().ifPresent(macro -> mc.player.networkHandler.sendChatMessage(macro.message()));
    }
}