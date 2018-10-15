package com.alfalfascout.CustomVillageTrades;

import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.enchantments.*;

class EnchantHelper {
    private static CustomVillageTrades plugin;
    private static final Random rand = new Random();
    /*private static final List<Material> enchantableItems = Arrays.asList(
            Material.FISHING_ROD,
            Material.CARROT_ON_A_STICK, Material.SHEARS, Material.BOOK, Material.BOW,
            Material.FLINT_AND_STEEL, Material.SHIELD, Material.ELYTRA,
            Material.WOODEN_AXE, Material.WOODEN_HOE, Material.WOODEN_PICKAXE, 
            Material.WOODEN_SHOVEL, Material.WOODEN_SWORD, Material.STONE_AXE,
            Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SHOVEL, 
            Material.STONE_SWORD, Material.IRON_AXE, Material.IRON_HOE,
            Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_SWORD,
            Material.GOLDEN_AXE, Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, 
            Material.GOLDEN_SHOVEL, Material.GOLDEN_SWORD, Material.DIAMOND_AXE, 
            Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, 
            Material.DIAMOND_SHOVEL,
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
            Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET,
            Material.ENCHANTED_BOOK);*/
    
    private static final List<Material> enchantability_25 = Arrays.asList(
            Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, 
            Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET);
    
    private static final List<Material> enchantability_22 = Arrays.asList(
            Material.GOLDEN_AXE,
            Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, 
            Material.GOLDEN_SWORD);
    
    private static final List<Material> enchantability_15 = Arrays.asList(
            Material.WOODEN_AXE, Material.WOODEN_HOE, Material.WOODEN_PICKAXE, 
            Material.WOODEN_SHOVEL, Material.WOODEN_SWORD, Material.LEATHER_BOOTS, 
            Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, 
            Material.LEATHER_HELMET);
    
    private static final List<Material> enchantability_14 = Arrays.asList(
            Material.IRON_AXE, 
            Material.IRON_HOE, Material.IRON_PICKAXE, Material.IRON_SHOVEL, 
            Material.IRON_SWORD);
    
    private static final List<Material> enchantability_12 = Arrays.asList(
            Material.CHAINMAIL_BOOTS,
            Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, 
            Material.CHAINMAIL_HELMET);
    
    private static final List<Material> enchantability_10 = Arrays.asList(
            Material.DIAMOND_AXE, 
            Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, 
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_SWORD, Material.DIAMOND_BOOTS, 
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET);
    
    private static final List<Material> enchantability_9 = Arrays.asList(
            Material.IRON_BOOTS, 
            Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, 
            Material.IRON_HELMET);
    
    private static final List<Material> enchantability_5 = Arrays.asList(
            Material.STONE_AXE,
            Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SHOVEL, 
            Material.STONE_SWORD);
    
    public EnchantHelper(CustomVillageTrades instance) {
        plugin = instance;
    }
    
    // return a number not bigger than max or smaller that min, initially num
    private static int clampInt(int num, int min, int max) {
        return num < min ? min : (num > max ? max : num);
    }
    
    // get one enchantment type that could be applied to a given item
    public Enchantment getRandomEnchantment(ItemStack item) {
        List<Enchantment> possibilities = new ArrayList<Enchantment>();
        
        for (Enchantment enchantment : Enchantment.values()) {
            if (enchantment.canEnchantItem(item) || 
                    item.getType().equals(Material.ENCHANTED_BOOK)) {
                possibilities.add(enchantment);
            }
        }

        plugin.getLogger().info(Integer.toString(possibilities.size()) + " possibilities to enchant " + item.getType().toString());
        
        return possibilities.get(rand.nextInt(possibilities.size()));
    }
    
    // Returns an enchanted book roughly equivalent to a vanilla librarian's
    public  ItemStack randomlyEnchantBook( 
            ItemStack enchantedBook, boolean allowTreasure) {
        Enchantment type = getRandomEnchantment(enchantedBook);
        while (!(!type.isTreasure() || allowTreasure)) {
            type = getRandomEnchantment(enchantedBook);
        }
        
        int level = rand.nextInt(type.getMaxLevel()) + 1;
        
        LeveledEnchantment enchantment = 
                new LeveledEnchantment(plugin, type.getKey().getKey(), level);
        
        enchantedBook = applyEnchantment(enchantedBook, enchantment);
        
        return enchantedBook;
    }
        
    
    // Enchants the item randomly, roughly equivalent to mc's random enchants
    public  ItemStack randomlyEnchant(ItemStack item, int level, 
            boolean allowTreasure) {
        
        if (Enchantment.VANISHING_CURSE.canEnchantItem(item)) {
            plugin.getLogger().info("Item can be enchanted...");
            boolean isBook = item.getType() == Material.BOOK;
            List<LeveledEnchantment> enchants = new ArrayList<LeveledEnchantment>();

            if (isBook) {
                item.setType(Material.ENCHANTED_BOOK);
            }
            
            while(enchants.size() < 1) {
                enchants = buildLeveledEnchantmentList(
                        item, level, allowTreasure);
            }
            
            for (LeveledEnchantment enchantment : enchants) {
                plugin.getLogger().info("Applying enchantment to item:" + enchantment.toString());
                item = applyEnchantment(item, enchantment);
            }
        }
        
        return item;
    }
    
    // Builds a list of semi-random enchantments for given item
    private List<LeveledEnchantment> buildLeveledEnchantmentList(ItemStack item,
                                                                 int level, boolean allowTreasure) {
        List<LeveledEnchantment> enchants = new ArrayList<LeveledEnchantment>();
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
        float margin = (rand.nextFloat() + rand.nextFloat() - 1.0F) * 0.15F;
        level = clampInt(Math.round((float)level + (float)level * margin),
                1, Integer.MAX_VALUE);
        
        List<LeveledEnchantment> possibilities = getLeveledEnchantments(item, level, allowTreasure);
        
        if (!possibilities.isEmpty()) {
            enchants.add(LeveledEnchantment.getRandomLeveledEnchant(possibilities, rand));
            
            while (rand.nextInt(50) <= level) {
                possibilities = removeIncompatibleEnchants(possibilities, enchants);
                
                if (possibilities.isEmpty()) {
                    break;
                }
                
                enchants.add(LeveledEnchantment.getRandomLeveledEnchant(possibilities, rand));
                level /= 2;
            }
        }
        
        return enchants;
    }
    
    // Get all enchantments that might apply to an item
    private List<LeveledEnchantment> getLeveledEnchantments(ItemStack item,
                                                            int level, boolean allowTreasure) {
        List<LeveledEnchantment> enchants = new ArrayList<LeveledEnchantment>();
        boolean isBook = item.getType() == Material.ENCHANTED_BOOK;
        
        for (Enchantment enchantment : Enchantment.values()) {
            
            if ((!enchantment.isTreasure() || allowTreasure) &&
                    (enchantment.canEnchantItem(item) || isBook)) {
                
                for (int i = enchantment.getMaxLevel(); 
                        i > enchantment.getStartLevel() - 1; --i) {
                    
                    if (level >= (1 + i * 10) && level <= ((1 + i * 10) + 5)) {
                        
                        enchants.add(new LeveledEnchantment(plugin,
                                enchantment.getKey().getKey(), i));
                    }
                }
            }
        }
        
        return enchants;
    }
    
    // Remove enchantments from the pool of possibilities based on selected
    private List<LeveledEnchantment> removeIncompatibleEnchants(
            List<LeveledEnchantment> pool, List<LeveledEnchantment> enchants) {
        List<LeveledEnchantment> newPool = new ArrayList<LeveledEnchantment>();
        newPool.addAll(pool);
        
        for (LeveledEnchantment possibility : pool) {
            for (LeveledEnchantment selected : enchants) {
                if (possibility.conflictsWith(selected)) {
                    newPool.remove(possibility);
                }
            }
        }
        
        return newPool;
    }
    
    // Either applies or stores enchantments as appropriate for item type
    public ItemStack applyEnchantment(ItemStack item,
            LeveledEnchantment enchantment) {
        if (item.getType().equals(Material.ENCHANTED_BOOK)) {
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
    
    // Gets a vanilla-appropriate value in currency for an enchanted book
    public ItemStack appraiseEnchantedBook(
            FileConfiguration f, ItemStack book) {
        ItemStack price = new ItemStack(plugin.getCurrency(f));
        
        if (!book.getType().equals(Material.ENCHANTED_BOOK)) {
            plugin.getLogger().warning(
                    "Auto pricing is only for enchanted books.");
            return price;
        }
        
        EnchantmentStorageMeta meta = 
                (EnchantmentStorageMeta) book.getItemMeta();
        
        if (!meta.hasStoredEnchants()) {
            plugin.getLogger().warning("This book has no enchantments.");
            return price;
        }
        
        Map<Enchantment,Integer> enchants = meta.getStoredEnchants();
        
        if (enchants.size() > 1) {
            plugin.getLogger().warning(
                    "Auto pricing only works for books with one enchantment.");
            return price;
        }

        Enchantment type = Enchantment.DURABILITY;
        int level = 1;
        for (Map.Entry<Enchantment,Integer> enchant : enchants.entrySet()) {
            type = enchant.getKey();
            level = enchant.getValue();
        }
        
        int amount = 1;
        
        switch (level) {
            case 1:
                amount = rand.nextInt(14) + 5;
                break;
            case 2:
                amount = rand.nextInt(24) + 8;
                break;
            case 3:
                amount = rand.nextInt(34) + 11;
                break;
            case 4:
                amount = rand.nextInt(44) + 14;
                break;
            case 5:
                amount = clampInt((rand.nextInt(54) + 17), 17, 64);
                break;
            default: 
                amount = 1;
        }
        
        if (type.isTreasure()) {
            amount *= 2;
        }
        
        if (price.getMaxStackSize() == 16) {
            amount /= 4;
        }
        
        else if (price.getMaxStackSize() == 1) {
            plugin.getLogger().warning("In theory, you could charge " + 
                    Integer.toString(amount) + " " + 
                    price.getType().toString() + 
                    " for an enchanted book, but in practicality " +
                    "the max stack size is " + 
                    Integer.toString(price.getMaxStackSize()) + ".");
            amount = 1;
        }
        
        price.setAmount(amount);
        
        return price;
    }
}
