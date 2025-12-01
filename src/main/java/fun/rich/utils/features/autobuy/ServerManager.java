package fun.rich.utils.features.autobuy;

import fun.rich.features.module.setting.implement.BooleanSetting;
import fun.rich.utils.math.time.TimerUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {
    private List<String> anarchyServers165 = new ArrayList<>();
    private List<String> anarchyServers214 = new ArrayList<>();
    private int currentServerIndex = 0;
    private String currentServer = "";
    private boolean inHub = false;
    private boolean waitingForServerLoad = false;

    private TimerUtil hubCheckTimer = TimerUtil.create();
    private TimerUtil serverSwitchCooldown = TimerUtil.create();

    private BooleanSetting bypassDelay;
    private BooleanSetting bypassDelay1214;

    public ServerManager(BooleanSetting bypassDelay, BooleanSetting bypassDelay1214) {
        this.bypassDelay = bypassDelay;
        this.bypassDelay1214 = bypassDelay1214;
        initializeServers();
    }

    private void initializeServers() {
        anarchyServers165.addAll(List.of("/an102", "/an103", "/an104", "/an105", "/an106", "/an107"));
        for (int i = 203; i <= 221; i++) {
            anarchyServers165.add("/an" + i);
        }
        for (int i = 302; i <= 313; i++) {
            anarchyServers165.add("/an" + i);
        }
        anarchyServers165.addAll(List.of("/an502", "/an503", "/an504", "/an505", "/an506", "/an507", "/an602"));

        for (int i = 11; i <= 14; i++) {
            anarchyServers214.add("/an" + i);
        }
        for (int i = 21; i <= 27; i++) {
            anarchyServers214.add("/an" + i);
        }
        for (int i = 31; i <= 34; i++) {
            anarchyServers214.add("/an" + i);
        }
        for (int i = 51; i <= 53; i++) {
            anarchyServers214.add("/an" + i);
        }
        anarchyServers214.add("/an91");
    }

    public void resetTimers() {
        hubCheckTimer.resetCounter();
        serverSwitchCooldown.resetCounter();
    }

    public void reset() {
        currentServerIndex = 0;
        currentServer = "";
        inHub = false;
        waitingForServerLoad = false;
    }

    public void updateHubStatus(ClientWorld world) {
        inHub = isInHubInternal(world);
    }

    private boolean isInHubInternal(ClientWorld world) {
        if (world == null) return true;
        Scoreboard scoreboard = world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return true;
        }
        String displayName = objective.getDisplayName().getString();
        return !displayName.contains("Анархия-");
    }

    private int getCurrentAnarchyNumber(ClientWorld world) {
        if (world == null) return -1;
        Scoreboard scoreboard = world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective != null) {
            String displayName = objective.getDisplayName().getString();
            if (displayName.contains("Анархия-")) {
                String[] parts = displayName.split("-");
                if (parts.length > 1) {
                    try {
                        return Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    private String getNextServer(List<String> servers, ClientWorld world) {
        if (servers.isEmpty()) return null;

        int currentAnarchy = getCurrentAnarchyNumber(world);

        if (currentAnarchy != -1) {
            String currentServerCmd = "/an" + currentAnarchy;
            int currentIdx = servers.indexOf(currentServerCmd);
            if (currentIdx != -1) {
                currentServerIndex = currentIdx;
            }
        }

        currentServerIndex = (currentServerIndex + 1) % servers.size();
        return servers.get(currentServerIndex);
    }

    public void switchToNextServer(ClientPlayerEntity player, NetworkManager networkManager, boolean isBuyer) {
        if (!serverSwitchCooldown.hasTimeElapsed(3000)) {
            return;
        }

        if (!isBuyer) {
            return;
        }

        List<String> availableServers = getAvailableServers();
        if (availableServers == null) return;

        ClientWorld world = (ClientWorld) player.getWorld();
        String newServer = getNextServer(availableServers, world);

        if (newServer != null) {
            currentServer = newServer;
            CommandSender.sendCommand(player, newServer);
            networkManager.sendToAllClients("switch_server:" + newServer);
            waitingForServerLoad = true;
            serverSwitchCooldown.resetCounter();
        }
    }

    public void joinAnarchyFromHub(ClientPlayerEntity player) {
        List<String> availableServers = getAvailableServers();
        if (availableServers == null || availableServers.isEmpty()) return;

        String server = availableServers.get(0);
        CommandSender.sendCommand(player, server);
        waitingForServerLoad = true;
        hubCheckTimer.resetCounter();
    }

    private List<String> getAvailableServers() {
        if (bypassDelay1214.isValue()) {
            return new ArrayList<>(anarchyServers214);
        } else if (bypassDelay.isValue()) {
            return new ArrayList<>(anarchyServers165);
        }
        return null;
    }

    public boolean shouldJoinAnarchy(boolean bypass165, boolean bypass1214) {
        return inHub && hubCheckTimer.hasTimeElapsed(3000) && (bypass165 || bypass1214);
    }

    public boolean isInHub() {
        return inHub;
    }

    public boolean isWaitingForServerLoad() {
        return waitingForServerLoad;
    }

    public void setWaitingForServerLoad(boolean value) {
        waitingForServerLoad = value;
    }
}