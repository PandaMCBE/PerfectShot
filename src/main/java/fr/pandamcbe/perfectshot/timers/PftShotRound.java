package fr.pandamcbe.perfectshot.timers;

import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.arenas.State;
import fr.pandamcbe.mGConstructorAPI.managers.CountDownsManager;
import fr.pandamcbe.mGConstructorAPI.managers.GameSessionManager;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.arenas.PftShotArena;
import fr.pandamcbe.perfectshot.managers.PftshotManager;
import fr.pandamcbe.perfectshot.utils.GiveKitsUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
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
                    p.setGameMode(GameMode.ADVENTURE);
                    p.sendTitle("§7§lLancement dans:", "§c§l10");
                    p.sendMessage(perfectShot.arenaManager.getColor()+"§l" + perfectShot.arenaManager.getPrefix() + " §r§3> §b§lProfitez en pour vous disperser sur la map !");
                    p.playSound(p.getLocation(), Sound.CLICK, 1.0f, 1.0f);
                    break;
                case 5:
                    p.sendMessage(perfectShot.arenaManager.getColor()+"§l" + perfectShot.arenaManager.getPrefix() + " §r§3> §4§l⚠ §b§lLe cooldown arrive à 0\nLes blocks se casseront sous vos pieds faites gaffe !");
                    break;
                case 0:
                    p.sendMessage("§a§l----------------------------\n§a§l----------------------------\n§a§l-----------------\n§b§lBut du jeu : §6Snipez tous vos adver\n§6saires avec votre arc du futur !\n§d§nSoyez le dernier en vie !\n§a§l-----------------\n§a§l----------------------------\n§a§l----------------------------");
                    new GiveKitsUtils(p); //Donner le kit au joueur
                    p.updateInventory();
                    break;
            }
        }
        if(timer == 0){
            PftShotGameCycle osGameCycle = new PftShotGameCycle(arena); //Initialisation de la classe de type Runnable du cycle de la partie à un nouveau constructeur, avec l'objet arena
            osGameCycle.runTaskTimer(perfectShot, 0, 20); //Démarrage sans délai d'un timer qui s'exécute toutes les secondes
            startTimer();
            }
        timer--;
    }

    private void startTimer() {
        arena.setBukkitTask(Bukkit.getScheduler().runTaskTimer(PerfectShot.getInstance(), () -> {
            if (arena.getState() == State.PLAYING) {
                for (UUID uuid : arena.getInGamePlayers()) {
                    Block block = getBlocksUnder(Bukkit.getPlayer(uuid));
                    if (block != null) {
                        if (block.getType() == Material.STONE) {
                            //Si ce bloc est de type stone

                            if (arena.getBlocks().putIfAbsent(block.getLocation(), block.getType()) == null) {
                                Bukkit.getScheduler().runTaskLater(perfectShot, () -> {
                                    block.setType(Material.AIR); //On remplace ce bloc par de l'air après un délai de 10 s pour le détruire sans que cela se casse directement sous les pieds du joueur.
                                }, 10);
                            }
                        }
                    }
                }
            } else stop();
        }, 20, 2));
    }

    public void stop() {
        if (arena.getBukkitTask() != null) arena.getBukkitTask().cancel();
    }
    private static final double HALF_WIDTH = 0.3;
    private static final int MAX_DEPTH = 2;

    private Block getBlocksUnder(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        int startY = (int) Math.floor(loc.getY() - 0.1);

        double[][] offsets = {
                {0, 0},                                  // centre en priorité
                {-HALF_WIDTH, -HALF_WIDTH}, {-HALF_WIDTH, HALF_WIDTH},
                {HALF_WIDTH, -HALF_WIDTH},  {HALF_WIDTH, HALF_WIDTH}
        };

        // 2. Centre au-dessus du vide (joueur au bord) : on cherche parmi les coins
        //    et on ne garde que le bloc le plus proche du centre
        for (int i = 0; i <= MAX_DEPTH; i++) {
            int y = startY - i;

            // Centre : si solide à ce niveau, c'est lui et on s'arrête
            Block center = world.getBlockAt((int) Math.floor(loc.getX()), y, (int) Math.floor(loc.getZ()));
            if (center.getType() != Material.AIR) return center;

            // Sinon, coins à CE niveau : on garde le plus proche du joueur
            Block closest = null;
            double closestDist = Double.MAX_VALUE;
            for (int k = 1; k < offsets.length; k++) {
                Block b = world.getBlockAt(
                        (int) Math.floor(loc.getX() + offsets[k][0]), y,
                        (int) Math.floor(loc.getZ() + offsets[k][1]));
                if (b.getType() == Material.AIR) continue;

                // distance entre le joueur et le centre du bloc (en X/Z)
                double distX = (b.getX() + 0.5) - loc.getX();
                double distZ = (b.getZ() + 0.5) - loc.getZ();
                double dist = distX * distX + distZ * distZ;

                if (dist < closestDist) {
                    closestDist = dist;
                    closest = b;
                }
            }
            if(closest != null) return closest;
        }
        return null; // peut être null : rien sous les pieds
    }
}