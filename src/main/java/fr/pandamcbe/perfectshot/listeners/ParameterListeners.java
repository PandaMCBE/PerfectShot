package fr.pandamcbe.perfectshot.listeners;

import fr.pandamcbe.commons.ProfilAccount;
import fr.pandamcbe.commons.StatsAccount;
import fr.pandamcbe.mGConstructorAPI.MGConstructorAPI;
import fr.pandamcbe.mGConstructorAPI.arenas.Arena;
import fr.pandamcbe.mGConstructorAPI.managers.GameSessionManager;
import fr.pandamcbe.mgapi.Main;
import fr.pandamcbe.mgapi.core.localmemory.DirtyManager;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AccountType;
import fr.pandamcbe.mgapi.serverutilapi.enums_strings.AllNames;
import fr.pandamcbe.perfectshot.PerfectShot;
import fr.pandamcbe.perfectshot.arenas.PftShotArena;
import fr.pandamcbe.perfectshot.managers.PftshotManager;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ParameterListeners implements Listener {

    final private PftshotManager arenaManager = PerfectShot.getInstance().getArenaManager();
    final private GameSessionManager gameSessionManager = MGConstructorAPI.getInstance().getGameManager();
    private static final double HALF_WIDTH = 0.3;
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PerfectShot perfectShot = PerfectShot.getInstance();
         //Récupération de la classe ArenaManager qui ne craint rien si elle est appelée au démarrage, car elle contient que des méthodes et aucune variables
        Arena arena = arenaManager.getArenaByPlayer(player.getUniqueId(), perfectShot.createArenas.getArenas());
        PftShotArena pftShotArena = (PftShotArena) arena;
        Location location = player.getLocation();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return; //Si le joueur est en mode spectateur, on ne veut pas que les blocs se cassent alors, on annule l'exécution du code
        if (pftShotArena != null) {
            if (pftShotArena.getInGamePlayers().contains(player.getUniqueId())) { //Si le joueur n'est pas éliminé
                if (location.getY() == 20) { //Si la coordonnée de haut de l'endroit où se situe le joueur est élevé à 20
                    gameSessionManager.onDeath(arenaManager, arena, GameMode.ADVENTURE, 10, 3, player); //On déclenche la mort du joueur s'il est hors de l'arène sans encore l'éliminer
                }

                //On récupère le bloc sur lequel se tient le joueur (en soustrayant 1)
                Block block = getBlocksUnder(player);
                if(block != null) {
                    if (block.getType() == Material.STONE) {
                        //Si ce bloc est de type stone
//                        pftShotArena.getBlocks().put(block.getLocation(), block.getType());
                        if (pftShotArena.getBlocks().putIfAbsent(block.getLocation(), block.getType()) == null) {
                            Bukkit.getScheduler().runTaskLater(perfectShot, () -> {
                                block.setType(Material.AIR); //On remplace ce bloc par de l'air après un délai de 10 s pour le détruire sans que cela se casse directement sous les pieds du joueur.
                            }, 5);
                        }
                    }
                }
            }
        }
    }
    private Block getBlocksUnder(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        double y = loc.getY() - 0.1; // juste sous les pieds

        Block center = new Location(world, loc.getX(), y, loc.getZ()).getBlock();
        if (center.getType() != Material.AIR) {
            return center;
        }

        // 2. Centre au-dessus du vide (joueur au bord) : on cherche parmi les coins
        //    et on ne garde que le bloc le plus proche du centre
        Block closest = null;
        double closestDist = Double.MAX_VALUE;

        for (double dx : new double[]{-HALF_WIDTH, HALF_WIDTH}) {
            for (double dz : new double[]{-HALF_WIDTH, HALF_WIDTH}) {
                Block b = new Location(world, loc.getX() + dx, y, loc.getZ() + dz).getBlock();
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
        }
        return closest; // peut être null : rien sous les pieds
    }
    @EventHandler
    void onFoodLevelChange(FoodLevelChangeEvent event) {
        //Je tiens à préciser que l'objet Player est tiré de la classe HumanEntity, tirée de HumanEntity, tirée d'Entity, donc normalement, c'est effectivement compatible
        if (event.getEntity() instanceof Player) { //Si l'entité sur laquelle il y a une tentation de baisse de nourriture est un joueur
            Player player = (Player) event.getEntity();

            // Annule le changement de niveau de faim du joueur
            event.setCancelled(true);

            // Définit le niveau de faim du joueur à 20 (maximum)
            player.setFoodLevel(20);
        }
    }

    @EventHandler
    void onBlockUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (p.getGameMode() != GameMode.CREATIVE) { //Si le joueur n'est pas en créatif
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) { //Si le joueur fait un clique droit ou clique gauche sur un objet
                event.setCancelled(true); //Annuler l'interaction -> C'est une sécurité pour que les joueurs ne cassent pas la déco
            }
        }
    }

    //on annule le drop des items
    @EventHandler
    void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    void onDamage(EntityDamageByEntityEvent event){

        if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) { //Si la victime et celui qui a causé les dégats sur cet individu sont des joueurs

            Player victim = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            if(damager == victim) { //Si le joueur s'est tiré une flèche à lui-même
                return; //Arrêter l'exécution du code de l'event
            }
            Arena victimArena = arenaManager.getArenaByPlayer(victim.getUniqueId(), PerfectShot.getInstance().createArenas.getArenas());

            if (victimArena == null || !victimArena.getAlluuids().contains(damager.getUniqueId())) { //Si l'arène est null et si la liste des joueurs non-éliminés ne contient pas le damager
                event.setCancelled(true); //On annule le dégat
            }

        }

    }

    @EventHandler
    void onDeath(PlayerDeathEvent e) { //Je n'ai pas commenté tous les messages et ajouts de stats basiques
        Player victim = e.getEntity();
        Player Killer = victim.getKiller();
        PerfectShot perfectShot = PerfectShot.getInstance();
        PftshotManager pftshotManager = PerfectShot.getInstance().getArenaManager(); //Récupération de la classe ArenaManager qui ne craint rien si elle est appelée au démarrage, car elle contient que des méthodes et aucune variables
        DirtyManager dirtyManager = new DirtyManager();
        victim.spigot().respawn();

        if (victim.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) victim.getLastDamageCause();
            Entity damager = damageEvent.getDamager();

            // If the damager is a projectile (arrow), get the shooter
            if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof Player) {
                    Killer = (Player) projectile.getShooter();
                }
            }

            if (Killer != null) {
                Arena arena = arenaManager.getArenaByPlayer(Killer.getUniqueId(), perfectShot.createArenas.getArenas()); //Récupération de l'arène où s'est déroulé l'évènement
                PftShotArena pftShotArena = (PftShotArena) arena;
                User user1 = perfectShot.luckPerms.getPlayerAdapter(Player.class).getUser(victim);
                String serverName = AllNames.PFTSHOT.getName();

                User user2 = perfectShot.luckPerms.getPlayerAdapter(Player.class).getUser(Killer);
                if (victim != Killer) { //Vérification si c'est pas le killer qui s'est envoyé une flèche à lui-même
                    StatsAccount statsAccount = (StatsAccount) Main.getInstance().manipulation.getStatsAccount(perfectShot.mainMap, Killer.getUniqueId(), AccountType.statsKey(serverName));
                    ProfilAccount profilAccount = (ProfilAccount) Main.getInstance().manipulation.getProfilAccount(perfectShot.mainMap, Killer.getUniqueId(), AccountType.PROFIL.getKeyPrefix());
                    profilAccount.setCoins(profilAccount.getCoins() + 5);
                    statsAccount.setKills(statsAccount.getKills() + 1);
                    statsAccount.setElos(statsAccount.getElos() + 1);
                    dirtyManager.markDirty(Killer.getUniqueId(), AccountType.statsKey(serverName), statsAccount);
                    dirtyManager.markDirty(Killer.getUniqueId(), AccountType.PROFIL.getKeyPrefix(), profilAccount);

                    pftShotArena.getPoints().put(Killer.getUniqueId(), pftShotArena.getPoints().get(Killer.getUniqueId()) + 10); //On lui rajoute des points
                    Killer.sendMessage("§7§l+ §b§l1 kill !");
                    Killer.sendMessage("§7§l+ §6§l5 coins !");
                    Killer.sendMessage("§b§l" + pftshotManager.getPrefix() + " §3> §r" + user2.getCachedData().getMetaData().getPrefix() + "§r " + Killer.getName() + " §7a\n térrassé §r" + user1.getCachedData().getMetaData().getPrefix() + "§r" + victim.getName());

                    Main.getInstance().gameMechanics.applyKillStreak(statsAccount, Killer, pftshotManager.getColor()+pftshotManager.getPrefix()); //Application du système de killstreak pour le killer
                    StatsAccount statsAccount2 = (StatsAccount) Main.getInstance().manipulation.getStatsAccount(perfectShot.mainMap, victim.getUniqueId(), AccountType.statsKey(serverName));
                    if (statsAccount2.getKillstreaks() > 1) { //Si la victime avait plus d'un kill, donc un killstreak
                        statsAccount2.setKillstreaks(0); //On lui réinitialise son killstreak car il est nul
                        victim.sendMessage("§c§lVous avez perdu vos KillStreaks!");
                    }
                    dirtyManager.markDirty(victim.getUniqueId(), AccountType.statsKey(serverName), statsAccount2);

                    if (victim.getHealthScale() > 1.0) {
                        gameSessionManager.onDeath(arenaManager, arena, GameMode.ADVENTURE,10, 3, victim); //Gestion de la mort de la victime, pas encore l'élimination
                        victim.setHealthScale(victim.getHealthScale() - 1.0);
                    }else {
                        pftShotArena.eliminate(victim);
                    }
                }
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, true)); //Ajout d'un effet de vitesse de durée infini de niveau 2 sans voir les particules, à chaque joueur
            }
        }
    }
}