package com.alfalfascout.CustomVillageTrades;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

public class CareerTier { // FIXME: 8 Sep 2018 Decide whether this still needs its own class or not
    
    static CustomVillageTrades plugin;
    public int tier;
    public boolean lastVanilla;
    
    public CareerTier(CustomVillageTrades instance) {
        this.tier = 0;
        this.lastVanilla = false;
        plugin = instance;
    }
    
    // get villager's career and trade tier based on current trade
    public CareerTier setCareerTier(Villager villager) { // FIXME: 8 Sep 2018 Revise or remove function as necessary
        switch (villager.getRecipeCount()) {
            case 0:
                break;
            case 1:
                if (villager.getCareer() == Villager.Career.CARTOGRAPHER) { this.tier = 1; }
        }
        
        if (this.tier > 0) {
            saveVillager(this, villager);
        }
        
        return this;
    }
    
    //figure out what tier a librarian is in
    public int getLastLibrarianTier(Villager villager, // FIXME: 8 Sep 2018 Simplify or eliminate this function
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
        plugin.getVillagers().set(
                villagerId + ".lastvanilla", careerTier.lastVanilla);
        plugin.saveVillagers();
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
            this.lastVanilla = plugin.getVillagers().getBoolean(
                    villagerId + ".lastvanilla");
        }
        else {
            this.tier = plugin.getVillagers().getInt(legacyVillagerId);
            
            saveVillager(this, villager);
        }
    }
}
