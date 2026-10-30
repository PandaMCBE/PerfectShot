package fr.pandamcbe.perfectshot.managers;

import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.arenas.Arena;
import fr.pandamcbe.mGConstructorAPI.arenas.State;
import fr.pandamcbe.mGConstructorAPI.managers.ArenaManager;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.arenas.PftShotArena;
import fr.pandamcbe.perfectshot.timers.PftShotRound;
import fr.pandamcbe.perfectshot.timers.PftShotStart;
import fr.pandamcbe.perfectshot.utils.GiveKitsUtils;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PftshotManager extends ArenaManager {

    public Set<UUID> invincible = new HashSet<>();

    public void customJoinEvent(Player p) {
        System.out.println("Waiting for new game...");
        PerfectShot perfectShot = PerfectShot.getInstance();
        perfectShot.getScoreboardManager().removePlayers();
        perfectShot.getScoreboardManager().onLogout(p);
        p.setGameMode(GameMode.ADVENTURE);
        p.sendTitle("§e§lEn attente au", getPrefix()+ "..."); //Envoie du titre d'attente au joueur
        p.playSound(p.getLocation(), Sound.BLAZE_BREATH, 1.0f, 1.0f); //Envoie du son au joueur
        p.setHealthScale(20); //Remettre la taille de vie du joueur à 20, la taille par défaut après sa précédente partie si elle a existé
        User user = perfectShot.luckPerms.getPlayerAdapter(Player.class).getUser(p); //Récupération de l'objet User de LuckPerms avec l'objet Joueur
        MGConstructorAPI.getInstance().getGameManager().clearStuffandHealth(p);
        lookForArena(this, perfectShot.createArenas.getArenas(), getPrefix(), p.getUniqueId());

//        for (UUID uuid : waitingRoom.getWaitingRoom()) { //Pour chaque joueur dans la liste
//            Player pls = Bukkit.getPlayer(uuid);
//            pls.sendMessage(getPrefix()+ " §3§l> §r" + user.getCachedData().getMetaData().getPrefix() + " §r" + p.getName() + " §b§la rejoint §7§lla game du "+ getPrefix()+ " !§e§l(" + waitingRoom.getWaitingRoom().size() + "§e§l/8)");
//        } //Envoie du message d'arrivée du nouveau joueur dans la file à tous
    }

    public void startCountDown(Arena mgArena) {
        PftShotArena pftShotArena = (PftShotArena) mgArena;
        PerfectShot perfectShot = PerfectShot.getInstance();
        if(!mgArena.getState().equals(State.PLAYING)) {
            pftShotArena.getPoints().clear();
            PftShotStart pftShotStart = new PftShotStart(pftShotArena, perfectShot);
            pftShotStart.runTaskTimer(MGConstructorAPI.getInstance(), 0, 20);
        } else {
            PftShotRound pftShotRound = new PftShotRound(pftShotArena, perfectShot);
            pftShotRound.runTaskTimer(MGConstructorAPI.getInstance(), 20 * 2, 20);
        }
    }

    public String getPrefix() {
        return "PerfectShot";
    }

    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    public void respawning(Arena arena, Player player) {
//        player.teleport(mgArena.getSpawnpointUUID().get(player.getUniqueId()));
        new GiveKitsUtils(player);
    }

    public void breakOrPlace(Arena arena) {
      placeBrokenBlocks(arena);
    }

    public void playerQuits(Player player) {
        PerfectShot perfectShot = PerfectShot.getInstance();
        PftShotArena pftShotArena = (PftShotArena) getArenaByPlayer(player.getUniqueId(), perfectShot.createArenas.getArenas());
        whenPlayerDisconnects(this, player, pftShotArena, perfectShot.createArenas.getArenas());
//        if(pftShotArena.getState().equals(State.FINISHED)) {
//            new PftShotRound(pftShotArena, perfectShot).stop();
//        }
    }
}