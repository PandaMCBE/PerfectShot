package fr.pandamcbe.perfectshot.arenas;

import fr.pandamcbe.commons.ProfilAccount;
import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.arenas.Arena;
import fr.pandamcbe.mGConstructorAPI.arenas.MGArena;
import fr.pandamcbe.mGConstructorAPI.managers.GameSessionManager;
import fr.pandamcbe.mGConstructorAPI.managers.WaitingRoom;
import fr.pandamcbe.mgapi.Main;
import fr.pandamcbe.mgapi.core.localmemory.DirtyManager;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AccountType;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AllNames;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.managers.PftShotManager;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PftShotArena extends MGArena {
    private Map<UUID, Integer> points;
    private BukkitTask bukkitTask;

    public Map<UUID, Integer> getPoints() {
        return points;
    }

    public void setPoints(Map<UUID, Integer> points) {
        this.points = points;
    }

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public void setBukkitTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public PftShotArena(Location middlespawn, String name, String mode, Integer maxRounds, Integer minimumPlayers, Integer maxPlayers){
        this.middlespawn = middlespawn;
        this.name = name;
        this.maxRounds = maxRounds;
        this.minimumPlayers = minimumPlayers;
        this.maxPlayersAllowed = maxPlayers;
        this.spawns = new ArrayList<>();
        this.waitingRoom = new WaitingRoom();
        this.points = new HashMap<>();
        this.mode = mode;
        this.bukkitTask = null;
        restart();
    }

    private final String serverName = AllNames.PFTSHOT.getName();
    private final PerfectShot perfectShot = PerfectShot.getInstance();
    private final PftShotManager arenaManager = PerfectShot.getInstance().arenaManager;
    private final GameSessionManager gameSessionManager = MGConstructorAPI.getInstance().getGameManager();
    private final DirtyManager dirtyManager = new DirtyManager();

    public void onSwitchingRound(Arena arena, Player winner){
        User user = PerfectShot.getInstance().luckPerms.getPlayerAdapter(Player.class).getUser(winner);
        for (UUID uuids : arena.getAlluuids()) {
            Player pls = Bukkit.getPlayer(uuids);
            pls.sendMessage(arenaManager.getColor() +"§l"+ arenaManager.getPrefix() + " > §r" + user.getCachedData().getMetaData().getPrefix() + " " + winner.getName() + " §3Remporte la manche !");
        }
    }

    public void eliminate(Player victim) {
        StatsAccount statsAccount = (StatsAccount) Main.getInstance().manipulation.getStatsAccount(Main.getInstance().localCacheManager, victim.getUniqueId(), AccountType.statsKey(serverName));
        ProfilAccount profilAccount = (ProfilAccount) Main.getInstance().manipulation.getProfilAccount(Main.getInstance().localCacheManager, victim.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
        statsAccount.setElos(statsAccount.getElos() - 1);
        profilAccount.setCoins(profilAccount.getCoins() - 2);
        dirtyManager.markDirty(victim.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
        dirtyManager.markDirty(victim.getUniqueId(), AccountType.PROFIL.getKeyPrefix(), profilAccount);
        victim.sendMessage("§c- §62 coins");
        victim.sendMessage("§c- §d1 elo");
        victim.sendMessage("§cVous êtes mort ! Bouh le nullos ! §eVous devez attendre le prochain round !");
        gameSessionManager.eliminate(victim, perfectShot.createArenas.getArenas());
    }

    public void customWinEvent(Object pWinner) {
        Player winner = (Player) pWinner;
        StatsAccount statsAccount = (StatsAccount) Main.getInstance().manipulation.getStatsAccount(Main.getInstance().localCacheManager, winner.getUniqueId(), AccountType.statsKey(serverName));
        ProfilAccount profilAccount = (ProfilAccount) Main.getInstance().manipulation.getProfilAccount(Main.getInstance().localCacheManager, winner.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
        profilAccount.setCoins(profilAccount.getCoins() + 15);
        statsAccount.setWins(statsAccount.getWins() + 1);
        dirtyManager.markDirty(winner.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
        dirtyManager.markDirty(winner.getUniqueId(), AccountType.PROFIL.getKeyPrefix(), profilAccount);
        User user = PerfectShot.getInstance().luckPerms.getPlayerAdapter(Player.class).getUser(winner);
        winner.sendMessage("§7+ §b1 win !");
        winner.sendMessage("§7+ §615 coins !");
        for(UUID uuid : arenaManager.getArenaByPlayer(winner.getUniqueId(), perfectShot.createArenas.getArenas()).getAlluuids()) {
            Bukkit.getPlayer(uuid).sendMessage(arenaManager.getColor()+"§l"+arenaManager.getPrefix() + " > §r" + user.getCachedData().getMetaData().getPrefix() + " §r" + winner.getName() + " §b§lRemporte le oneshot ! §6§lGG");
        }
        gameSessionManager.checkWinCore(arenaManager, winner, perfectShot.createArenas.getArenas());
    }

}