package fun.rich.utils.features.autobuy;

import fun.rich.utils.client.chat.ChatMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {
    private static final int PORT = 20001;
    private static final int RECONNECT_DELAY = 2000;

    private ServerSocket serverSocket = null;
    private Socket clientSocket = null;
    private PrintWriter clientOut = null;
    private BufferedReader clientIn = null;
    private List<Socket> connections = new ArrayList<>();
    private Map<Socket, PrintWriter> outs = new ConcurrentHashMap<>();
    private Map<Socket, BufferedReader> ins = new ConcurrentHashMap<>();
    private Map<Socket, Boolean> clientInAuction = new ConcurrentHashMap<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private volatile boolean running = false;
    private volatile boolean isClientMode = false;
    private long lastReconnectAttempt = 0;

    private ConcurrentLinkedQueue<BuyRequest> queue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<BuyRequest> priorityQueue = new ConcurrentLinkedQueue<>();

    public void start(String mode) {
        running = true;
        isClientMode = mode.equals("Проверяющий");
        executorService.execute(() -> connectionLoop(mode));
    }

    public void stop() {
        running = false;
        executorService.shutdownNow();
        executorService = Executors.newFixedThreadPool(10);
        stopAll();
    }

    private void connectionLoop(String mode) {
        while (running) {
            if (mode.equals("Покупающий")) {
                startServer();
            } else if (mode.equals("Проверяющий")) {
                long currentTime = System.currentTimeMillis();
                if (clientSocket == null || clientSocket.isClosed()) {
                    if (currentTime - lastReconnectAttempt >= RECONNECT_DELAY) {
                        startClient();
                        lastReconnectAttempt = currentTime;
                    }
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
        }
    }

    private void startServer() {
        if (serverSocket == null || serverSocket.isClosed()) {
            try {
                serverSocket = new ServerSocket(PORT);
                ChatMessage.brandmessage("Сервер запущен на порту " + PORT);
                executorService.execute(this::listenerThread);
            } catch (IOException e) {
                ChatMessage.brandmessage("Ошибка запуска сервера");
            }
        }
    }

    private void startClient() {
        try {
            clientSocket = new Socket("localhost", PORT);
            clientSocket.setTcpNoDelay(true);
            clientSocket.setSoTimeout(0);
            clientSocket.setKeepAlive(true);
            clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
            clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientOut.println("connect");
            executorService.execute(this::clientReaderThread);
            ChatMessage.brandmessage("Подключено к покупающему аккаунту");
        } catch (IOException e) {
            clientSocket = null;
            clientOut = null;
            clientIn = null;
        }
    }

    private void listenerThread() {
        try {
            while (running && serverSocket != null && !serverSocket.isClosed()) {
                Socket conn = serverSocket.accept();
                conn.setTcpNoDelay(true);
                conn.setKeepAlive(true);
                conn.setSoTimeout(0);
                connections.add(conn);
                PrintWriter out = new PrintWriter(conn.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                outs.put(conn, out);
                ins.put(conn, in);
                clientInAuction.put(conn, false);
                ChatMessage.brandmessage("Подключен аккаунт с проверяющим");
                executorService.execute(() -> readerThread(conn));
            }
        } catch (IOException ignored) {}
    }

    private void readerThread(Socket conn) {
        try {
            BufferedReader in = ins.get(conn);
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("buy:")) {
                    handleBuyMessage(line);
                } else if (line.equals("enter_auction")) {
                    clientInAuction.put(conn, true);
                } else if (line.equals("leave_auction")) {
                    clientInAuction.put(conn, false);
                } else if (line.equals("ping")) {
                    PrintWriter out = outs.get(conn);
                    if (out != null) {
                        out.println("pong");
                    }
                }
            }
        } catch (IOException ignored) {}
        finally {
            removeConnection(conn);
        }
    }

    private void handleBuyMessage(String line) {
        try {
            String[] parts = line.substring(4).split("\\|");
            if (parts.length == 2) {
                String itemName = parts[0];
                int price = Integer.parseInt(parts[1]);
                BuyRequest request = new BuyRequest(itemName, price);
                priorityQueue.add(request);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void clientReaderThread() {
        try {
            String line;
            while ((line = clientIn.readLine()) != null) {
                if (line.equals("update_now")) {
                    ClientUpdateHandler.handleUpdate();
                } else if (line.startsWith("switch_server:")) {
                    String cmd = line.substring(14);
                    CommandSender.handleServerSwitch(cmd);
                } else if (line.equals("open_auction")) {
                    CommandSender.openAuction();
                } else if (line.equals("pong")) {
                }
            }
        } catch (IOException ignored) {}
        finally {
            stopClient();
            if (running && isClientMode) {
                try {
                    Thread.sleep(RECONNECT_DELAY);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void removeConnection(Socket conn) {
        connections.remove(conn);
        outs.remove(conn);
        ins.remove(conn);
        clientInAuction.remove(conn);
        try {
            conn.close();
        } catch (IOException ignored) {}
    }

    private void stopAll() {
        queue.clear();
        priorityQueue.clear();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {}
            serverSocket = null;
        }
        for (Socket conn : new ArrayList<>(connections)) {
            removeConnection(conn);
        }
        stopClient();
    }

    private void stopClient() {
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException ignored) {}
            clientSocket = null;
        }
        clientOut = null;
        clientIn = null;
    }

    public void sendToAllClients(String message) {
        List<Socket> deadConnections = new ArrayList<>();
        for (Socket conn : new ArrayList<>(connections)) {
            PrintWriter out = outs.get(conn);
            if (out != null) {
                try {
                    out.println(message);
                    if (out.checkError()) {
                        deadConnections.add(conn);
                    }
                } catch (Exception e) {
                    deadConnections.add(conn);
                }
            }
        }
        for (Socket conn : deadConnections) {
            removeConnection(conn);
        }
    }

    public void sendBuy(String itemName, int price) {
        if (clientOut != null) {
            try {
                clientOut.println("buy:" + itemName + "|" + price);
                if (clientOut.checkError()) {
                    stopClient();
                }
            } catch (Exception ignored) {}
        }
    }

    public void notifyAuctionEnter() {
        if (clientOut != null) {
            try {
                clientOut.println("enter_auction");
            } catch (Exception ignored) {}
        }
    }

    public void notifyAuctionLeave() {
        if (clientOut != null) {
            try {
                clientOut.println("leave_auction");
            } catch (Exception ignored) {}
        }
    }

    public void sendUpdateToClients() {
        sendToAllClients("update_now");
    }

    public void requestAuctionOpen() {
        sendToAllClients("open_auction");
    }

    public long getClientInAuctionCount() {
        return clientInAuction.values().stream().filter(Boolean::booleanValue).count();
    }

    public boolean hasConnectedClients() {
        return !connections.isEmpty();
    }

    public boolean isConnectedToServer() {
        return clientSocket != null && !clientSocket.isClosed() && clientOut != null;
    }

    public BuyRequest pollRequest() {
        BuyRequest request = priorityQueue.poll();
        if (request == null) {
            request = queue.poll();
        }
        return request;
    }

    public int getQueueSize() {
        return priorityQueue.size() + queue.size();
    }

    public boolean isQueuesEmpty() {
        return priorityQueue.isEmpty() && queue.isEmpty();
    }

    public void clearQueues() {
        queue.clear();
        priorityQueue.clear();
    }
}