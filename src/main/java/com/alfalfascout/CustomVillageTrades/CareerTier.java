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
    public boolean lastVanilla;
    
    public CareerTier(CustomVillageTrades instance) {
        this.career = "villager";
        this.tier = 0;
        this.lastVanilla = false;
        plugin = instance;
    }
    
    // get villager's career and trade tier based on current trade
    public CareerTier setCareerTier(Villager villager, 
            MerchantRecipe recipe) {
        ItemStack result = recipe.getResult();
        List<ItemStack> ingredients = recipe.getIngredients();
        
        // farmer
        if (result.getType().equals(Material.BREAD)) {
            this.career = "farmer";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.PUMPKIN_PIE)) {
            this.career = "farmer";
            this.tier = 2;
        }
        else if (result.getType().equals(Material.APPLE)) {
            this.career = "farmer";
            this.tier = 3;
        }
        else if (result.getType().equals(Material.COOKIE)) {
            this.career = "farmer";
            this.tier = 4;
            this.lastVanilla = true;
        } // fisherman
        else if (result.getType().equals(Material.COOKED_FISH)) {
            this.career = "fisherman";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.FISHING_ROD)) {
            this.career = "fisherman";
            this.tier = 2;
            this.lastVanilla = true;
        } // fletcher
        else if (result.getType().equals(Material.ARROW)) {
            this.career = "fletcher";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.BOW)) {
            this.career = "fletcher";
            this.tier = 2;
            this.lastVanilla = true;
        } // shepherd
        else if (result.getType().equals(Material.SHEARS)) {
            this.career = "shepherd";
            this.tier = 1;
        }
        else if (result.equals(new ItemStack(Material.WOOL, 1, (short)1))) {
            this.career = "shepherd";
            this.tier = 2;
            this.lastVanilla = true;
        } // librarian
        else if (ingredients.get(0).getType().equals(Material.PAPER)) {
            this.career = "librarian";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.BOOKSHELF)) {
            this.career = "librarian";
            this.tier = 2;
        }
        else if (result.getType().equals(Material.GLASS)) {
            this.career = "librarian";
            this.tier = 3;
        }
        else if (result.getType().equals(Material.NAME_TAG)) {
            this.career = "librarian";
            this.tier = 6;
            this.lastVanilla = true;
        }
        else if (result.getType().equals(Material.ENCHANTED_BOOK)) {
            this.career = "librarian";
            this.tier = 0;
        }// butcher
        else if (ingredients.get(0).getType().equals(Material.RAW_CHICKEN)) {
            this.career = "butcher";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.COOKED_CHICKEN)) {
            this.career = "butcher";
            this.tier = 2;
            this.lastVanilla = true;
        }// leatherworker
        else if (result.getType().equals(Material.LEATHER_LEGGINGS)) {
            this.career = "leatherworker";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.LEATHER_CHESTPLATE)) {
            this.career = "leatherworker";
            this.tier = 2;
        }
        else if (result.getType().equals(Material.SADDLE)) {
            this.career = "leatherworker";
            this.tier = 3;
            this.lastVanilla = true;
        }// armorer
        else if (result.getType().equals(Material.IRON_HELMET)) {
            this.career = "armorer";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.IRON_CHESTPLATE)) {
            this.career = "armorer";
            this.tier = 2;
        }
        else if (result.getType().equals(Material.DIAMOND_CHESTPLATE)) {
            this.career = "armorer";
            this.tier = 3;
        }
        else if (result.getType().equals(Material.CHAINMAIL_CHESTPLATE)) {
            this.career = "armorer";
            this.tier = 4;
            this.lastVanilla = true;
        }// weaponsmith
        else if (result.getType().equals(Material.IRON_AXE)) {
            this.career = "weaponsmith";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.IRON_SWORD)) {
            this.career = "weaponsmith";
            this.tier = 2;
        }
        else if (result.getType().equals(Material.DIAMOND_SWORD)) {
            this.career = "weaponsmith";
            this.tier = 3;
            this.lastVanilla = true;
        }// toolsmith
        else if (result.getType().equals(Material.IRON_SPADE)) {
            this.career = "toolsmith";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.IRON_PICKAXE)) {
            this.career = "toolsmith";
            this.tier = 2;
        }
        else if (result.getType().equals(Material.DIAMOND_PICKAXE)) {
            this.career = "toolsmith";
            this.tier = 3;
            this.lastVanilla = true;
        }// cleric
        else if (ingredients.get(0).getType().equals(Material.ROTTEN_FLESH)) {
            this.career = "cleric";
            this.tier = 1;
        }
        else if (result.getType().equals(Material.REDSTONE)) {
            this.career = "cleric";
            this.tier = 2;
        }
        else if (result.getType().equals(Material.GLOWSTONE)) {
            this.career = "cleric";
            this.tier = 3;
        }
        else if (result.getType().equals(Material.EXP_BOTTLE)) {
            this.career = "cleric";
            this.tier = 4;
            this.lastVanilla = true;
        }
        
        
        if (this.career == "librarian" && this.tier == 0) {
            this.tier = this.getLastLibrarianTier(villager, recipe);
            if (this.tier > 1) {
                this.tier++;
            }
            else {
                this.tier = 0;
            }
        }
        
        if (this.tier > 0) {
            saveVillager(this, villager);
        }
        
        return this;
    }
    
    //figure out what tier a librarian is in
    public int getLastLibrarianTier(Villager villager, 
            MerchantRecipe recipe) {
        int last = 0;
        
        //check the villager file
        String villagerId = "id" + villager.getUniqueId().toString();
        if (plugin.getVillagers().contains(villagerId)) {
            if (plugin.getVillagers().isInt(villagerId)) { //backwards compat
                last = plugin.getVillagers().getInt(villagerId);
            }
            else {
                last = plugin.getVillagers().getInt(villagerId + ".tier");
            }
        }
        //check librarian tier list against their trade list
        else if (plugin.getConfig().contains("librarian")) {
            List<Integer> tradesByTier = new ArrayList<Integer>();
            int currenttrades = villager.getRecipeCount();
            
            if (plugin.getConfig().getString("librarian") != "default") {
                for (String tier : plugin.getConfig().getStringList(
                        "librarian")) {
                    tradesByTier.add(plugin.getConfig().getList(tier).size() + 
                            tradesByTier.get(tradesByTier.size() - 1));
                }
            }
            else {
                for (String tier : plugin.getTree("vanilla").conf.getStringList(
                        "librarian")) {
                    tradesByTier.add(
                            plugin.getTree("vanilla").conf.getList(tier).size() +
                            tradesByTier.get(tradesByTier.size() - 1));
                }
            }
            for (int trades : tradesByTier) {
                if (currenttrades < trades) {
                    last = tradesByTier.indexOf(trades);
                }
            }
        }
        else {
            plugin.getLogger().warning("Librarians missing from config.yml");
        }
        
        return last;
    }
    
    public void getLastCareerTier(Villager villager) {
        String villagerId = "id" + villager.getUniqueId().toString();
        
        this.career = plugin.getVillagers().getString(villagerId + ".career");
        this.tier = plugin.getVillagers().getInt(villagerId + ".tier");
        this.lastVanilla = plugin.getVillagers().getBoolean(
                villagerId + ".lastvanilla");
    }
    
    public static void saveVillager(CareerTier careerTier, Villager villager) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (!plugin.getVillagers().contains(villagerId)) {
            plugin.getVillagers().createSection(villagerId);
        }
        
        plugin.getVillagers().set(villagerId + ".tier", careerTier.tier);
        plugin.getVillagers().set(villagerId + ".career", careerTier.career);
        plugin.getVillagers().set(
                villagerId + ".lastvanilla", careerTier.lastVanilla);
    }
    
    public void loadVillager(Villager villager) { 
        String villagerId = "id" + villager.getUniqueId().toString();
        String legacyVillagerId = "id" + 
                Integer.toString(villager.getEntityId());
        
        if (!(plugin.getVillagers().contains(villagerId) || 
                plugin.getVillagers().contains(legacyVillagerId))) {
            saveVillager(this, villager);
            return;
        }
        
        else if (!plugin.getVillagers().isInt(villagerId)) {
            this.tier = plugin.getVillagers().getInt(villagerId + ".tier");
            this.career = plugin.getVillagers().getString(
                    villagerId + ".career");
            this.lastVanilla = plugin.getVillagers().getBoolean(
                    villagerId + ".lastvanilla");
        }
        else {
            this.tier = plugin.getVillagers().getInt(legacyVillagerId);
            this.career = "villager";
            
            saveVillager(this, villager);
        }
    }
}
