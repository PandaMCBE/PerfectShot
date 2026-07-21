package fr.pandamcbe.perfectshot.arenas;

import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.arenas.Arena;
import fr.pandamcbe.mGConstructorAPI.arenas.MGArena;
import fr.pandamcbe.mGConstructorAPI.managers.GameSessionManager;
import fr.pandamcbe.mgapi.Main;
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

    public PftShotArena(Location middlespawn, String name, Integer minimumPlayers, Integer maxPlayers, Integer totalTeams){
        this.middlespawn = middlespawn;
        this.name = name;
        this.minimumPlayers = minimumPlayers;
        this.maxPlayersAllowed = maxPlayers;
        this.totalTeams = totalTeams;
        this.spawns = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.locByTeam = new HashMap<>();
        this.points = new HashMap<>();
        restart();
    }

    private final String serverName = AllNames.PFTSHOT.getName();
    private final PerfectShot perfectShot = PerfectShot.getInstance();
    private final PftshotManager arenaManager = PerfectShot.getInstance().arenaManager;
    private final GameSessionManager gameSessionManager = MGConstructorAPI.getInstance().getGameManager();

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
        StatsAccount statsAccount = (StatsAccount) Main.getInstance().manipulation.getAccount(PerfectShot.getInstance().mainMap, victim.getUniqueId(), AllNames.statsPojo(serverName));
        statsAccount.setElos(statsAccount.getElos() - 1);
        statsAccount.setCoins(statsAccount.getCoins() - 2);
        victim.sendMessage("§c§l- §6§l2 coins");
        victim.sendMessage("§c§l- §d§l1 elo");
        victim.sendMessage("§cVous êtes mort ! Bouh le nullos ! §eVous devez attendre le prochain round !");
        gameSessionManager.eliminateCore(victim, perfectShot.createArenas.getArenas());
    }


    public void nextMGRound(Arena arena, Object o) {
        Player player = (Player) o;
        gameSessionManager.nextRound(perfectShot.createArenas.getArenas(), player, perfectShot.mainMap, 10, 0);
        OnChangingRound((PftShotArena)arena, player);
    }

    public void customWinEvent(Object pWinner) {
        Player winner = (Player) pWinner;
        StatsAccount statsAccount = (StatsAccount) Main.getInstance().manipulation.getAccount(PerfectShot.getInstance().mainMap, winner.getUniqueId(), AllNames.statsPojo(serverName));
        statsAccount.setCoins(15);
        statsAccount.setWins(1);
//        Main.getInstance().updatePlayerData.updateStats(winner.getUniqueId(), Main.getInstance().table, statsAccount);
        User user = PerfectShot.getInstance().luckPerms.getPlayerAdapter(Player.class).getUser(winner);
        winner.sendMessage("§7§l+ §b§l1 win !");
        winner.sendMessage("§7§l+ §6§l15 coins !");
        Bukkit.broadcastMessage("§e§l" + arenaManager.getPrefix() + " > §r" + user.getCachedData().getMetaData().getPrefix() + " §r" + winner.getName() + " §b§lRemporte le oneshot ! §6§lGG");
        gameSessionManager.checkWinCore(winner, perfectShot.createArenas.getArenas());
    }

}