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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomVillageTrades extends JavaPlugin implements Listener {
    Random rand = new Random();
    static Material currency;
    boolean vanillaTrades;
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
    
    // gets either the vanilla trade list or the user's trade list
    public FileConfiguration getInfo(String file) {
    	if (file.equals("vanilla")) {
    		return getVanilla();
    	}
    	else {
    		return getConfig();
    	}
    }
    
    // makes sure the plugin has all the config flies it needs
    public void createFiles() {
    	File villagersFile =
    			new File(getDataFolder(), "villagers.yml");
    	
    	if (!villagersFile.exists()) {
    		villagersFile.getParentFile().mkdirs();
    		saveResource("villagers.yml", true);
    	}
    	
    	villagers = YamlConfiguration.loadConfiguration(villagersFile);
    	
    	vanilla = YamlConfiguration.loadConfiguration(
    			getTextResource("vanillaTrades.yml"));
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
                vanillaTrades = getConfig().getBoolean("allow_vanilla_trades");
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
        List<String> villagerList = Arrays.asList("librarian", "cleric",
        		"farmer", "fletcher", "fisherman", "shepherd", "butcher",
        		"leatherworker", "armorer", "toolsmith", "weaponsmith");
        for (String villagerType : villagerList) {
        	if (!getConfig().contains(villagerType, true)) {
        		getConfig().createSection(villagerType);
        		getConfig().set(villagerType, "default");
        	}
        }
        if (!getConfig().contains("all_villagers", true)) {
        	getConfig().createSection("all_villagers");
        	getConfig().set("all_villagers", "none");
        }
        saveConfig();
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
            List<MerchantRecipe> newTrades = getTradesInTier("config", path);
            if (getConfig().getString(trade.career).equals("default")) {
            	newTrades.addAll(getTradesInTier("vanilla", path));
            }
            
            path = "all_villagers.tier" + Integer.toString(trade.tier);
            if (getConfig().contains(path)) {
            	newTrades.addAll(getTradesInTier("config", path));
            }
            
            for (MerchantRecipe newTrade : newTrades) {
                addRecipe(villager, newTrade);
            }
        }
        if (!vanillaTrades) {
        	e.setCancelled(!vanillaTrades);
        }
        else if (!currency.equals(Material.EMERALD)) {
        	recipe = changeVanillaCurrency(recipe);
        	e.setRecipe(recipe);
        }
    }
        
    public void addRecipe(Villager villager, MerchantRecipe recipe) {
        List<MerchantRecipe> newRecipes = new ArrayList<MerchantRecipe>();
        for (int i = 0; i < villager.getRecipeCount(); i++) {
            newRecipes.add(villager.getRecipe(i));
        }
        newRecipes.add(recipe);
        villager.setRecipes(newRecipes);
    }
    
    public List<MerchantRecipe> getTradesInTier(String f, String path) {
        List<MerchantRecipe> list = new ArrayList<MerchantRecipe>();
        
        if (getConfig().isString(path) &&
        		getConfig().getString(path).equals("default")) {
        	f = "vanilla";
        }
        
        int tradeNum = 1;
        
        while (getInfo(f).contains(path + ".trade" + 
        		Integer.toString(tradeNum))) {
            String tradePath = path + ".trade" + Integer.toString(tradeNum);
            ItemStack result = new ItemStack(Material.DIRT);
            List<ItemStack> ingredients = new ArrayList<ItemStack>();
            
            if (getInfo(f).contains(tradePath + ".result")) {
            	result = new ItemStack(getItemInTrade(f, 
            			tradePath + ".result"));
            }
            else {
            	getLogger().warning("Result missing. It's dirt now.");
            }
            
            if (getInfo(f).contains(tradePath + ".ingredient1")) {
            	
            	if (getInfo(f).getString(
            			tradePath + ".ingredient1").equals("auto")) {
            		ingredients.add(
            				EnchantHelper.appraiseEnchantedBook(this, result));
            	}
            	
            	else {
            		ingredients.add(new ItemStack(getItemInTrade(f, 
                			tradePath + ".ingredient1")));
            	}
            	
            }
            else {
            	getLogger().warning("Main ingredient missing. It's stone now.");
            	ingredients.add(new ItemStack(Material.STONE));
            }
            
            if (getInfo(f).contains(tradePath + ".ingredient2")) {
                ingredients.add(new ItemStack(getItemInTrade(f, 
                        tradePath + ".ingredient2")));
            }
            
            list.add(new MerchantRecipe(result, 7));
            
            for (ItemStack ingredient : ingredients) {
                list.get(list.size() - 1).addIngredient(ingredient);
            }
            
            tradeNum++;
        }
        
        return list;
    }
    
    public ItemStack getItemInTrade(String f, String path) {
        String materialName = getInfo(f).getString(path + ".material");
        Material itemType;
        
        // get the item type
        if (materialName.equals("currency")) {
            itemType = currency;
        }
        else {
            itemType = Material.matchMaterial(materialName);
            
            if (itemType == null) {
                itemType = Material.COBBLESTONE;
                getLogger().warning("No material matching '" + 
                        materialName + "'. It's cobbles now. " + path);
            }
        }
        
        ItemStack item = new ItemStack(itemType);
        
        // get how many of the item there are
        if (getInfo(f).contains(path + ".min") &&
                getInfo(f).contains(path + ".max")) {
            try {
                int min = getInfo(f).getInt(path + ".min");
                if (min < 0) {
                	getLogger().warning("min must be greater than zero.");
                	min = 1;
                }
                
                int max = 1 + getInfo(f).getInt(path + ".max") - min;
                if ((max + min) < min) {
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
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("The value in " + path +
                        ".min or .max should be an integer between 1 and 64.");
            }
        }
        
        // get any extra data/damage value
        if (getInfo(f).contains(path + ".data")) {
            try {
                item.setDurability(
                        (short)getInfo(f).getInt(path + ".data"));
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("The value in " + path + 
                        ".data should be an integer above zero.");
            }
            
        }
        
        // get enchantments
        if (getInfo(f).contains(path + ".enchantment")) {
            
            // get user-specified enchantments
            if (getInfo(f).contains(path + ".enchantment.enchant1")) {
            	String enchantPath = path + ".enchantment.enchant1";
            	int enchantNum = 1;
            	
            	while (getInfo(f).contains(enchantPath)) {
            		int specLevel = 1;
            		Enchantment specType = Enchantment.DURABILITY;
            		
            		if (getInfo(f).contains(enchantPath + ".type")) {
            			String typeString = getInfo(f).getString(
            					enchantPath + ".type").toUpperCase();
            			
            			if (typeString.equals("random")) {
            				specType = EnchantHelper.getRandomEnchantment(
            						this, item);
            			}
            			else {
                			specType = Enchantment.getByName(typeString);
            			}
            			
            			if (specType == null) {
            				getLogger().warning("No valid type: " + typeString);
            				specType = Enchantment.DURABILITY;
            			}
            		}
            		
            		if (getInfo(f).contains(enchantPath + ".level")) {
            			String levelString = getInfo(f).getString(
            					enchantPath + ".level");
            			
            			if (levelString.equals("random")) {
            				specLevel = rand.nextInt(
            						specType.getMaxLevel()) + 1;
            			}
            			else {
            				specLevel = getInfo(f).getInt(
                					enchantPath + ".level");
            			}
            		}
            		
            		LeveledEnchantment specEnchant = new LeveledEnchantment(
            				this, specType.hashCode(), specLevel);
            		
            		if (specEnchant.canEnchantItem(item)) {
            			EnchantHelper.applyEnchantment(
            					this, item, specEnchant);
            		}
            		else {
            			getLogger().warning("The enchantment " + 
            					specEnchant.toString() +
            					" can't be applied to " + item.toString());
            		}
            		
            		enchantNum++;
            		enchantPath = (path + ".enchantment.enchant" +
            				Integer.toString(enchantNum));
            	}
            }
            
            // OR generate random enchantments
            else {
            	int level = 1;
                boolean allowTreasure = false;
                
            	if (getInfo(f).contains(path + ".enchantment.level")) {
            		try {
            			level = getInfo(f).getInt(path + ".enchantment.level");
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
            
	            
	            if (getInfo(f).contains(path + ".enchantment.allow_treasure")) {
	                try {
	                    allowTreasure = getInfo(f).getBoolean(path
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
	                item = EnchantHelper.randomlyEnchantBook(this, 
	                		allowTreasure);
	            }
	            else {
	                item = EnchantHelper.randomlyEnchant(this, 
	                        item, level, allowTreasure);
	            }
            }
        }
        return item;
    }
    
    public static MerchantRecipe changeVanillaCurrency(MerchantRecipe recipe) {
		ItemStack result = recipe.getResult();
		List<ItemStack> ingredients = recipe.getIngredients();
		
    	if (result.getType().equals(Material.EMERALD)) {
    		result.setType(currency);
    		recipe = new MerchantRecipe(result, 7);
    	}
    	
    	
    	for (ItemStack ingredient : ingredients) {
    		if (ingredient.getType().equals(Material.EMERALD)) {
    			ingredient.setType(currency);
    		}
    	}
    	
    	recipe.setIngredients(ingredients);
    	
    	return recipe;
    }
}
