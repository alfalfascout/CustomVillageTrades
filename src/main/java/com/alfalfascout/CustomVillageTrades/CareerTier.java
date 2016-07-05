package com.alfalfascout.CustomVillageTrades;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class CareerTier {
    
    static CustomVillageTrades plugin;
    public String career;
    public int tier;
    
    public CareerTier(CustomVillageTrades instance) {
        this.career = "villager";
        this.tier = 0;
        plugin = instance;
    }
    
    // get villager's career and trade tier based on current trade
    public static CareerTier setCareerTier(CareerTier careerTier,
            Villager villager, MerchantRecipe recipe) {
        ItemStack result = recipe.getResult();
        List<ItemStack> ingredients = recipe.getIngredients();
        
        // farmer
        if (result.getType().equals(Material.BREAD)) {
            careerTier.career = "farmer";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.PUMPKIN_PIE)) {
            careerTier.career = "farmer";
            careerTier.tier = 2;
        }
        else if (result.getType().equals(Material.APPLE)) {
            careerTier.career = "farmer";
            careerTier.tier = 3;
        }
        else if (result.getType().equals(Material.COOKIE)) {
            careerTier.career = "farmer";
            careerTier.tier = 4;
        } // fisherman
        else if (result.getType().equals(Material.COOKED_FISH)) {
            careerTier.career = "fisherman";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.FISHING_ROD)) {
            careerTier.career = "fisherman";
            careerTier.tier = 2;
        } // fletcher
        else if (result.getType().equals(Material.ARROW)) {
            careerTier.career = "fletcher";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.BOW)) {
            careerTier.career = "fletcher";
            careerTier.tier = 2;
        } // shepherd
        else if (result.getType().equals(Material.SHEARS)) {
            careerTier.career = "shepherd";
            careerTier.tier = 1;
        }
        else if (result.equals(new ItemStack(Material.WOOL, 1, (short)1))) {
            careerTier.career = "shepherd";
            careerTier.tier = 2;
        } // librarian
        else if (ingredients.get(0).getType().equals(Material.PAPER)) {
            careerTier.career = "librarian";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.BOOKSHELF)) {
            careerTier.career = "librarian";
            careerTier.tier = 2;
        }
        else if (result.getType().equals(Material.GLASS)) {
            careerTier.career = "librarian";
            careerTier.tier = 3;
        }
        else if (result.getType().equals(Material.NAME_TAG)) {
            careerTier.career = "librarian";
            careerTier.tier = 6;
        }
        else if (result.getType().equals(Material.ENCHANTED_BOOK)) {
            careerTier.career = "librarian";
            careerTier.tier = 0;
        }// butcher
        else if (ingredients.get(0).getType().equals(Material.RAW_CHICKEN)) {
            careerTier.career = "butcher";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.COOKED_CHICKEN)) {
            careerTier.career = "butcher";
            careerTier.tier = 2;
        }// leatherworker
        else if (result.getType().equals(Material.LEATHER_LEGGINGS)) {
            careerTier.career = "leatherworker";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.LEATHER_CHESTPLATE)) {
            careerTier.career = "leatherworker";
            careerTier.tier = 2;
        }
        else if (result.getType().equals(Material.SADDLE)) {
            careerTier.career = "leatherworker";
            careerTier.tier = 3;
        }// armorer
        else if (result.getType().equals(Material.IRON_HELMET)) {
            careerTier.career = "armorer";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.IRON_CHESTPLATE)) {
            careerTier.career = "armorer";
            careerTier.tier = 2;
        }
        else if (result.getType().equals(Material.DIAMOND_CHESTPLATE)) {
            careerTier.career = "armorer";
            careerTier.tier = 3;
        }
        else if (result.getType().equals(Material.CHAINMAIL_CHESTPLATE)) {
            careerTier.career = "armorer";
            careerTier.tier = 4;
        }// weaponsmith
        else if (result.getType().equals(Material.IRON_AXE)) {
            careerTier.career = "weaponsmith";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.IRON_SWORD)) {
            careerTier.career = "weaponsmith";
            careerTier.tier = 2;
        }
        else if (result.getType().equals(Material.DIAMOND_SWORD)) {
            careerTier.career = "weaponsmith";
            careerTier.tier = 3;
        }// toolsmith
        else if (result.getType().equals(Material.IRON_SPADE)) {
            careerTier.career = "toolsmith";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.IRON_PICKAXE)) {
            careerTier.career = "toolsmith";
            careerTier.tier = 2;
        }
        else if (result.getType().equals(Material.DIAMOND_PICKAXE)) {
            careerTier.career = "toolsmith";
            careerTier.tier = 3;
        }// cleric
        else if (ingredients.get(0).getType().equals(Material.ROTTEN_FLESH)) {
            careerTier.career = "cleric";
            careerTier.tier = 1;
        }
        else if (result.getType().equals(Material.REDSTONE)) {
            careerTier.career = "cleric";
            careerTier.tier = 2;
        }
        else if (result.getType().equals(Material.GLOWSTONE)) {
            careerTier.career = "cleric";
            careerTier.tier = 3;
        }
        else if (result.getType().equals(Material.EXP_BOTTLE)) {
            careerTier.career = "cleric";
            careerTier.tier = 4;
        }
        
        
        if (careerTier.career == "librarian" && careerTier.tier == 0) {
            careerTier.tier = getLastTier(careerTier, villager, recipe);
            if (careerTier.tier > 1) {
                careerTier.tier++;
            }
            else {
                careerTier.tier = 0;
            }
        }
        
        if (careerTier.tier > 0) {
            saveVillager(careerTier, villager);
        }
        
        return careerTier;
    }
    
    //figure out what tier a librarian is in
    public static int getLastTier(CareerTier careertier,
            Villager villager, MerchantRecipe recipe) {
        int last = 0;
        
        //check the librarian file
        String villager_id = "id" + Integer.toString(villager.getEntityId());
        if (plugin.getVillagers().contains(villager_id)) {
            last = plugin.getVillagers().getInt(villager_id);
        }
        //check librarian tier list against their trade list
        else if (plugin.getConfig().contains("librarian")) {
            List<Integer> tradesbytier = new ArrayList<Integer>();
            int currenttrades = villager.getRecipeCount();
            
            if (plugin.getConfig().getString("librarian") != "default") {
                for (String tier : plugin.getConfig().getStringList(
                        "librarian")) {
                    tradesbytier.add(plugin.getConfig().getList(tier).size() + 
                            tradesbytier.get(tradesbytier.size() - 1));
                }
            }
            else {
                for (String tier : plugin.getVanilla().getStringList(
                        "librarian")) {
                    tradesbytier.add(plugin.getVanilla().getList(tier).size() +
                            tradesbytier.get(tradesbytier.size() - 1));
                }
            }
            for (int trades : tradesbytier) {
                if (currenttrades < trades) {
                    last = tradesbytier.indexOf(trades);
                }
            }
        }
        else {
            plugin.getLogger().warning("Librarians missing from config.yml");
        }
        
        return last;
    }
    
    public static void saveVillager(CareerTier careertier, Villager villager) {
        String villager_id = "id" + Integer.toString(villager.getEntityId());
        if (!plugin.getVillagers().contains(villager_id)) {
            plugin.getVillagers().createSection(villager_id);
        }
        
        plugin.getVillagers().set(villager_id, careertier.tier);
    }
}
