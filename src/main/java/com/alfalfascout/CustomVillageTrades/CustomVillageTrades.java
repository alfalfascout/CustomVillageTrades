package com.alfalfascout.CustomVillageTrades;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random; 
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
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
    Material currency = Material.EMERALD;
    boolean vanilla_trades;
    private File configf, librariansf, vanillaf;
    private FileConfiguration config, librarians, vanilla;
    
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        createFiles();
        getDefaultConfigs();
        getLogger().info("Librarians: " +
                Boolean.toString(librarians.contains("id0")));
    }
    
    public void onDisable() {
        try {
            librarians.save(librariansf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            config.save(configf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            vanilla.save(vanillaf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public FileConfiguration getLibrarians() {
        return this.librarians;
    }
    
    public FileConfiguration getVanilla() {
        return this.vanilla;
    }
    
    public void createFiles() {
        File configf = new File(getDataFolder(), "config.yml");
        File librariansf = new File(getDataFolder(), "librarians.yml");
        File vanillaf = new File(getDataFolder(), "vanilla_trades.yml");
        if (!configf.exists()) {
            getLogger().info("config.yml not found, creating!");
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        if (!librariansf.exists()) {
            getLogger().info("librarians.yml not found, creating!");
            librariansf.getParentFile().mkdirs();
            saveResource("librarians.yml", false);
        }
        if (!vanillaf.exists()) {
            getLogger().info("vanilla_trades.yml not found, creating!");
            vanillaf.getParentFile().mkdirs();
            saveResource("vanilla_trades.yml", false);
        }
        
        YamlConfiguration config = new YamlConfiguration();
        YamlConfiguration librarians = new YamlConfiguration();
        YamlConfiguration vanilla = new YamlConfiguration();
        
        try {
            config.load(configf);
            librarians.load(librariansf);
            getLogger().info(librariansf.toString());
            vanilla.load(vanillaf);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    public void getDefaultConfigs() {
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
        }
        getLogger().info("Currency is " + currency.toString());
        
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
    }
    
    @EventHandler
    public void onTradeAcquire(VillagerAcquireTradeEvent e) {
        Villager villager = e.getEntity();
        MerchantRecipe recipe = e.getRecipe();
        
        CareerTier trade = new CareerTier(this);
        CareerTier.setCareerTier(trade, villager, recipe);
        
        if (trade.tier > 0) {
            String path = trade.career + ".tier" + 
                        Integer.toString(trade.tier);
            List<MerchantRecipe> new_trades = getTradesInTier(path);
            
            for (MerchantRecipe new_trade : new_trades) {
                addRecipe(villager, new_trade);
            }
        }
        
        e.setCancelled(!vanilla_trades);
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
        
        for (String new_trade : getConfig().getStringList(path)) {
            String trade_path = path + "." + new_trade;
            
            ItemStack result = new ItemStack(getItemInTrade(
                    trade_path + ".result"));
            List<ItemStack> ingredients = new ArrayList<ItemStack>();
            ingredients.add(new ItemStack(getItemInTrade(
                    trade_path + ".ingredient1")));
            if (getConfig().contains(trade_path + ".ingredient2")) {
                ingredients.add(new ItemStack(getItemInTrade(
                        trade_path + ".ingredient2")));
            }
            
            list.add(new MerchantRecipe(result, 7));
            
            for (ItemStack ingredient : ingredients) {
                list.get(list.size() - 1).addIngredient(ingredient);
            }
        }
        
        return list;
    }
    
    public ItemStack getItemInTrade(String path) {
        
        String material_name = getConfig().getString(path + ".material");
        Material item_type;
        if (material_name == "currency") {
            item_type = currency;
        }
        else {
            item_type = Material.matchMaterial(material_name);
            if (item_type == null) {
                item_type = Material.AIR;
                getLogger().warning("No material matching '" + 
                        material_name + "'. " + path);
            }
        }
        
        ItemStack item = new ItemStack(item_type);
        
        if (getConfig().contains(path + ".min") &&
                getConfig().contains(path + ".max")) {
            try {
                int min = getConfig().getInt(path + ".min");
                int max = getConfig().getInt(path + ".max") - min;
                item.setAmount(rand.nextInt(min) + max); 
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("The value in " + path +
                        ".min or .max should be an integer.");
            }
        }
        
        if (getConfig().contains(path + ".data")) {
            try {
                item.setDurability(
                        (short)getConfig().getInt(path + ".data"));
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("The value in " + path + 
                        ".data should be an integer.");
            }
            
        }
        
        if (getConfig().contains(path + ".enchantment")) {
            int level = 1;
            boolean allow_treasure = false;
            
            if (getConfig().contains(path + ".enchantment.level")) {
                try {
                    level = getConfig().getInt(path + ".enchantment.level");
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
                item = EnchantHelper.randomEnchantedBook(
                        level, allow_treasure);
            }
            else {
                item = EnchantHelper.randomEnchantment(
                        item, level, allow_treasure);
            }
        }
        return item;
    }
    
}
