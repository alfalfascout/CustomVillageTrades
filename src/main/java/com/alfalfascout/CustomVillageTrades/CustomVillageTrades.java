package com.alfalfascout.CustomVillageTrades;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random; 
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomVillageTrades extends JavaPlugin implements Listener {
    Random rand = new Random();
    Material currency;
    boolean vanilla_trades;
    private FileConfiguration config, villagers, vanilla;
    
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        createFiles();
        getDefaultConfigs();
    }
    
    public void onDisable() {
        try {
            villagers.save(new File(getDataFolder(), "villagers.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveConfig();
    }
    
    public FileConfiguration getVillagers() {
        return this.villagers;
    }
    
    public FileConfiguration getVanilla() {
        return this.vanilla;
    }
    
    public void createFiles() {
    	villagers = YamlConfiguration.loadConfiguration(
    			new File(getDataFolder(), "villagers.yml"));
    	vanilla = YamlConfiguration.loadConfiguration(
    			new File(getDataFolder(), "vanilla.yml"));
    }
    
    
    public void getDefaultConfigs() {
    	// get currency, default is emerald
        if (getConfig().contains("currency")) {
            currency = Material.matchMaterial(
                    getConfig().getString("currency"));
            if (currency == null) {
                getLogger().warning("No material found matching '" + 
                        getConfig().getString("currency") + "' at currency. " +
                        "Using default.");
                currency = Material.EMERALD;
            }
        }
        else {
            getLogger().info("Config has no currency! Adding default.");
            config.createSection("currency");
            config.set("currency", "emerald");
            saveConfig();
        }
        getLogger().info("Currency is " + currency.toString());
        
        // are vanilla trades allowed to be created? default false
        if (getConfig().contains("allow_vanilla_trades")) {
            try {
                vanilla_trades = getConfig().getBoolean("allow_vanilla_trades");
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("Value at allow_vanilla_trades " +
                        "should be true or false.");
            }
        }
        else {
        	getConfig().createSection("allow_vanilla_trades");
        	getConfig().set("allow_vanilla_trades", "false");
        }
        
        // make sure all the villager types are in the config
        List<String> villager_list = Arrays.asList("librarian", "cleric",
        		"farmer", "fletcher", "fisherman", "shepherd", "butcher",
        		"leatherworker", "armorer", "toolsmith", "weaponsmith");
        for (String villager_type : villager_list) {
        	if (!getConfig().contains(villager_type)) {
        		getConfig().createSection(villager_type);
        		getConfig().set(villager_type, "default");
        	}
        }
        saveConfig();
    }
    
    
    @EventHandler
    public void onTradeAcquire(VillagerAcquireTradeEvent e) {
        Villager villager = e.getEntity();
        MerchantRecipe recipe = e.getRecipe();
        
        getLogger().info("New trade event. Determining career tier.");
        
        CareerTier trade = new CareerTier(this);
        CareerTier.setCareerTier(trade, villager, recipe);
        
        if (trade.tier > 0) {
            String path = trade.career + ".tier" + 
                        Integer.toString(trade.tier);
            getLogger().info("Finding trades in tier " + path);
            List<MerchantRecipe> new_trades = getTradesInTier(path);
            
            for (MerchantRecipe new_trade : new_trades) {
                addRecipe(villager, new_trade);
            }
        }
        if (!vanilla_trades) {
        	getLogger().info("Cancelling vanilla trade event.");
        	e.setCancelled(!vanilla_trades);
        }
    }
        
    public void addRecipe(Villager villager, MerchantRecipe recipe) {
        List<MerchantRecipe> newrecipes = new ArrayList<MerchantRecipe>();
        for (int i = 0; i < villager.getRecipeCount(); i++) {
            newrecipes.add(villager.getRecipe(i));
        }
        newrecipes.add(recipe);
        villager.setRecipes(newrecipes);
    }
    
    public List<MerchantRecipe> getTradesInTier(String path) {
        List<MerchantRecipe> list = new ArrayList<MerchantRecipe>();
        
        int trade_num = 1;
        
        while (getConfig().contains(path + ".trade" + 
        		Integer.toString(trade_num))) {
            String trade_path = path + ".trade" + Integer.toString(trade_num);
            ItemStack result = new ItemStack(Material.DIRT);
            List<ItemStack> ingredients = new ArrayList<ItemStack>();
            
            getLogger().info("Getting trade in " + trade_path);
            
            getLogger().info("Getting result");
            if (getConfig().contains(trade_path + ".result")) {
            	result = new ItemStack(getItemInTrade(
            			trade_path + ".result"));
            }
            else {
            	getLogger().info("Result missing. It's dirt now.");
            }
            
            getLogger().info("Getting first ingredient");
            if (getConfig().contains(trade_path + ".ingredient1")) {
            	ingredients.add(new ItemStack(getItemInTrade(
            			trade_path + ".ingredient1"))); 
            }
            else {
            	getLogger().info("Main ingredient missing. It's stone now.");
            	ingredients.add(new ItemStack(Material.STONE));
            }
            
            if (getConfig().contains(trade_path + ".ingredient2")) {
            	getLogger().info("There's another ingredient too");
                ingredients.add(new ItemStack(getItemInTrade(
                        trade_path + ".ingredient2")));
            }
            
            getLogger().info("Building recipe.");
            list.add(new MerchantRecipe(result, 7));
            
            for (ItemStack ingredient : ingredients) {
                list.get(list.size() - 1).addIngredient(ingredient);
            }
            
            trade_num++;
        }
        
        return list;
    }
    
    public ItemStack getItemInTrade(String path) {
    	getLogger().info(path + ".material");
        String material_name = getConfig().getString(path + ".material");
        Material item_type;
        getLogger().info("Currency is " + currency.toString());
        
        // get the item type
        if (material_name.equals("currency")) {
            item_type = currency;
            getLogger().info("Material is " + currency.toString());
        }
        else {
            item_type = Material.matchMaterial(material_name);
            
            if (item_type == null) {
                item_type = Material.COBBLESTONE;
                getLogger().warning("No material matching '" + 
                        material_name + "'. It's cobbles now. " + path);
            }
            getLogger().info("Material is " + item_type.toString());
        }
        
        ItemStack item = new ItemStack(item_type);
        
        // get how many of the item there are
        if (getConfig().contains(path + ".min") &&
                getConfig().contains(path + ".max")) {
            try {
                int min = getConfig().getInt(path + ".min");
                if (min < 0) {
                	getLogger().warning("min must be greater than zero.");
                	min = 1;
                }
                
                int max = 1 + getConfig().getInt(path + ".max") - min;
                if (max < min) {
                	getLogger().warning("max must at least as great as min.");
                	max = 1;
                }
                
                int amount = rand.nextInt(max) + min;
                if (amount > item.getMaxStackSize()) {
                	getLogger().warning("The maximum stack size for " +
                			item.getType().toString() + " is " + 
                			Integer.toString(item.getMaxStackSize()));
                	amount = item.getMaxStackSize();
                }
                
                item.setAmount(amount); 
                getLogger().info("There are " + 
                		Integer.toString(item.getAmount()) + " of it.");
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("The value in " + path +
                        ".min or .max should be an integer between 1 and 64.");
            }
        }
        
        // get any extra data/damage value
        if (getConfig().contains(path + ".data")) {
            try {
                item.setDurability(
                        (short)getConfig().getInt(path + ".data"));
                getLogger().info("Data/dmg is " + 
                        Integer.toString(item.getDurability()));
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("The value in " + path + 
                        ".data should be an integer above zero.");
            }
            
        }
        
        if (getConfig().contains(path + ".enchantment")) {
            int level = 1;
            boolean allow_treasure = false;
            
            if (getConfig().contains(path + ".enchantment.level")) {
                try {
                    level = getConfig().getInt(path + ".enchantment.level");
                    if (level < 0) {
                    	getLogger().warning(
                    			"Enchantment level must be greater than zero.");
                    	level = 1;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    getLogger().warning("The value in " + path + 
                            ".enchantment.level should be an integer.");
                }
            }
            
            if (getConfig().contains(path + ".enchantment.allow_treasure")) {
                try {
                    allow_treasure = getConfig().getBoolean(path
                            + ".enchantment.allow_treasure");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    getLogger().warning("The value in " + path + 
                            ".enchantment.allow_treasure should be " +
                            "true or false.");
                }
            }
            
            if (item.getType().equals(Material.ENCHANTED_BOOK) ||
                    item.getType().equals(Material.BOOK)) {
                item = EnchantHelper.randomEnchantedBook(this,
                        level, allow_treasure);
            }
            else {
                item = EnchantHelper.randomEnchantment(this, 
                        item, level, allow_treasure);
            }
        }
        return item;
    }
    
}
