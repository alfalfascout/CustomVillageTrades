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
    
    public FileConfiguration getInfo(String file) {
    	if (file.equals("vanilla")) {
    		return getVanilla();
    	}
    	else {
    		return getConfig();
    	}
    }
    
    public void createFiles() {
    	villagers = YamlConfiguration.loadConfiguration(
    			new File(getDataFolder(), "villagers.yml"));
    	vanilla = YamlConfiguration.loadConfiguration(
    			getTextResource("vanilla_trades.yml"));
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
        	if (!getConfig().contains(villager_type, true)) {
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
        
        CareerTier trade = new CareerTier(this);
        CareerTier.setCareerTier(trade, villager, recipe);
        
        if (trade.tier > 0) {
            String path = trade.career + ".tier" + 
                        Integer.toString(trade.tier);
            List<MerchantRecipe> new_trades = getTradesInTier("config", path);
            if (getConfig().getString(trade.career).equals("default")) {
            	List<MerchantRecipe> vanilla_trades = 
            			getTradesInTier("vanilla", path);
            	new_trades.addAll(vanilla_trades);
            }
            
            for (MerchantRecipe new_trade : new_trades) {
                addRecipe(villager, new_trade);
            }
        }
        if (!vanilla_trades) {
        	e.setCancelled(!vanilla_trades);
        }
        else if (!currency.equals(Material.EMERALD)) {
        	recipe = changeVanillaCurrency(recipe);
        	e.setRecipe(recipe);
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
    
    public List<MerchantRecipe> getTradesInTier(String f, String path) {
        List<MerchantRecipe> list = new ArrayList<MerchantRecipe>();
        
        if (getConfig().isString(path) &&
        		getConfig().getString(path).equals("default")) {
        	f = "vanilla";
        }
        
        int trade_num = 1;
        
        while (getInfo(f).contains(path + ".trade" + 
        		Integer.toString(trade_num))) {
            String trade_path = path + ".trade" + Integer.toString(trade_num);
            ItemStack result = new ItemStack(Material.DIRT);
            List<ItemStack> ingredients = new ArrayList<ItemStack>();
            
            if (getInfo(f).contains(trade_path + ".result")) {
            	result = new ItemStack(getItemInTrade(f, 
            			trade_path + ".result"));
            }
            else {
            	getLogger().warning("Result missing. It's dirt now.");
            }
            
            if (getInfo(f).contains(trade_path + ".ingredient1")) {
            	
            	if (getInfo(f).getString(
            			trade_path + ".ingredient1").equals("auto")) {
            		ingredients.add(
            				EnchantHelper.appraiseEnchantedBook(this, result));
            	}
            	
            	else {
            		ingredients.add(new ItemStack(getItemInTrade(f, 
                			trade_path + ".ingredient1"))); 
            	}
            	
            }
            else {
            	getLogger().warning("Main ingredient missing. It's stone now.");
            	ingredients.add(new ItemStack(Material.STONE));
            }
            
            if (getInfo(f).contains(trade_path + ".ingredient2")) {
                ingredients.add(new ItemStack(getItemInTrade(f, 
                        trade_path + ".ingredient2")));
            }
            
            list.add(new MerchantRecipe(result, 7));
            
            for (ItemStack ingredient : ingredients) {
                list.get(list.size() - 1).addIngredient(ingredient);
            }
            
            trade_num++;
        }
        
        return list;
    }
    
    public ItemStack getItemInTrade(String f, String path) {
        String material_name = getInfo(f).getString(path + ".material");
        Material item_type;
        
        // get the item type
        if (material_name.equals("currency")) {
            item_type = currency;
        }
        else {
            item_type = Material.matchMaterial(material_name);
            
            if (item_type == null) {
                item_type = Material.COBBLESTONE;
                getLogger().warning("No material matching '" + 
                        material_name + "'. It's cobbles now. " + path);
            }
        }
        
        ItemStack item = new ItemStack(item_type);
        
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
            	String enchant_path = path + ".enchantment.enchant1";
            	int enchant_num = 1;
            	
            	while (getInfo(f).contains(enchant_path)) {
            		int spec_level = 1;
            		Enchantment spec_type = Enchantment.DURABILITY;
            		
            		if (getInfo(f).contains(enchant_path + ".type")) {
            			String typestring = getInfo(f).getString(
            					enchant_path + ".type").toUpperCase();
            			
            			if (typestring.equals("random")) {
            				spec_type = EnchantHelper.getRandomEnchantment(
            						this, item);
            			}
            			else {
                			spec_type = Enchantment.getByName(typestring);
            			}
            			
            			if (spec_type == null) {
            				getLogger().warning("No valid type: " + typestring);
            				spec_type = Enchantment.DURABILITY;
            			}
            		}
            		
            		if (getInfo(f).contains(enchant_path + ".level")) {
            			String levelstring = getInfo(f).getString(
            					enchant_path + ".level");
            			
            			if (levelstring.equals("random")) {
            				spec_level = rand.nextInt(
            						spec_type.getMaxLevel()) + 1;
            			}
            			else {
            				spec_level = getInfo(f).getInt(
                					enchant_path + ".level");
            			}
            		}
            		
            		LeveledEnchantment spec_enchant = new LeveledEnchantment(
            				this, spec_type.hashCode(), spec_level);
            		
            		if (spec_enchant.canEnchantItem(item)) {
            			EnchantHelper.applyEnchantment(
            					this, item, spec_enchant);
            		}
            		else {
            			getLogger().warning("The enchantment " + 
            					spec_enchant.toString() + 
            					" can't be applied to " + item.toString());
            		}
            		
            		enchant_num++;
            		enchant_path = (path + ".enchantment.enchant" + 
            				Integer.toString(enchant_num));
            	}
            }
            
            // OR generate random enchantments
            else {
            	int level = 1;
                boolean allow_treasure = false;
                
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
	                    allow_treasure = getInfo(f).getBoolean(path
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
	                		allow_treasure);
	            }
	            else {
	                item = EnchantHelper.randomlyEnchant(this, 
	                        item, level, allow_treasure);
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
