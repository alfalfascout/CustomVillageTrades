package com.alfalfascout.CustomVillageTrades;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

class VillagerHelper {
    private static CustomVillageTrades plugin;
    
    VillagerHelper(CustomVillageTrades instance) {
        plugin = instance;
    }

    // get villager's trade tier from file, else assume first tier
    int getCareerTier(Villager villager) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (plugin.getVillagers().contains(villagerId) && plugin.getVillagers().isInt(villagerId + ".tier")) {
            return plugin.getVillagers().getInt(villagerId + ".tier");
        }
        else {
            saveVillager(villager, 1);
            return 1;
        }
    }

    // get villager's trade tier from recipe
    int getCareerTier(Villager villager, MerchantRecipe recipe) {
        int tier = determineCareerTier(villager, recipe);
        saveVillager(villager, tier);
        return tier;
    }

    // determine villager's trade tier from recipe or file, if impossible assume first tier
    int determineCareerTier(Villager villager, MerchantRecipe recipe) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (lastTradeInTier(villager, recipe)) {
            if(plugin.getVillagers().getBoolean(villagerId + ".lastvanilla")) {
                return plugin.getVillagers().getInt(villagerId + ".tier");
            }
            return getTierOfTrade(villager, recipe);
        }
        else if (plugin.getVillagers().contains(villagerId) && plugin.getVillagers().isInt(villagerId + ".tier")) {
            return plugin.getVillagers().getInt(villagerId + ".tier");
        }
        else {
            return 1;
        }
    }

    void saveVillager(Villager villager, int careerTier) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (!plugin.getVillagers().contains(villagerId)) {
            plugin.getVillagers().createSection(villagerId);
        }

        plugin.getVillagers().set(villagerId + ".tier", careerTier);
        plugin.saveVillagers();
    }

    void saveVillager(Villager villager, boolean lastVanilla) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (!plugin.getVillagers().contains(villagerId)) {
            plugin.getVillagers().createSection(villagerId);
        }

        plugin.getVillagers().set(villagerId + ".lastvanilla", lastVanilla);
        plugin.saveVillagers();
    }

    void saveVillager(Villager villager, int careerTier, boolean lastVanilla) {
        saveVillager(villager, careerTier);
        saveVillager(villager, lastVanilla);
    }

    boolean knownVillager(Villager villager) {
        String villagerId = "id" + villager.getUniqueId().toString();

        if (!(plugin.getVillagers().contains(villagerId))) {
            saveVillager(villager, 1);
            return false;
        }

        else {
            return plugin.getVillagers().isInt(villagerId + ".tier") ||
                    plugin.getVillagers().isBoolean(villagerId + ".lastvanilla");
        }
    }

    // Determines whether the trade passed in matches the last trade in the villager's vanilla trade list
    boolean lastVanillaTrade(Villager villager, MerchantRecipe recipe) {
        Material result = recipe.getResult().getType();
        int tier = 0;
        boolean lastVanilla = false;

        switch (villager.getCareer()) {
            case ARMORER:
                lastVanilla = result == Material.CHAINMAIL_CHESTPLATE;
                if (lastVanilla) { tier = 4; saveVillager(villager, tier, lastVanilla); }
                break;
            case BUTCHER:
                lastVanilla = result == Material.COOKED_CHICKEN;
                if (lastVanilla) { tier = 2; saveVillager(villager, tier, lastVanilla); }
                break;
            case CARTOGRAPHER:
                lastVanilla = recipe.getResult().hasItemMeta() &&
                        recipe.getResult().getItemMeta().getDisplayName().equalsIgnoreCase("Woodland Explorer Map");
                if (lastVanilla) { tier = 4; saveVillager(villager, tier, lastVanilla); }
                break;
            case CLERIC:
                lastVanilla = result == Material.EXPERIENCE_BOTTLE;
                if (lastVanilla) { tier = 4; saveVillager(villager, tier, lastVanilla); }
                break;
            case FARMER:
                lastVanilla = result == Material.CAKE;
                if (lastVanilla) { tier = 4; saveVillager(villager, tier, lastVanilla); }
                break;
            case FISHERMAN:
                lastVanilla = result == Material.FISHING_ROD;
                if (lastVanilla) { tier = 2; saveVillager(villager, tier, lastVanilla); }
                break;
            case FLETCHER:
                lastVanilla = result == Material.BOW;
                if (lastVanilla) { tier = 2; saveVillager(villager, tier, lastVanilla); }
                break;
            case LEATHERWORKER:
                lastVanilla = result == Material.SADDLE;
                if (lastVanilla) { tier = 3; saveVillager(villager, tier, lastVanilla); }
                break;
            case LIBRARIAN:
                lastVanilla = result == Material.NAME_TAG;
                if (lastVanilla) { tier = 6; saveVillager(villager, tier, lastVanilla); }
                break;
            case NITWIT:
                lastVanilla = true;
                tier = 1;
                saveVillager(villager, tier, lastVanilla);
                break;
            case SHEPHERD:
                lastVanilla = result == Material.BLACK_WOOL;
                if (lastVanilla) { tier = 2; saveVillager(villager, tier, lastVanilla); }
                break;
            case TOOL_SMITH:
                lastVanilla = result == Material.DIAMOND_PICKAXE;
                if (lastVanilla) { tier = 3; saveVillager(villager, tier, lastVanilla); }
                break;
            case WEAPON_SMITH:
                lastVanilla = result == Material.DIAMOND_AXE;
                if (lastVanilla) { tier = 3; saveVillager(villager, tier, lastVanilla); }
        }

        return lastVanilla;
    }

    boolean lastTradeInTier(Villager villager, MerchantRecipe recipe) {
        if (lastVanillaTrade(villager, recipe)) {
            return true;
        }
        Material result = recipe.getResult().getType();
        Material ingredient = recipe.getResult().getType();
        switch (villager.getCareer()) {
            case ARMORER:
                return result == Material.IRON_HELMET || result == Material.IRON_CHESTPLATE ||
                        result == Material.DIAMOND_CHESTPLATE;
            case BUTCHER:
                return ingredient == Material.CHICKEN;
            case CARTOGRAPHER:
                return result == Material.EMERALD || result == Material.MAP;
            case CLERIC:
                return ingredient == Material.GOLD_INGOT || result == Material.LAPIS_LAZULI ||
                        result == Material.ENDER_PEARL;
            case FARMER:
                return result == Material.BREAD || result == Material.PUMPKIN_PIE || result == Material.APPLE;
            case FISHERMAN:
                return ingredient == Material.COAL;
            case FLETCHER:
                return result == Material.ARROW;
            case LEATHERWORKER:
                return ingredient == Material.EMERALD;
            case LIBRARIAN:
                return result == Material.GLASS || result == Material.ENCHANTED_BOOK || result == Material.BOOKSHELF;
            case NITWIT:
                return true;
            case SHEPHERD:
                return result == Material.SHEARS;
            case TOOL_SMITH:
                return result == Material.IRON_SHOVEL || result == Material.IRON_PICKAXE;
            case WEAPON_SMITH:
                return result == Material.IRON_AXE || result == Material.IRON_SWORD;
        }

        return true;
    }

    int getTierOfTrade(Villager villager, MerchantRecipe recipe) {
        Material result = recipe.getResult().getType();
        Material ingredient = recipe.getResult().getType();
        switch (villager.getCareer()) {
            case ARMORER:
                switch (result) {
                    case IRON_HELMET: return 1;
                    case IRON_CHESTPLATE: return 2;
                    case DIAMOND_CHESTPLATE: return 3;
                }
                break;
            case BUTCHER:
                if (ingredient == Material.CHICKEN) { return 1; }
                break;
            case CARTOGRAPHER:
                switch (result) {
                    case EMERALD: return 1;
                    case MAP: return 2;
                }
                break;
            case CLERIC:
                if (ingredient == Material.GOLD_INGOT) {return 1;}
                else if (result == Material.LAPIS_LAZULI) {return 2;}
                else if (result == Material.ENDER_PEARL) {return 3;}
                break;
            case FARMER:
                switch (result) {
                    case BREAD: return 1;
                    case PUMPKIN_PIE: return 2;
                    case APPLE: return 3;
                }
                break;
            case FISHERMAN:
                if (ingredient == Material.COAL) {return 1;}
                break;
            case FLETCHER:
                if (result == Material.ARROW) {return 1;}
                break;
            case LEATHERWORKER:
                if (ingredient == Material.EMERALD) {return 1;}
                break;
            case LIBRARIAN:
                switch (result) {
                    case ENCHANTED_BOOK: return getLibrarianTier(villager, recipe) + 1;
                    case BOOKSHELF: return 2;
                    case GLASS: return 3;
                }
                break;
            case NITWIT:
                return 1;
            case SHEPHERD:
                if (result == Material.SHEARS) {return 1;}
                break;
            case TOOL_SMITH:
                switch (result) {
                    case IRON_SHOVEL: return 1;
                    case IRON_PICKAXE: return 2;
                }
                break;
            case WEAPON_SMITH:
                switch (result) {
                    case IRON_AXE: return 1;
                    case IRON_SWORD: return 2;
                }
        }

        return 1;
    }

    int getLibrarianTier(Villager villager, MerchantRecipe recipe) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (plugin.getVillagers().contains(villagerId) && plugin.getVillagers().isInt(villagerId + ".tier")) {
            return plugin.getVillagers().getInt(villagerId + ".tier");
        }
        else {
            switch (recipe.getResult().getType()) {
                case BOOKSHELF: return 2;
                case GLASS: return 3;
                default: return 1;
            }
        }
    }
}
