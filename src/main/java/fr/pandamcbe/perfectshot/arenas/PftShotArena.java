package fr.pandamcbe.perfectshot.arenas;

import fr.pandamcbe.commons.ProfilAccount;
import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.arenas.MGArena;
import fr.pandamcbe.mGConstructorAPI.managers.GameSessionManager;
import fr.pandamcbe.mGConstructorAPI.managers.WaitingRoom;
import fr.pandamcbe.mgapi.Main;
import fr.pandamcbe.mgapi.core.localmemory.DirtyManager;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AccountType;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AllNames;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.managers.PftshotManager;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PftShotArena extends MGArena {
    private Map<UUID, Integer> points;

    public Map<UUID, Integer> getPoints() {
        return points;
    }

    public void setPoints(Map<UUID, Integer> points) {
        this.points = points;
    }

    public PftShotArena(Location middlespawn, String name, String mode, Integer minimumPlayers, Integer maxPlayers){
        this.middlespawn = middlespawn;
        this.name = name;
        this.minimumPlayers = minimumPlayers;
        this.maxPlayersAllowed = maxPlayers;
        this.spawns = new ArrayList<>();
        this.waitingRoom = new WaitingRoom();
        this.points = new HashMap<>();
        this.mode = mode;
        restart();
    }

    private final String serverName = AllNames.PFTSHOT.getName();
    private final PerfectShot perfectShot = PerfectShot.getInstance();
    private final PftshotManager arenaManager = PerfectShot.getInstance().arenaManager;
    private final GameSessionManager gameSessionManager = MGConstructorAPI.getInstance().getGameManager();
    private final DirtyManager dirtyManager = new DirtyManager();

    public void OnChangingRound(PftShotArena arena, Player winner){
        winner.sendMessage("§7§l+ §d§l1 elo !");
        winner.sendMessage("§7§l+ §6§l10 coins !");
        User user = PerfectShot.getInstance().luckPerms.getPlayerAdapter(Player.class).getUser(winner);
        for (UUID uuids : arena.getAlluuids()) {
            Player pls = Bukkit.getPlayer(uuids);
            pls.sendMessage("§e§l" + arenaManager.getPrefix() + " > §l" + user.getCachedData().getMetaData().getPrefix() + " §l§r" + winner.getName() + " §3§lRemporte la manche !");
            pls.sendTitle("§cNext Round!", "");
        }
    }

    public void eliminate(Player victim) {
        StatsAccount statsAccount = (StatsAccount) Main.getInstance().manipulation.getStatsAccount(PerfectShot.getInstance().mainMap, victim.getUniqueId(), AccountType.statsKey(serverName));
        ProfilAccount profilAccount = (ProfilAccount) Main.getInstance().manipulation.getProfilAccount(PerfectShot.getInstance().mainMap, victim.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
        statsAccount.setElos(statsAccount.getElos() - 1);
        profilAccount.setCoins(profilAccount.getCoins() - 2);
        dirtyManager.markDirty(victim.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
        dirtyManager.markDirty(victim.getUniqueId(), AccountType.PROFIL.getKeyPrefix(), profilAccount);
        victim.sendMessage("§c§l- §6§l2 coins");
        victim.sendMessage("§c§l- §d§l1 elo");
        victim.sendMessage("§cVous êtes mort ! Bouh le nullos ! §eVous devez attendre le prochain round !");
        gameSessionManager.eliminate(victim, perfectShot.createArenas.getArenas());
    }

    public void customWinEvent(Object pWinner) {
        Player winner = (Player) pWinner;
        StatsAccount statsAccount = (StatsAccount) Main.getInstance().manipulation.getStatsAccount(PerfectShot.getInstance().mainMap, winner.getUniqueId(), AccountType.statsKey(serverName));
        ProfilAccount profilAccount = (ProfilAccount) Main.getInstance().manipulation.getProfilAccount(PerfectShot.getInstance().mainMap, winner.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
        profilAccount.setCoins(profilAccount.getCoins() + 15);
        statsAccount.setWins(statsAccount.getWins() + 1);
        dirtyManager.markDirty(winner.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
        dirtyManager.markDirty(winner.getUniqueId(), AccountType.PROFIL.getKeyPrefix(), profilAccount);
        User user = PerfectShot.getInstance().luckPerms.getPlayerAdapter(Player.class).getUser(winner);
        winner.sendMessage("§7§l+ §b§l1 win !");
        winner.sendMessage("§7§l+ §6§l15 coins !");
        for(UUID uuid : arenaManager.getArenaByPlayer(winner.getUniqueId(), perfectShot.createArenas.getArenas()).getAlluuids()) {
            Bukkit.getPlayer(uuid).sendMessage("§e§l" + arenaManager.getPrefix() + " > §r" + user.getCachedData().getMetaData().getPrefix() + " §r" + winner.getName() + " §b§lRemporte le oneshot ! §6§lGG");
        }
        gameSessionManager.checkWinCore(arenaManager, winner, perfectShot.createArenas.getArenas());
    }

    @Override
    public void nextRound(MGArena arena, Object o) {
        Player player = (Player) o;
        gameSessionManager.nextRound(arenaManager, perfectShot.createArenas.getArenas(), player, perfectShot.mainMap, 10, 0);
        OnChangingRound((PftShotArena)arena, player);
    }
}