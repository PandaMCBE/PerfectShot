package fr.pandamcbe.perfectshot;

import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.managers.WaitingRoom;
import fr.pandamcbe.mgapi.serverutilapi.scoreboards.ScoreboardManager;
import fr.pandamcbe.perfectshot.arenas.CreateArenas;
import fr.pandamcbe.perfectshot.managers.PftshotManager;
import fr.pandamcbe.perfectshot.commands.Commands;
import fr.pandamcbe.perfectshot.commands.StatCommand;
import fr.pandamcbe.perfectshot.listeners.Game;
import fr.pandamcbe.perfectshot.listeners.ParameterListeners;
import fr.pandamcbe.perfectshot.rediscommunication.RPbSbUtils;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public final class PerfectShot extends JavaPlugin {

    private static PerfectShot INSTANCE; //Instanciation de la classe Main en privé et accessible par tout le reste du projet
    public LuckPerms luckPerms;
    public PftshotManager arenaManager = new PftshotManager();
    public CreateArenas createArenas;
    private ScheduledExecutorService executorMonoThread;
    private ScheduledExecutorService scheduledExecutorService;
    private ScoreboardManager scoreboardManager;
    public ScheduledExecutorService getExecutorMonoThread() {
        return executorMonoThread;
    }
    public WaitingRoom waitingRoom = MGConstructorAPI.getInstance().getWaitingRoomManager();
    public Map<UUID, Map<String, Object>> mainMap;
    public RPbSbUtils subToChannels;

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }
    @Override
    public void onEnable() {
        INSTANCE = this; //Initialisation de l'instance à elle-même au démarrage

        System.out.println("§2[DEBUG] OneShot chargé !");
        getCommand("report").setExecutor(new Commands());
        getCommand("loadArenas").setExecutor(new Commands());
        getCommand("stats").setExecutor(new StatCommand());

        getServer().getPluginManager().registerEvents(new Game(this), this);
        getServer().getPluginManager().registerEvents(new ParameterListeners(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getServer().setIdleTimeout(180);
        getServer().setDefaultGameMode(GameMode.ADVENTURE); //Le mode de jeu par défaut est celui d'aventure

        this.scheduledExecutorService = Executors.newScheduledThreadPool(16);
        this.executorMonoThread = Executors.newScheduledThreadPool(1);
        this.scoreboardManager = new ScoreboardManager();

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
        }
        createArenas = new CreateArenas(); //Chargement des arènes pré-crées, au démarrage
        mainMap = new HashMap<>();
        waitingRoom.setLocation(new Location(Bukkit.getWorld("world"), -6.626, 52, -28.721, -0.0f, -0.0f));
        subToChannels = new RPbSbUtils();

    }

    @Override
    public void onDisable() {
        this.scoreboardManager.onDisable(); //Désactivation du scoreboard
        subToChannels.sendAccountsToProxy(this); //Envoie des données JVM au proxy qui les enverra à Redis.
        // Plugin shutdown logic
    }

    public static PerfectShot getInstance() {
        return INSTANCE;
    }
    public ScoreboardManager getScoreboardManager() {
    return scoreboardManager;
}

    public PftshotManager getArenaManager() {
        return arenaManager;
    }


}
