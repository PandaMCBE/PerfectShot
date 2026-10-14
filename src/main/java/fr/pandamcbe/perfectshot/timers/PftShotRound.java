package fr.pandamcbe.perfectshot.timers;

import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.managers.CountDownsManager;
import fr.pandamcbe.mGConstructorAPI.managers.GameSessionManager;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.arenas.PftShotArena;
import fr.pandamcbe.perfectshot.managers.PftshotManager;
import fr.pandamcbe.perfectshot.utils.GiveKitsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PftShotRound extends BukkitRunnable {

    private final PftShotArena arena; //Instanciation de la classe Arena avec une variable de type privée et constante
    private int timer = 10; //Initialisation de la variable int timer à 10 de type privée et constante
    private final PerfectShot perfectShot;
    private final PftshotManager arenaManager = PerfectShot.getInstance().getArenaManager(); //Initialisation de la variable arenamanager à l'instance de sa classe du même nom, de type privée et constante
    private final GameSessionManager gameSessionManager = MGConstructorAPI.getInstance().getGameManager();
    public PftShotRound(PftShotArena arena, PerfectShot perfectShot) {
        this.arena = arena; //Initialisation de la variable arena avec celle qui est donnée par le constructeur
        this.perfectShot = perfectShot;
    }

    @Override
    public void run(){
        new CountDownsManager(arena, arenaManager).runNewRound(this, timer, Sound.CLICK, Sound.NOTE_PLING);
        for (UUID uuid : arena.getAlluuids()) { //Pour chaque joueur de la liste d'ensemble de l'arène
            Player p = Bukkit.getPlayer(uuid);
            switch (timer) {
                case 10:
                    gameSessionManager.teleport(arena); //Téléportation de tous les joueurs en jeu à leur spawnpoint
                    p.setHealthScale(6.0); //Taille de la durée de vie mise à 3 cœurs ou 6 demi-coeurs
                    gameSessionManager.clearStuffandHealth(p); //Nettoyage de l'inventaire de chaque joueur
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, true)); //Ajout d'un effet de vitesse de durée infini de niveau 2 sans voir les particules, à chaque joueur
                    p.sendTitle("§7§lLancement dans:", "§c§l10");
                    p.sendMessage("§6§l" + perfectShot.arenaManager.getPrefix() + "§3§l> §b§lProfitez en pour vous disperser sur la map !");
                    p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                    break;
                case 5:
                    p.sendMessage("§6§l" + perfectShot.arenaManager.getPrefix() + " §3§l> §4§l⚠ §b§lLe cooldown arrive à 0\nLes blocks se casseront sous vos pieds faites gaffe !");
                    break;
                case 0:
                    p.sendMessage("§a§l----------------------------\n§a§l----------------------------\n§a§l-----------------\n§b§lBut du jeu : §6§lSnipez tous vos adver\n§6§lsaires avec votre arc du futur !\n§d§n§lSoyez le dernier en vie !\n§a§l-----------------\n§a§l----------------------------\n§a§l----------------------------");
                    new GiveKitsUtils(p); //Donner le kit au joueur
                    p.updateInventory();
                    break;
            }
        }
        if(timer == 0){
            OSGameCycle osGameCycle = new OSGameCycle(arena); //Initialisation de la classe de type Runnable du cycle de la partie à un nouveau constructeur, avec l'objet arena
            osGameCycle.runTaskTimer(perfectShot, 0, 20); //Démarrage sans délai d'un timer qui s'exécute toutes les secondes
        }
        timer--;
    }
}