package fr.pandamcbe.perfectshot.timers;

import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.managers.GameSessionManager;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.arenas.PftShotArena;
import fr.pandamcbe.perfectshot.managers.PftshotManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PftShotGameCycle extends BukkitRunnable {

    private final PftShotArena arena; //Instanciation de la classe Arena avec une variable de type privée et constante
    private final PftshotManager pftshotManager = PerfectShot.getInstance().getArenaManager(); //Initialisation de la variable arenamanager à l'instance de sa classe du même nom, de type privée et constante
    private final GameSessionManager gameSessionManager = MGConstructorAPI.getInstance().getGameManager();
    private final PerfectShot perfectShot = PerfectShot.getInstance();
    public PftShotGameCycle(PftShotArena arena) {
        this.arena = arena; //Initialisation de la variable arena avec celle qui est donnée par le constructeur
    }

    @Override
    public void run() {
        if (arena == null || arena.getAlluuids().size() <= 1) //Si l'objet Arena est null ou sa liste d'ensemble de joueurs a une taille inférieure ou égale à 1
        {
            cancel(); //Annuler le timer
            return;// Arrêter l'exécution du code
        }

        for (UUID uuid : arena.getInGamePlayers()) { //Pour chaque joueur de la liste de joueurs qui ne sont pas éliminés de l'arène
            Player p = Bukkit.getPlayer(uuid);
            if (!p.isOnline()) { //si le joueur n'est pas en ligne
                pftshotManager.whenPlayerDisconnects(pftshotManager, p, arena, perfectShot.createArenas.getArenas()); //Application de la méthode whenPlayerGoOffline avec l'objet du joueur et l'arène
            }
        }

        if (arena.getTimerCycle() == 60 * 1) { //Si le timer du cycle de l'arène est égale à 180
            cancel(); //Annuler le timer
            new PftShotRound(arena, perfectShot).stop();
            pftshotManager.placeBrokenBlocks(arena); //Reconstruire l'arène
            //Pour chaque joueur de la liste d'ensemble de joueurs de l'arène


            arena.setRounds(arena.getRounds() + 1); //Ajouter 1 à la variable du nombre de manches essuyées
            if (arena.getPoints().get((UUID) gameSessionManager.getHighestPointsPlayer(arena.getPoints(), true)) > 0) {//Si le joueur qui a le nombre de points le plus haut de la map des points en a un nombre supérieure à 0
//                    gameSessionManager.ifLastRound(pftshotManager, arena);
                    for(UUID uuid : arena.getPoints().keySet()) {
                        if(arena.getPoints().get(uuid).equals(arena.getPoints().get((UUID) gameSessionManager.getHighestPointsPlayer(arena.getPoints(), true)))) {
                            arena.nextRound(arena, Bukkit.getPlayer(uuid));
                        }
                    }
                }
                if(arena.getRounds() <= (arena.getMaxRounds() - 1)) {
                    pftshotManager.startCountDown(arena);
                }
                for(UUID uuid : arena.getAlluuids()) {
                    Player p = Bukkit.getPlayer(uuid);
                    arena.getPoints().put(p.getUniqueId(), 0); //Réinitialisation du nombre de points des joueurs dans la map
                    arena.getInGamePlayers().remove(p.getUniqueId()); //Retirer tous les joueurs de la liste des joueurs qui ne sont pas éliminés pour recommencer
                }
            arena.setTimerCycle(0); //Remise à 0 du timer de l'arène
        } else {
            arena.setTimerCycle(arena.getTimerCycle() + 1); //Ajout de 1 à la variable du timer de l'arène chaque seconde
        }
    }
}