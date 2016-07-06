package com.alfalfascout.CustomVillageTrades;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random; 
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomVillageTrades extends JavaPlugin implements Listener {
    static Random rand = new Random();
    private static FileConfiguration villagers;
    private static Map<String,FileAndConfig> trees;
    
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
        return CustomVillageTrades.villagers;
    }
    
    // gets either the vanilla trade list or the user's trade list
    public FileAndConfig getTree(String file) {
        getLogger().info("trees contains " + file + ": " +
                trees.containsKey(file));
        
        if (trees.containsKey(file)) {
            return trees.get(file);
        }
        else {
            return trees.get("config");
        }
    }
    
    // makes sure the plugin has all the config flies it needs
    public void createFiles() {
        trees = new HashMap<String,FileAndConfig>();
        
        File villagersFile =
                new File(getDataFolder(), "villagers.yml");
        
        if (!villagersFile.exists()) {
            villagersFile.getParentFile().mkdirs();
            saveResource("villagers.yml", true);
        }
        
        villagers = YamlConfiguration.loadConfiguration(villagersFile);
        
        Reader vanillaReader = getTextResource("vanilla_trades.yml");
        trees.put("vanilla", new FileAndConfig(vanillaReader));
        
        File configFile = new File(getDataFolder(), "config.yml");
        FileAndConfig cfc = new FileAndConfig(configFile, getConfig());
        trees.put("config", cfc);
        
        if (getConfig().contains("worlds")) {
            if (!getConfig().getString("worlds").equals("default")) {
                loadTreesByWorld();
            }
        }
    }
    
    // load all the trees for all the worlds specified in config.yml, into trees
    public void loadTreesByWorld() {
        ConfigurationSection worldsSection = 
                getConfig().getConfigurationSection("worlds");
        Map<String,Object> worlds = worldsSection.getValues(false);
        
        File treeFile = new File(getDataFolder(), "config.yml");
        for (String world : worlds.keySet()) {
            treeFile = new File(getDataFolder(), worlds.get(world).toString());
            if (treeFile.exists()) {
                trees.put(world, new FileAndConfig(treeFile));
            }
            else {
                getLogger().warning("Undefined file in config: " + 
                        treeFile.toString());
            }
        }
        getLogger().info(trees.toString());
    }
    
    
    public void getDefaultConfigs() {
        // make sure all the villager types are in the config. just in case
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
    
    public Material getCurrency(FileConfiguration f) {
        Material currency = Material.EMERALD;
        if (f.contains("currency")) {
            currency = Material.matchMaterial(
                    f.getString("currency"));
            if (currency == null) {
                getLogger().warning("No material found matching '" + 
                        f.getString("currency") + "' at currency. " +
                        "Using emerald.");
                currency = Material.EMERALD;
            }
            
            if (currency.getMaxStackSize() == 1) {
                getLogger().warning(currency.toString() + " can't stack. " +
                        "It might not be a good choice of currency.");
            }
        }
        else {
            getLogger().info("Config has no currency! Adding default.");
            f.createSection("currency");
            f.set("currency", "emerald");
        }
        return currency;
    }
    
    
    public boolean getAllowVanilla(FileConfiguration f) {
        // are vanilla trades allowed to be created? default false
        boolean vanillaTrades = false;
        if (f.contains("allow_vanilla_trades")) {
            try {
                vanillaTrades = f.getBoolean("allow_vanilla_trades");
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("Value at allow_vanilla_trades " +
                        "should be true or false.");
            }
        }
        else {
            f.createSection("allow_vanilla_trades");
            f.set("allow_vanilla_trades", "false");
        }
        return vanillaTrades;
    }
    
    
    @EventHandler
    public void onTradeAcquire(VillagerAcquireTradeEvent e) {
        Villager villager = e.getEntity();
        String location = villager.getEyeLocation().getWorld().getName();
        
        getLogger().info("New trade in " + location);
        
        if (trees.containsKey(location)) {
            FileConfiguration file = getTree(location).conf;
            handleTrade(file, e);
        }
        else if (!getConfig().contains("worlds")) {
            handleTrade(getConfig(), e);
        }
    }
        
    public void handleTrade(FileConfiguration f, VillagerAcquireTradeEvent e) {
        Villager villager = e.getEntity();
        MerchantRecipe recipe = e.getRecipe();
        CareerTier trade = new CareerTier(this);
        CareerTier.setCareerTier(trade, villager, recipe);
        
        if (trade.tier > 0) {
            String path = trade.career + ".tier" + 
                        Integer.toString(trade.tier);
            List<MerchantRecipe> newTrades = getTradesInTier(f, path);
            if (f.getString(trade.career).equals("default") && 
                    !f.getBoolean("allow_vanilla_trades")) {
                
                List<MerchantRecipe> vanillaTrades = getTradesInTier(
                        getTree("vanilla").conf, path);
                
                if (!getCurrency(f).equals(Material.EMERALD)) {
                    for (MerchantRecipe vanillaTrade : vanillaTrades) {
                        MerchantRecipe newVanillaTrade = 
                                changeVanillaCurrency(f, vanillaTrade);
                        vanillaTrades.set(vanillaTrades.indexOf(vanillaTrade),
                                newVanillaTrade);
                    }
                }
                newTrades.addAll(vanillaTrades);
            }
            
            path = "all_villagers.tier" + Integer.toString(trade.tier);
            if (f.contains(path)) {
                newTrades.addAll(getTradesInTier(f, path));
            }
            
            for (MerchantRecipe newTrade : newTrades) {
                addRecipe(villager, newTrade);
            }
        }
        if (!getAllowVanilla(f)) {
            e.setCancelled(!getAllowVanilla(f));
        }
        else if (!getCurrency(f).equals(Material.EMERALD)) {
            recipe = changeVanillaCurrency(f, recipe);
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
    
    public List<MerchantRecipe> getTradesInTier(
            FileConfiguration f, String path) {
        List<MerchantRecipe> list = new ArrayList<MerchantRecipe>();
        
        if (getTree("vanilla").conf == f) {
            
        }
        
        if (f.isString(path) &&
                f.getString(path).equals("default")) {
            f = getTree("vanilla").conf;
        }
        
        int tradeNum = 1;
        
        while (f.contains(path + ".trade" + 
                Integer.toString(tradeNum))) {
            String tradePath = path + ".trade" + Integer.toString(tradeNum);
            ItemStack result = new ItemStack(Material.DIRT);
            List<ItemStack> ingredients = new ArrayList<ItemStack>();
            
            if (f.contains(tradePath + ".result")) {
                result = new ItemStack(getItemInTrade(f, 
                        tradePath + ".result"));
            }
            else {
                getLogger().warning("Result missing. It's dirt now.");
            }
            
            if (f.contains(tradePath + ".ingredient1")) {
                
                if (f.getString(
                        tradePath + ".ingredient1").equals("auto")) {
                    ingredients.add(
                            EnchantHelper.appraiseEnchantedBook(this, f, result));
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
            
            if (f.contains(tradePath + ".ingredient2")) {
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
    
    public ItemStack getItemInTrade(FileConfiguration f, String path) {
        String materialName = f.getString(path + ".material");
        Material itemType;
        
        // get the item type
        if (materialName.equals("currency")) {
            itemType = getCurrency(f);
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
        if (f.contains(path + ".min") &&
                f.contains(path + ".max")) {
            try {
                int min = f.getInt(path + ".min");
                if (min < 0) {
                    getLogger().warning("min must be greater than zero.");
                    min = 1;
                }
                
                int max = 1 + f.getInt(path + ".max") - min;
                if ((max + min) < min) {
                    getLogger().warning("max must at least as great as min.");
                    max = 1;
                }
                
                int amount = rand.nextInt(max) + min;
                if (amount > item.getMaxStackSize()) {
                    if (!f.equals("vanilla")) {
                        getLogger().warning("The maximum stack size for " +
                                item.getType().toString() + " is " + 
                                Integer.toString(item.getMaxStackSize()));
                    }
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
        
        // get item name
        if (f.contains(path + ".name")) {
            ItemMeta meta = item.getItemMeta();
            try {
                meta.setDisplayName(f.getString(path + ".name"));
                item.setItemMeta(meta);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // get item lore
        if (f.contains(path + ".lore")) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = Arrays.asList(
                    f.getString(path + ".lore").split(","));
            try {
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // get any extra data/damage value
        if (f.contains(path + ".data")) {
            try {
                item.setDurability(
                        (short)f.getInt(path + ".data"));
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("The value in " + path + 
                        ".data should be an integer above zero.");
            }
            
        }
        
        // get enchantments
        if (f.contains(path + ".enchantment")) {
            
            // get user-specified enchantments
            if (f.contains(path + ".enchantment.enchant1")) {
                String enchantPath = path + ".enchantment.enchant1";
                int enchantNum = 1;
                
                while (f.contains(enchantPath)) {
                    int specLevel = 1;
                    Enchantment specType = Enchantment.DURABILITY;
                    
                    if (f.contains(enchantPath + ".type")) {
                        String typeString = f.getString(
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
                    
                    if (f.contains(enchantPath + ".level")) {
                        String levelString = f.getString(
                                enchantPath + ".level");
                        
                        if (levelString.equals("random")) {
                            specLevel = rand.nextInt(
                                    specType.getMaxLevel()) + 1;
                        }
                        else {
                            specLevel = f.getInt(
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
                
                if (f.contains(path + ".enchantment.level")) {
                    try {
                        level = f.getInt(path + ".enchantment.level");
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
            
                
                if (f.contains(path + ".enchantment.allow_treasure")) {
                    try {
                        allowTreasure = f.getBoolean(path
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
    
    public MerchantRecipe changeVanillaCurrency(FileConfiguration f,
            MerchantRecipe recipe) {
        ItemStack result = recipe.getResult();
        List<ItemStack> ingredients = recipe.getIngredients();
        
        if (result.getType().equals(Material.EMERALD)) {
            result.setType(getCurrency(f));
            
            if (result.getAmount() > result.getMaxStackSize()) {
                result.setAmount(result.getMaxStackSize());
            }
            
            recipe = new MerchantRecipe(result, 7);
        }
        
        
        for (ItemStack ingredient : ingredients) {
            if (ingredient.getType().equals(Material.EMERALD)) {
                ingredient.setType(getCurrency(f));
                
                if (ingredient.getAmount() > ingredient.getMaxStackSize()) {
                    ingredient.setAmount(ingredient.getMaxStackSize());
                }
            }
        }
        
        recipe.setIngredients(ingredients);
        
        return recipe;
    }
}
