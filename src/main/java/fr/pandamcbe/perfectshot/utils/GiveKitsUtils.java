package fr.pandamcbe.perfectshot.utils;

import fr.pandamcbe.mgapi.serverutilapi.utils.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveKitsUtils {
        //Je ne vais pas commenter ce constructeur, car c'est sans secret
    public GiveKitsUtils(Player p) {
        p.getInventory().setItem(2, new ItemBuilder(Material.BOW).setName("§cOne Hit Bow").setLore("§7OneShot tous tes adversaires", "§7grâce à ton super arc !").addEnchant(Enchantment.ARROW_DAMAGE, 200).addEnchant(Enchantment.ARROW_INFINITE, 2).makeUnbreakable().toItemStack());
        p.getInventory().setItem(5, new ItemBuilder(Material.ARROW, 1).toItemStack());
        ItemStack casque = new ItemBuilder(Material.LEATHER_HELMET).makeUnbreakable().toItemStack();
        ItemStack plastron = new ItemBuilder(Material.LEATHER_CHESTPLATE).makeUnbreakable().toItemStack();
        ItemStack jambieres = new ItemBuilder(Material.LEATHER_LEGGINGS).makeUnbreakable().toItemStack();
        ItemStack bottes = new ItemBuilder(Material.LEATHER_BOOTS).makeUnbreakable().toItemStack();

        p.getInventory().setHelmet(casque);
        p.getInventory().setChestplate(plastron);
        p.getInventory().setLeggings(jambieres);
        p.getInventory().setBoots(bottes);

    }
}