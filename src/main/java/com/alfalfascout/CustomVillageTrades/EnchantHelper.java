package com.alfalfascout.CustomVillageTrades;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.enchantments.*;

public class EnchantHelper {
    static CustomVillageTrades plugin;
    private static Random rand = new Random();
    private static List<Material> enchantable_items = Arrays.asList(
            Material.FISHING_ROD,
            Material.CARROT_STICK, Material.SHEARS, Material.BOOK, Material.BOW,
            Material.FLINT_AND_STEEL, Material.SHIELD, Material.ELYTRA,
            Material.WOOD_AXE, Material.WOOD_HOE, Material.WOOD_PICKAXE, 
            Material.WOOD_SPADE, Material.WOOD_SWORD, Material.STONE_AXE,
            Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SPADE, 
            Material.STONE_SWORD, Material.IRON_AXE, Material.IRON_HOE,
            Material.IRON_PICKAXE, Material.IRON_SPADE, Material.IRON_SWORD,
            Material.GOLD_AXE, Material.GOLD_HOE, Material.GOLD_PICKAXE, 
            Material.GOLD_SPADE, Material.GOLD_SWORD, Material.DIAMOND_AXE, 
            Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, 
            Material.DIAMOND_SPADE,
            Material.DIAMOND_SWORD, Material.LEATHER_BOOTS, 
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
            Material.IRON_BOOTS, Material.IRON_LEGGINGS, 
            Material.IRON_CHESTPLATE,
            Material.IRON_HELMET, Material.DIAMOND_BOOTS, 
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET,
            Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS,
            Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET,
            Material.GOLD_BOOTS, Material.GOLD_LEGGINGS,
            Material.GOLD_CHESTPLATE, Material.GOLD_HELMET,
            Material.ENCHANTED_BOOK);
    
    private static List<Material> enchantability_25 = Arrays.asList(
            Material.GOLD_BOOTS, Material.GOLD_LEGGINGS, 
            Material.GOLD_CHESTPLATE, Material.GOLD_HELMET);
    
    private static List<Material> enchantability_22 = Arrays.asList(
            Material.GOLD_AXE,
            Material.GOLD_HOE, Material.GOLD_PICKAXE, Material.GOLD_SPADE, 
            Material.GOLD_SWORD);
    
    private static List<Material> enchantability_15 = Arrays.asList(
            Material.WOOD_AXE, Material.WOOD_HOE, Material.WOOD_PICKAXE, 
            Material.WOOD_SPADE, Material.WOOD_SWORD, Material.LEATHER_BOOTS, 
            Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, 
            Material.LEATHER_HELMET);
    
    private static List<Material> enchantability_14 = Arrays.asList(
            Material.IRON_AXE, 
            Material.IRON_HOE, Material.IRON_PICKAXE, Material.IRON_SPADE, 
            Material.IRON_SWORD);
    
    private static List<Material> enchantability_12 = Arrays.asList(
            Material.CHAINMAIL_BOOTS,
            Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, 
            Material.CHAINMAIL_HELMET);
    
    private static List<Material> enchantability_10 = Arrays.asList(
            Material.DIAMOND_AXE, 
            Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, 
            Material.DIAMOND_SPADE,
            Material.DIAMOND_SWORD, Material.DIAMOND_BOOTS, 
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET);
    
    private static List<Material> enchantability_9 = Arrays.asList(
            Material.IRON_BOOTS, 
            Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, 
            Material.IRON_HELMET);
    
    private static List<Material> enchantability_5 = Arrays.asList(
            Material.STONE_AXE,
            Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SPADE, 
            Material.STONE_SWORD);
    
    private static List<Enchantment> treasure = Arrays.asList(
            Enchantment.FROST_WALKER, Enchantment.MENDING);
    
    public EnchantHelper(CustomVillageTrades instance) {
    	plugin = instance;
    }
    
    // Lifted from mc because I am not writing this out by hand
    public static int clamp_int(int num, int min, int max)
    {
        return num < min ? min : (num > max ? max : num);
    }
    
    public static ItemStack randomEnchantedBook(CustomVillageTrades instance,
            int level, boolean allowTreasure) {
        ItemStack book = new ItemStack(Material.BOOK);
        List<LeveledEnchantment> list = new ArrayList<LeveledEnchantment>();
        while(list.size() < 1) {
        	list = buildEnchantmentList(instance, book, level, allowTreasure);
        }
        
        book.setType(Material.ENCHANTED_BOOK);
        LeveledEnchantment enchantment = list.get(rand.nextInt(list.size()));
        book = applyEnchantment(instance, book, enchantment);
        return book;
    }
        
    
    // Returns a random enchantment roughly equivalent to mc's random enchants
    public static ItemStack randomEnchantment(CustomVillageTrades instance,
    		ItemStack item, int level, boolean allowTreasure) {
        if (enchantable_items.contains(item.getType())) {
            boolean isBook = item.getType() == Material.BOOK;
            List<LeveledEnchantment> list = new ArrayList<LeveledEnchantment>();
            while(list.size() < 1) {
            	list = buildEnchantmentList(instance, item, level, allowTreasure);
            }
            
            if (isBook) {
                item.setType(Material.ENCHANTED_BOOK);
            }
            for (LeveledEnchantment enchantment : list) {
            	item = applyEnchantment(instance, item, enchantment);
            }
        }
        
        return item;
    }
    
    // Makes a list of enchantments an item might have
    public static List<LeveledEnchantment> buildEnchantmentList(
    		CustomVillageTrades instance, ItemStack item,
            int level, boolean allowTreasure) {
        List<LeveledEnchantment> list = new ArrayList<LeveledEnchantment>();
        int enchantability;
        
        if (enchantability_25.contains(item.getType())) {
            enchantability = 25;
        }
        else if (enchantability_22.contains(item.getType())) {
            enchantability = 22;
        }
        else if (enchantability_15.contains(item.getType())) {
            enchantability = 15;
        }
        else if (enchantability_14.contains(item.getType())) {
            enchantability = 14;
        }
        else if (enchantability_12.contains(item.getType())) {
            enchantability = 12;
        }
        else if (enchantability_10.contains(item.getType())) {
            enchantability = 10;
        }
        else if (enchantability_9.contains(item.getType())) {
            enchantability = 9;
        }
        else if (enchantability_5.contains(item.getType())) {
            enchantability = 5;
        }
        else {
            enchantability = 1;
        }
        
        level = level + 1 + rand.nextInt(enchantability / 4 + 1) + 
                rand.nextInt(enchantability / 4 + 1);
        float f = (rand.nextFloat() + rand.nextFloat() - 1.0F) * 0.15F;
        level = clamp_int(Math.round((float)level + (float)level * f),
                1, Integer.MAX_VALUE);
        List<LeveledEnchantment> list1 = getEnchantments(instance, item,
                level, allowTreasure);
        
        if (!list1.isEmpty()) {
            list.add(LeveledEnchantment.getRandomLeveledEnchant(
            		instance, list1, rand));
            
            while (rand.nextInt(50) <= level) {
                list1 = removeIncompatibleEnchants(list1, list);
                
                if (list1.isEmpty()) {
                    break;
                }
                
                list.add(LeveledEnchantment.getRandomLeveledEnchant(
                        instance, list1, rand));
                level /= 2;
            }
        }
        
        return list;
    }
    
    // Get all enchantments that might apply to an item
    public static List<LeveledEnchantment> getEnchantments(
    		CustomVillageTrades instance, ItemStack item,
            int level, boolean allowTreasure) {
        List<LeveledEnchantment> list = new ArrayList<LeveledEnchantment>();
        boolean isBook = item.getType() == Material.BOOK;
        
        for (Enchantment enchantment : Enchantment.values()) {
            if ((!treasure.contains(enchantment) || allowTreasure) && 
                    (enchantment.canEnchantItem(item) || isBook)) {
                for (int i = enchantment.getMaxLevel(); 
                        i > enchantment.getStartLevel() - 1; --i) {
                    if (level >= (1 + i * 10) && level <= ((1 + i * 10) + 5)) {
                        list.add(new LeveledEnchantment(instance,
                                enchantment.hashCode(), i));
                    }
                }
            }
        }
        
        return list;
    }
    
    // Remove enchantments from the pool of possibilities based on selected
    public static List<LeveledEnchantment> removeIncompatibleEnchants(
            List<LeveledEnchantment> pool, List<LeveledEnchantment> list) {
    	List<LeveledEnchantment> new_pool = new ArrayList<LeveledEnchantment>();
    	new_pool.addAll(pool);
        for (LeveledEnchantment possibility : pool) {
            for (LeveledEnchantment selected : list) {
                if (possibility.conflictsWith(selected)) {
                    new_pool.remove(possibility);
                }
            }
        }
        return new_pool;
    }
    
    public static ItemStack applyEnchantment(CustomVillageTrades instance,
    		ItemStack item, LeveledEnchantment enchantment) {
    	if (item.getType().equals(Material.BOOK) ||
    			item.getType().equals(Material.ENCHANTED_BOOK)) {
            EnchantmentStorageMeta meta = 
            		(EnchantmentStorageMeta)item.getItemMeta();
    		meta.addStoredEnchant(enchantment.getEnchantment(),
            		enchantment.getLevel(), false);
            item.setItemMeta(meta);
    	}
    	else {
            item.addEnchantment(enchantment.getEnchantment(),
                        enchantment.getLevel());
    	}
    	return item;
    }
}
