package fun.rich.utils.features.autobuy;

public class ServerSwitchHandler {
    private static boolean waitingForServerLoad = false;
    private static long serverSwitchTime = 0;

    public static void setWaitingForServerLoad(boolean value) {
        waitingForServerLoad = value;
    }

    public static boolean isWaitingForServerLoad() {
        return waitingForServerLoad;
    }

    public static void setServerSwitchTime(long time) {
        serverSwitchTime = time;
    }

    public static long getServerSwitchTime() {
        return serverSwitchTime;
    }

    public static boolean hasTimedOut() {
        return System.currentTimeMillis() - serverSwitchTime > 10000;
    }
}