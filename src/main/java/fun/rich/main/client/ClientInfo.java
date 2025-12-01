package fun.rich.main.client;

import fun.rich.utils.client.chat.StringHelper;

import java.io.File;

public record ClientInfo(String clientName, String userName, String role, File clientDir, File filesDir) implements ClientInfoProvider {

    @Override
    public String getFullInfo() {
        return String.format("Welcome! Client: %s Version: %s Branch: %s", clientName, "Baflllik && HZeed", StringHelper.getUserRole());
    }

    @Override
    public File configsDir() {
        return clientDir;
    }
}