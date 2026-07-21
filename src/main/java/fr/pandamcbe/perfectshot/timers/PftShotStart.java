package fr.pandamcbe.perfectshot.timers;

import fr.pandamcbe.mGConstructorAPI.managers.CountDownsManager;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.arenas.PftShotArena;
import fr.pandamcbe.perfectshot.managers.PftshotManager;
import fr.pandamcbe.perfectshot.scoreboards.samaboardsclasses.PersonalScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PftShotStart extends BukkitRunnable {
    private final PftShotArena arena; //Variable Arena de type privée et constante
    private final PerfectShot perfectShot;
    private final PftshotManager arenaManager = PerfectShot.getInstance().arenaManager;

    public PftShotStart(PftShotArena arena, PerfectShot perfectShot) { //Constructeur de la classe, nécessitant l'entrée d'une arène
        this.arena = arena; //L'arène qui est rentrée en paramètre est égale à celle qui est écrite au-dessus
        this.perfectShot = perfectShot;
    }

    @Override //Cette annotation sert à indiquer explicitement qu'il s'agit d'une méthode "parent" de BukkitRunnable
    public void run() {
//        Arena arena = OneShot.getInstance().arenaManager.getArenaByPlayer(player); //si le seul joueur se déco alors on ne peut pas récupérer arena
        for (UUID uuid : arena.getAlluuids()) { //Pour chaque joueur de la liste d'ensemble de l'arène
            Player p = Bukkit.getPlayer(uuid);
            new CountDownsManager(arena).runNewGame(this, perfectShot.waitingRoom.getWaitingRoom(), p);
            if (arena.getStartingTimer() == 0) { //Si le timer atteint 0
                cancel(); //Arrêter le timer
                arena.getPoints().put(p.getUniqueId(), 0); //Entrer de la clé du joueur et de sa valeur à 0 dans la map des points
                arena.getWonRounds().put(p.getUniqueId(), 0); //Entrer de la clé du joueur et de sa valeur à 0 dans la map des manches remportées

                p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f); //Jouer ce son à tous les joueurs
                perfectShot.getScoreboardManager().onLogin(new PersonalScoreboard(p), p);

            }
        }
        if(arena.getStartingTimer() == 0){
            PftShotRound round = new PftShotRound(arena, perfectShot); //Initialisation du runnable des manches de la partie avec le constructeur de OSGameRound et l'arène en cours en paramètre
            round.runTaskTimer(perfectShot, 0, 20); //Exécution du timer chaque seconde, sans délai au démarrage
        }

        arena.setStartingTimer(arena.getStartingTimer() - 1); //Enlever 1 chaque seconde au temps impartit par l'arène
    }
}