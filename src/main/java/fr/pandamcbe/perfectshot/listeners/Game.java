package fr.pandamcbe.perfectshot.listeners;

import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.mgapi.Main;
import fr.pandamcbe.mgapi.core.localmemory.DirtyManager;
import fr.pandamcbe.mgapi.core.localmemory.MemAccountsManipulation;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AccountType;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AllNames;
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
        Player p = event.getPlayer();
        event.setJoinMessage(null); //Annulation du message par défaut
        MemAccountsManipulation memAccountsManipulation = Main.getInstance().manipulation;
        memAccountsManipulation.getShopAccounts(Main.getInstance().localCacheManager, p.getUniqueId(), AccountType.CJFFAKITS.getKeyPrefix(), AccountType.COSMEWORN.getKeyPrefix());
        memAccountsManipulation.getProfilAccount(Main.getInstance().localCacheManager, p.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
        StatsAccount statsAccount = (StatsAccount) memAccountsManipulation.getStatsAccount(Main.getInstance().localCacheManager, p.getUniqueId(), AccountType.statsKey(AllNames.CJFFA.getName()));
        statsAccount.setKills(0);
        statsAccount.setKillstreaks(0);

        perfectShot.arenaManager.customJoinEvent(p);

    }

    @EventHandler
    void onQuit(PlayerQuitEvent event){
        Player leaver = event.getPlayer(); //Récupération de l'objet du joueur
        event.setQuitMessage(null); //Annulation du message par défaut
//        Arena arena = MGConstructorAPI.getInstance().getArenaManager().getArenaByPlayer(leaver.getUniqueId(), perfectShot.createArenas.getArenas()); //Récupération de l'arène où se trouve l'objet leaver avec la méthode getArenaByPlayer grâce à l'instance de la classe Main et celle d'ArenaManager enregistrée dedans et son getter
        perfectShot.getScoreboardManager().onLogout(leaver);
        perfectShot.arenaManager.playerQuits(leaver); //Appel de la méthode whenPlayerGoOffline via l'instance de la classe Main, en y entrant l'objet leaver et l'arène retournée par la recherche de la méthode au-dessus.
        new DirtyManager().forceSave(PerfectShot.getInstance().mainMap, leaver.getUniqueId());
        Main.getInstance().manipulation.deleteAccount(leaver.getUniqueId(), PerfectShot.getInstance().mainMap); //Suppression du compte de mémoire interne de l'objet leaver après l'avoir enregistré dans Redis
    }

}

