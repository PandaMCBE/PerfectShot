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

public class OSGameCycle extends BukkitRunnable {

    private final PftShotArena arena; //Instanciation de la classe Arena avec une variable de type privée et constante
    private final PftshotManager pftshotManager = PerfectShot.getInstance().getArenaManager(); //Initialisation de la variable arenamanager à l'instance de sa classe du même nom, de type privée et constante
    private final GameSessionManager gameSessionManager = MGConstructorAPI.getInstance().getGameManager();
    private final List<Map.Entry<UUID, Integer>> sorting; //Création d'une liste contenant les entrées d'une map avec un Joueur en clé et Integer en valeur, de type privée et constante
    private final PerfectShot perfectShot = PerfectShot.getInstance();
    public OSGameCycle(PftShotArena arena) {
        this.arena = arena; //Initialisation de la variable arena avec celle qui est donnée par le constructeur
        this.sorting = new ArrayList<>(arena.getPoints().entrySet()); //Initialisation de la liste à une nouvelle ArrayList contenant le Set des entrées de la map des points
    }

    private List<Map.Entry<UUID, Integer>> getSorting(){ //getter de la liste
        return sorting;
    }

    private UUID getHighestPointsPlayer(){
        getSorting().sort(Map.Entry.comparingByValue(Collections.reverseOrder())); //Trier le getter de la liste d'entrées des joueurs par points dans l'ordre décroissant

        for (Map.Entry<UUID, Integer> entry : sorting) { //Pour chaque entrée de la map des points
                return entry.getKey(); //retourner la clé de l'entrée et arrêter la boucle, donc concrètement la première clé, qui est en plus le joueur qui a le plus de points
        }
        return null; //retourner null par défaut, s'il n'y a aucun joueur
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
                pftshotManager.whenPlayerDisconnects(p, arena, perfectShot.createArenas.getArenas()); //Application de la méthode whenPlayerGoOffline avec l'objet du joueur et l'arène
            }
        }

        if (arena.getTimerCycle() == 60 * 3) { //Si le timer du cycle de l'arène est égale à 180
            cancel(); //Annuler le timer
            pftshotManager.arenaManager.placeBrokenBlocks(arena); //Reconstruire l'arène
            //Pour chaque joueur de la liste d'ensemble de joueurs de l'arène

                if (arena.getPoints().get(getHighestPointsPlayer()) > 0) {//Si le joueur qui a le nombre de points le plus haut de la map des points en a un nombre supérieure à 0
                    gameSessionManager.ifLastRoundCore(arena);
                    for(UUID uuid : arena.getPoints().keySet()) {
                        if(arena.getPoints().get(uuid).equals(arena.getPoints().get(getHighestPointsPlayer()))) {
                            arena.nextMGRound(arena, Bukkit.getPlayer(uuid));
                        }
                    }
                }
                pftshotManager.startCountDown(arena);
                arena.setRounds(arena.getRounds() + 1); //Ajouter 1 à la variable du nombre de manches essuyées
                for(UUID uuid : arena.getAlluuids()) {
                    Player p = Bukkit.getPlayer(uuid);
                    arena.getPoints().put(p.getUniqueId(), 0); //Réinitialisation du nombre de points des joueurs dans la map
                    arena.getInGamePlayers().remove(p.getUniqueId()); //Retirer tous les joueurs de la liste des joueurs qui ne sont pas éliminés pour recommencer
                }
            arena.setTimerCycle(0); //Remise à 0 du timer de l'arène
        }

        arena.setTimerCycle(arena.getTimerCycle() + 1); //Ajout de 1 à la variable du timer de l'arène chaque seconde
    }
}