package fun.rich;

import antidaunleak.api.annotation.Native;
import antidaunleak.api.UserProfile;
import fun.rich.commands.manager.CommandRepository;
import fun.rich.utils.client.managers.file.exception.FileProcessingException;
import fun.rich.utils.client.chat.ChatMessage;
import fun.rich.utils.client.logs.Logger;
import fun.rich.utils.connection.auracheckft.FTCheckClient;
import fun.rich.utils.connection.irc.IRCManager;
import fun.rich.utils.connection.tps.TPSCalculate;
import fun.rich.utils.display.scissor.ScissorAssist;
import net.fabricmc.api.ModInitializer;
import fun.rich.common.repository.box.BoxESPRepository;
import fun.rich.common.repository.rct.RCTRepository;
import fun.rich.common.repository.way.WayRepository;
import fun.rich.common.discord.DiscordManager;
import fun.rich.utils.client.managers.api.draggable.DraggableRepository;
import fun.rich.utils.client.managers.file.*;
import fun.rich.common.repository.macro.MacroRepository;
import fun.rich.utils.client.managers.event.EventManager;
import fun.rich.features.module.ModuleProvider;
import fun.rich.features.module.ModuleRepository;
import fun.rich.features.module.ModuleSwitcher;
import fun.rich.utils.client.sound.SoundManager;
import fun.rich.display.screens.clickgui.MenuScreen;
import fun.rich.utils.connection.cloud.CloudConfigWebSocketClient;
import fun.rich.main.client.ClientInfo;
import fun.rich.main.client.ClientInfoProvider;
import fun.rich.main.listener.ListenerRepository;
import fun.rich.commands.CommandDispatcher;
import fun.rich.utils.features.aura.striking.StrikerConstructor;
import com.google.common.eventbus.EventBus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import fun.rich.display.screens.mainmenu.altscreen.impl.AccountRepository;
import fun.rich.utils.client.managers.file.impl.AccountFile;
import fun.rich.utils.client.managers.file.impl.AutoBuyConfigFile;
import fun.rich.display.screens.mainmenu.altscreen.impl.Account;
import fun.rich.mixins.client.IMinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import com.mojang.authlib.minecraft.UserApiService;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rich implements ModInitializer {
    @Getter
    static Rich instance;
    EventManager eventManager = new EventManager();
    EventBus eventBus = new EventBus();
    ModuleRepository moduleRepository;
    ModuleSwitcher moduleSwitcher;
    CommandRepository commandRepository;
    CommandDispatcher commandDispatcher;
    BoxESPRepository boxESPRepository = new BoxESPRepository(eventManager);
    MacroRepository macroRepository = new MacroRepository(eventManager);
    WayRepository wayRepository = new WayRepository(eventManager);
    RCTRepository RCTRepository = new RCTRepository(eventManager);
    ModuleProvider moduleProvider;
    DraggableRepository draggableRepository;
    DiscordManager discordManager;
    FileRepository fileRepository;
    FileController fileController;
    ScissorAssist scissorManager = new ScissorAssist();
    ClientInfoProvider clientInfoProvider;
    ListenerRepository listenerRepository;
    StrikerConstructor attackPerpetrator = new StrikerConstructor();
    CloudConfigWebSocketClient cloudConfigClient;
    FTCheckClient ftCheckClient;
    IRCManager ircManager = new IRCManager();
    AccountRepository accountRepository;
    TPSCalculate tpsCalculate;
    boolean initialized;
    boolean showIrcMessages = false;
    ScheduledExecutorService reconnectScheduler;
    boolean reconnecting = false;

    @Override
    @Native(type = Native.Type.VMProtectBeginMutation)
    public void onInitialize() {
        instance = this;
        initClientInfoProvider();
        initModules();
        initDraggable();
        initFileManager();
        initCommands();
        initListeners();
        initDiscordRPC();
        initWebSocketClient();
        initFTCheckClient();
        ircManager.connect();
        startReconnectTask();
        SoundManager.init();
        loadCurrentAccount();

        MenuScreen menuScreen = new MenuScreen();
        menuScreen.initialize();
        initialized = true;

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
        }).start();
    }


    @Native(type = Native.Type.VMProtectBeginMutation)
    private void loadCurrentAccount() {
        if (accountRepository.currentAccount != null && !accountRepository.currentAccount.isEmpty()) {
            Account currentAcc = accountRepository.accountList.stream()
                    .filter(acc -> acc.name.equals(accountRepository.currentAccount))
                    .findFirst()
                    .orElse(null);

            if (currentAcc != null) {
                setSession(currentAcc);
            }
        }
    }

    @Native(type = Native.Type.VMProtectBeginMutation)
    private void setSession(Account account) {
        Session newSession = new Session(account.name, UUID.fromString(account.uuid), "0", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
        IMinecraftClient mca = (IMinecraftClient) MinecraftClient.getInstance();
        mca.setSessionT(newSession);
        MinecraftClient.getInstance().getGameProfile().getProperties().clear();
        UserApiService apiService = UserApiService.OFFLINE;
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManagerT(new SocialInteractionsManager(MinecraftClient.getInstance(), apiService));
        mca.setProfileKeys(ProfileKeys.create(apiService, newSession, MinecraftClient.getInstance().runDirectory.toPath()));
        mca.setAbuseReportContextT(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
    }

    @Native(type = Native.Type.VMProtectBeginUltra)
    private void initWebSocketClient() {
        try {
            cloudConfigClient = new CloudConfigWebSocketClient(new URI("ws://45.155.205.202:8080"));
            cloudConfigClient.connect();
        } catch (Exception e) {
        }
    }

    @Native(type = Native.Type.VMProtectBeginUltra)
    private void initFTCheckClient() {
        try {
            ftCheckClient = new FTCheckClient(new URI("ws://45.155.205.202:6312"));
            ftCheckClient.connect();
        } catch (Exception e) {
        }
    }

    private void initDraggable() {
        draggableRepository = new DraggableRepository();
        draggableRepository.setup();
    }

    private void initModules() {
        moduleRepository = new ModuleRepository();
        moduleRepository.setup();
        moduleProvider = new ModuleProvider(moduleRepository.modules());
        moduleSwitcher = new ModuleSwitcher(moduleRepository.modules(), eventManager);
    }

    private void initCommands() {
        commandRepository = new CommandRepository();
        commandDispatcher = new CommandDispatcher(eventManager);
    }

    private void initDiscordRPC() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            return;
        }
        discordManager = new DiscordManager();
        discordManager.init();
    }

    private void initClientInfoProvider() {
        File clientDirectory = new File(MinecraftClient.getInstance().runDirectory, "\\Rich\\");
        File filesDirectory = new File(clientDirectory, "\\Files\\");
        clientInfoProvider = new ClientInfo("Rich Build 0.3", "Baflllik && HZeed", "Developer", clientDirectory, filesDirectory);
    }

    private void initFileManager() {
        DirectoryCreator directoryCreator = new DirectoryCreator();
        directoryCreator.createDirectories(clientInfoProvider.clientDir(), clientInfoProvider.filesDir());

        File autoBuyDir = new File(clientInfoProvider.clientDir(), "AutoBuy");
        if (!autoBuyDir.exists()) {
            autoBuyDir.mkdirs();
        }

        File customDir = new File(clientInfoProvider.clientDir(), "Custom");
        if (!customDir.exists()) {
            customDir.mkdirs();
        }

        fileRepository = new FileRepository();
        fileRepository.setup(this);
        accountRepository = new AccountRepository();
        fileRepository.getClientFiles().add(new AccountFile(accountRepository));
        fileRepository.getClientFiles().add(new AutoBuyConfigFile());
        fileController = new FileController(fileRepository.getClientFiles(), clientInfoProvider.filesDir());
        try {
            fileController.loadFiles();
        } catch (FileProcessingException e) {
            Logger.error("Failed to load files: " + e.getMessage());
        }
    }

    private void initListeners() {
        listenerRepository = new ListenerRepository();
        listenerRepository.setup();
        tpsCalculate = new TPSCalculate();
    }

    private void startReconnectTask() {
        reconnectScheduler = Executors.newSingleThreadScheduledExecutor();
        reconnectScheduler.scheduleAtFixedRate(() -> {
            if ((ircManager.getClient() == null || !ircManager.getClient().isOpen()) && !reconnecting) {
                reconnecting = true;
                try {
                    ircManager.connect();
                } catch (Exception e) {
                    if (showIrcMessages) {
                        ChatMessage.ircmessageWithRed("Переподключение к серверу IRC не удалось");
                    }
                } finally {
                    reconnecting = false;
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
}