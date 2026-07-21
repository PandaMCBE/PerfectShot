package fr.pandamcbe.perfectshot.scoreboards.samaboardsclasses;

import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.arenas.Arena;
import fr.pandamcbe.mgapi.Main;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AllNames;
import fr.pandamcbe.mgapi.serverutilapi.scoreboards.ObjectiveSign;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.arenas.PftShotArena;
import org.bukkit.entity.Player;

public class PersonalScoreboard extends fr.pandamcbe.mgapi.serverutilapi.scoreboards.PersonalScoreboard {


    public PersonalScoreboard(Player player){
        this.player = player;

        uuid = player.getUniqueId();
        objectiveSign = new ObjectiveSign("sidebar", "§e§l§n"+ PerfectShot.getInstance().arenaManager.getPrefix());
        reloadData();
        objectiveSign.addReceiver(player);
    }

    public void reloadData() {
    }
    public void setLines(String ip) {
        Arena arena = MGConstructorAPI.getInstance().arenaManager.getArenaByPlayer(player.getUniqueId(), PerfectShot.getInstance().createArenas.getArenas());
        PftShotArena pftShotArena = (PftShotArena) arena;

        StatsAccount statsAccount = (StatsAccount) Main.getInstance().manipulation.getAccount(PerfectShot.getInstance().mainMap, player.getUniqueId(), AllNames.statsPojo(AllNames.PFTSHOT.getName()));

            if(player != null) {//Scoreboard en attente

                    this.objectiveSign.setLine(1, "§8§m------------");
                    this.objectiveSign.setLine(2, "§7En vies : §f" + pftShotArena.getInGamePlayers().size());
                    this.objectiveSign.setLine(3, "§7Cooldown : §f" + pftShotArena.getTimerCycle());
                    this.objectiveSign.setLine(4, "§7RoundsWon : §f" + pftShotArena.getWonRounds().get(player.getUniqueId()) + "§8/§f" + pftShotArena.getRounds());
                    this.objectiveSign.setLine(5, "§7Coins : §f" + statsAccount.getCoins());
                    this.objectiveSign.setLine(6, "§7Points : §f" + pftShotArena.getPoints().get(player.getUniqueId()));
                    this.objectiveSign.setLine(7, "§7Kills : §f" + (statsAccount.getKills() + (statsAccount.getKillstreaks() * 5)));
                    this.objectiveSign.setLine(8, "§7Arène : §f" + arena.getName());
                    this.objectiveSign.setLine(9, "§8§m------------");
                    this.objectiveSign.setLine(10, ip);
                }
                    this.objectiveSign.updateLines();

    }
    //Cette méthode en-dessous n'a aucun sens puisque la valeur de base du getter du timer est 30,
    // donc il ne peut qu'afficher cette valeur de toute façon.
    // J'ai bien compris que ça permettait de remettre au début si on n'est pas sûr que le getter n'est pas revenu à 30,
    // sauf qu'après, le timer va bien reprendre là où il s'était arrêté précédemment.
    // Alors, dans la gestion de la déconnexion, j'ai réparé ça en réinitialisant le timer à 30 quand un joueur
    // se déconnecte et que ça annule le lancement.
    // C'est toujours mieux que la valeur change automatiquement plutôt que de passer par une méthode annexe comme celle-ci.
    private int getStartingTimer(PftShotArena arena){
        if(arena == null) { //Si la recherche d'arène libre n'a rien retourné -> null
            return 30; //Retourner la valeur initiale du décompte
        }else {
            return arena.getStartingTimer(); //Sinon, afficher le cours
        }
    }

}