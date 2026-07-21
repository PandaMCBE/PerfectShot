package fr.pandamcbe.perfectshot.listeners;

import fr.pandamcbe.mgapi.Main;
import fr.pandamcbe.perfectshot.PerfectShot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Game implements Listener {
    PerfectShot perfectShot;

    public Game(PerfectShot perfectShot) {
        this.perfectShot = perfectShot;
    }
    @EventHandler
    void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null); //Annulation du message par défaut
        perfectShot.arenaManager.customJoinEvent(player);

    }

    @EventHandler
    void onQuit(PlayerQuitEvent event){
        Player leaver = event.getPlayer(); //Récupération de l'objet du joueur
        event.setQuitMessage(null); //Annulation du message par défaut
//        Arena arena = MGConstructorAPI.getInstance().getArenaManager().getArenaByPlayer(leaver.getUniqueId(), perfectShot.createArenas.getArenas()); //Récupération de l'arène où se trouve l'objet leaver avec la méthode getArenaByPlayer grâce à l'instance de la classe Main et celle d'ArenaManager enregistrée dedans et son getter
        perfectShot.arenaManager.playerQuits(leaver); //Appel de la méthode whenPlayerGoOffline via l'instance de la classe Main, en y entrant l'objet leaver et l'arène retournée par la recherche de la méthode au-dessus.
        perfectShot.getScoreboardManager().onLogout(leaver);
        Main.getInstance().redisPbSb.saveDatasToCache(leaver.getUniqueId(), PerfectShot.getInstance().mainMap); //Enregistrement des données en mémoire interne du joueur avec la Map principale, en direction de clés Redis
        Main.getInstance().manipulation.deleteAccount(leaver.getUniqueId(), PerfectShot.getInstance().mainMap); //Suppression du compte de mémoire interne de l'objet leaver après l'avoir enregistré dans Redis
    }

}

