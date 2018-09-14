package com.alfalfascout.CustomVillageTrades;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

public class VillagerHelper {
    static CustomVillageTrades plugin;
    private static FileConfiguration villagers;
    
    public VillagerHelper(CustomVillageTrades instance) {
        plugin = instance;
        villagers = plugin.getVillagers();
    }

    // get villager's trade tier from file, else assume first tier
    public int getCareerTier(Villager villager) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (villagers.contains(villagerId) && villagers.isInt(villagerId + ".tier")) {
            return villagers.getInt(villagerId + ".tier");
        }
        else {
            saveVillager(villager, 1);
            return 1;
        }
    }

    // get villager's trade tier from recipe
    public int getCareerTier(Villager villager, MerchantRecipe recipe) {
        int tier = determineCareerTier(villager, recipe);
        saveVillager(villager, tier);
        return tier;
    }

    // determine villager's trade tier from recipe or file, if impossible assume first tier
    public int determineCareerTier(Villager villager, MerchantRecipe recipe) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (lastTradeInTier(villager, recipe)) {
            if(villagers.getBoolean(villagerId + ".lastvanilla")) {
                return villagers.getInt(villagerId + ".tier");
            }
            return getTierOfTrade(villager, recipe);
        }
        else if (villagers.contains(villagerId) && villagers.isInt(villagerId + ".tier")) {
            return villagers.getInt(villagerId + ".tier");
        }
        else {
            return 1;
        }
    }

    public static void saveVillager(Villager villager, int careerTier) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (!villagers.contains(villagerId)) {
            villagers.createSection(villagerId);
        }

        villagers.set(villagerId + ".tier", careerTier);
        plugin.saveVillagers();
    }

    public static void saveVillager(Villager villager, boolean lastVanilla) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (!villagers.contains(villagerId)) {
            villagers.createSection(villagerId);
        }

        villagers.set(villagerId + ".lastvanilla", lastVanilla);
        plugin.saveVillagers();
    }

    public static void saveVillager(Villager villager, int careerTier, boolean lastVanilla) {
        saveVillager(villager, careerTier);
        saveVillager(villager, lastVanilla);
    }

    public static boolean knownVillager(Villager villager) {
        String villagerId = "id" + villager.getUniqueId().toString();

        if (!(villagers.contains(villagerId))) {
            saveVillager(villager, 1);
            return false;
        }

        else {
            if (villagers.isInt(villagerId + ".tier") &&
                    villagers.isBoolean(villagerId + ".lastvanilla")) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    // Determines whether the trade passed in matches the last trade in the villager's vanilla trade list
    public boolean lastVanillaTrade(Villager villager, MerchantRecipe recipe) {
        Material result = recipe.getResult().getType();
        int tier = 0;
        boolean lastVanilla = false;

        switch (villager.getCareer()) {
            case ARMORER:
                lastVanilla = result == Material.CHAINMAIL_CHESTPLATE;
                if (lastVanilla) { tier = 4; saveVillager(villager, tier, lastVanilla); }
            case BUTCHER:
                lastVanilla = result == Material.COOKED_CHICKEN;
                if (lastVanilla) { tier = 2; saveVillager(villager, tier, lastVanilla); }
            case CARTOGRAPHER:
                lastVanilla = result == Material.FILLED_MAP ||
                        villagers.getInt(villager.getUniqueId() + ".tier") > 3;
                if (lastVanilla) { tier = 4; saveVillager(villager, tier, lastVanilla); }
            case CLERIC:
                lastVanilla = result == Material.EXPERIENCE_BOTTLE;
                if (lastVanilla) { tier = 4; saveVillager(villager, tier, lastVanilla); }
            case FARMER:
                lastVanilla = result == Material.CAKE;
                if (lastVanilla) { tier = 4; saveVillager(villager, tier, lastVanilla); }
            case FISHERMAN:
                lastVanilla = result == Material.FISHING_ROD;
                if (lastVanilla) { tier = 2; saveVillager(villager, tier, lastVanilla); }
            case FLETCHER:
                lastVanilla = result == Material.BOW;
                if (lastVanilla) { tier = 2; saveVillager(villager, tier, lastVanilla); }
            case LEATHERWORKER:
                lastVanilla = result == Material.SADDLE;
                if (lastVanilla) { tier = 3; saveVillager(villager, tier, lastVanilla); }
            case LIBRARIAN:
                lastVanilla = result == Material.NAME_TAG;
                if (lastVanilla) { tier = 6; saveVillager(villager, tier, lastVanilla); }
            case NITWIT:
                lastVanilla = true;
                tier = 4;
                saveVillager(villager, tier, lastVanilla);
            case SHEPHERD:
                lastVanilla = result == Material.BLACK_WOOL;
                if (lastVanilla) { tier = 2; saveVillager(villager, tier, lastVanilla); }
            case TOOL_SMITH:
                lastVanilla = result == Material.DIAMOND_PICKAXE;
                if (lastVanilla) { tier = 3; saveVillager(villager, tier, lastVanilla); }
            case WEAPON_SMITH:
                lastVanilla = result == Material.DIAMOND_AXE;
                if (lastVanilla) { tier = 3; saveVillager(villager, tier, lastVanilla); }
        }

        return lastVanilla;
    }

    public boolean lastTradeInTier(Villager villager, MerchantRecipe recipe) {
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

    public int getTierOfTrade(Villager villager, MerchantRecipe recipe) {
        Material result = recipe.getResult().getType();
        Material ingredient = recipe.getResult().getType();
        switch (villager.getCareer()) {
            case ARMORER:
                switch (result) {
                    case IRON_HELMET: return 1;
                    case IRON_CHESTPLATE: return 2;
                    case DIAMOND_CHESTPLATE: return 3;
                }
            case BUTCHER:
                if (ingredient == Material.CHICKEN) { return 1; }
            case CARTOGRAPHER:
                switch (result) {
                    case EMERALD: return 1;
                    case MAP: return 2;
                }
            case CLERIC:
                if (ingredient == Material.GOLD_INGOT) {return 1;}
                else if (result == Material.LAPIS_LAZULI) {return 2;}
                else if (result == Material.ENDER_PEARL) {return 3;}
            case FARMER:
                switch (result) {
                    case BREAD: return 1;
                    case PUMPKIN_PIE: return 2;
                    case APPLE: return 3;
                }
            case FISHERMAN:
                if (ingredient == Material.COAL) {return 1;}
            case FLETCHER:
                if (result == Material.ARROW) {return 1;}
            case LEATHERWORKER:
                if (ingredient == Material.EMERALD) {return 1;}
            case LIBRARIAN:
                switch (result) {
                    case ENCHANTED_BOOK: return getLibrarianTier(villager, recipe) + 1;
                    case BOOKSHELF: return 2;
                    case GLASS: return 3;
                }
            case NITWIT:
                return 1;
            case SHEPHERD:
                if (result == Material.SHEARS) {return 1;}
            case TOOL_SMITH:
                switch (result) {
                    case IRON_SHOVEL: return 1;
                    case IRON_PICKAXE: return 2;
                }
            case WEAPON_SMITH:
                switch (result) {
                    case IRON_AXE: return 1;
                    case IRON_SWORD: return 2;
                }
        }

        return 1;
    }

    public int getLibrarianTier(Villager villager, MerchantRecipe recipe) {
        String villagerId = "id" + villager.getUniqueId().toString();
        if (villagers.contains(villagerId) && villagers.isInt(villagerId + ".tier")) {
            return villagers.getInt(villagerId + ".tier");
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
