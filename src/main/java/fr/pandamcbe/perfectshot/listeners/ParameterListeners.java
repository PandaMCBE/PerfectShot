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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
                    gameSessionManager.onDeath(arenaManager, arena, GameMode.ADVENTURE,10, 3, player); //On déclenche la mort du joueur s'il est hors de l'arène sans encore l'éliminer
                }
                Block block = location.subtract(0, 1, 0).getBlock(); //On récupère le bloc sur lequel se tient le joueur (en soustrayant 1)
                if (block.getType().equals(Material.STONE)) { //Si ce bloc est de type stone
                    pftShotArena.getBlocks().put(block.getLocation(), block.getType());

                    Bukkit.getScheduler().runTaskLater(perfectShot, () -> {
                        block.setType(Material.AIR); //On remplace ce bloc par de l'air après un délai de 10 s pour le détruire sans que cela se casse directement sous les pieds du joueur.

                    }, 10);
                }
            }
        }
    }

    private Block getBlock(Location location) {
        Block block = location.subtract(0, 1, 0).getBlock();
        for (int xOffset = -1; xOffset <= 1; xOffset++) { //Si offSet qui est égale à -1 est égale ou inférieure à 1, on lui rajoute 1 -> Cette boucle est donc exécutée deux fois pour obtenir 2 à la fin
//            for (int yOffset = -1; yOffset <= 1; yOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                block.getLocation().subtract(xOffset, 0, zOffset);
            }
//            }
        }

        return block;
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

                    Main.getInstance().gameMechanics.applyKillStreak(statsAccount, Killer, pftshotManager.getPrefix()); //Application du système de killstreak pour le killer
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