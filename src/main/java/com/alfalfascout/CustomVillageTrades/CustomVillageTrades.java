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
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomVillageTrades extends JavaPlugin implements Listener {
    static Random rand = new Random();
    private static FileConfiguration villagers;
    static Map<String,FileAndConfig> trees;
    
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("customvillagetrades").setExecutor(
                new CvtCommand(this));
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
    
    // gets the specified trade list or the default config if not found
    public FileAndConfig getTree(String file) {
        
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
        
        // the file keeping track of villager career tiers
        File villagersFile =
                new File(getDataFolder(), "villagers.yml");
        if (!villagersFile.exists()) {
            villagersFile.getParentFile().mkdirs();
            saveResource("villagers.yml", true);
        }
        villagers = YamlConfiguration.loadConfiguration(villagersFile);
        
        // the file with some example trades
        File exampleFile = new File(getDataFolder(), "trades_example.yml");
        if (!exampleFile.exists()) {
            villagersFile.getParentFile().mkdirs();
            saveResource("trades_example.yml", true);
        }
        
        // vanilla trades (not modifiable by the user)
        Reader vanillaReader = getTextResource("vanilla_trades.yml");
        trees.put("vanilla", new FileAndConfig(vanillaReader));
        
        // main config file
        File configFile = new File(getDataFolder(), "config.yml");
        FileAndConfig cfc = new FileAndConfig(configFile, getConfig());
        trees.put("config", cfc);
        
        if (!getConfig().contains("worlds")) {
            getConfig().createSection("worlds");
        }
        populateWorlds();
        loadTreesByWorld();
    }
    
    // add all the worlds it can find to the config world tree
    public void populateWorlds() {
        List<World> allWorlds = this.getServer().getWorlds();
        for (World world : allWorlds) {
            String worldPath = "worlds." + world.getName();
            if (!getConfig().contains(worldPath, true)) {
                getConfig().createSection(worldPath);
                getConfig().set(worldPath, "config.yml");
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
            if (worlds.get(world).toString().equals("none")) {
                continue;
            }
            if (treeFile.exists()) {
                trees.put(world, new FileAndConfig(treeFile));
            }
            else {
                getLogger().info(treeFile.toString() + " is missing! " +
                        "Creating it now.");
                try {
                    treeFile.createNewFile();
                    trees.put(world, new FileAndConfig(treeFile));
                    populateTree(getTree(world));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void getDefaultConfigs() {
        populateTree(getTree("config"));
    }
    
    // make sure all villager types, currency, and vanilla bool are in the tree
    public void populateTree(FileAndConfig tree) {
        List<String> villagerList = Arrays.asList("librarian", "cleric",
                "farmer", "fletcher", "fisherman", "shepherd", "butcher",
                "leatherworker", "armorer", "toolsmith", "weaponsmith");
        for (String villagerType : villagerList) {
            if (!tree.conf.contains(villagerType, true)) {
                tree.conf.createSection(villagerType);
                tree.conf.set(villagerType, "default");
            }
        }
        if (!tree.conf.contains("all_villagers", true)) {
            tree.conf.createSection("all_villagers");
            tree.conf.set("all_villagers", "none");
        }
        
        if (!tree.conf.contains("currency")) {
            tree.conf.createSection("currency");
            tree.conf.set("currency", "emerald");
        }
        
        if (!tree.conf.contains("allow_vanilla_trades")) {
            tree.conf.createSection("allow_vanilla_trades");
            tree.conf.set("allow_vanilla_trades", "false");
        }
        tree.save();
    }
    
    // get the currency of the given world configuration
    public Material getCurrency(FileConfiguration f) {
        Material currency = Material.matchMaterial(
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
        
        return currency;
    }
    
    // get whether vanilla trades are allowed in the current world config
    public boolean getAllowVanilla(FileConfiguration f) {
        boolean vanillaTrades = false;
        
        try {
            vanillaTrades = f.getBoolean("allow_vanilla_trades");
        }
        catch (Exception e) {
            e.printStackTrace();
            getLogger().warning("Value at allow_vanilla_trades " +
                    "should be true or false.");
        }
        
        return vanillaTrades;
    }
    
    
    @EventHandler
    public void onTradeAcquire(VillagerAcquireTradeEvent e) {
        Villager villager = e.getEntity();
        String world = villager.getEyeLocation().getWorld().getName();
        
        if (trees.containsKey(world)) {
            FileConfiguration file = getTree(world).conf;
            handleTrade(file, e);
        }
        else if (!getConfig().contains("worlds")) { // for legacy configs??
            handleTrade(getConfig(), e);
        }
        else if (getConfig().getString("worlds." + world).equals("none")) {
            e.setCancelled(true);
        }
    }
    
    // get trades the villager should have based on the trade it's acquiring
    public void handleTrade(FileConfiguration f, VillagerAcquireTradeEvent e) {
        Villager villager = e.getEntity();
        MerchantRecipe recipe = e.getRecipe();
        
        // hacky way to get the villager's career until spigot adds that in
        CareerTier trade = new CareerTier(this);
        trade.setCareerTier(villager, recipe);
        
        // get all the trades appropriate for villager's career and tier
        if (trade.tier > 0) {
            // specific trades listed in world's trade file
            String path = trade.career + ".tier" + 
                        Integer.toString(trade.tier);
            List<MerchantRecipe> newTrades = getTradesInTier(f, path);
            
            // pseudo vanilla trades if career set to "default"
            if (f.getString(trade.career).equals("default") && 
                    !f.getBoolean("allow_vanilla_trades")) {
                List<MerchantRecipe> vanillaTrades = getTradesInTier(
                        getTree("vanilla").conf, path);
                
                // correct the currency of the vanilla trades
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
            
            // trades given to all villagers of this tier
            path = "all_villagers.tier" + Integer.toString(trade.tier);
            if (f.contains(path)) {
                newTrades.addAll(getTradesInTier(f, path));
            }
            
            // add all the above trades to the villager
            addRecipes(villager, newTrades);
        }
        
        if (!getAllowVanilla(f)) {
            e.setCancelled(true);
        }
        else if (!getCurrency(f).equals(Material.EMERALD)) {
            recipe = changeVanillaCurrency(f, recipe);
            e.setRecipe(recipe);
        }
    }
    
    // add recipes to the end of the villager's recipe list
    public void addRecipes(Villager villager, List<MerchantRecipe> recipes) {
        List<MerchantRecipe> newRecipes = new ArrayList<MerchantRecipe>();
        
        newRecipes.addAll(villager.getRecipes());
        newRecipes.addAll(recipes);
        
        villager.setRecipes(newRecipes);
    }
    
    // get all the trades in the given path (tier) of the given world file
    public List<MerchantRecipe> getTradesInTier(
            FileConfiguration f, String path) {
        List<MerchantRecipe> list = new ArrayList<MerchantRecipe>();
        
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
                            EnchantHelper.appraiseEnchantedBook(
                                    this, f, result));
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
    
    // build individual ItemStack at the given path of the given world file
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
        
        // handle spawn eggs
        if (itemType.equals(Material.MONSTER_EGG)) {
            EntityType spawnEggType = EntityType.PIG; 
            if (f.contains(path + ".spawns")) {
                try {
                    spawnEggType = EntityType.valueOf(
                            f.getString(path + ".spawns").toUpperCase());
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    getLogger().info(f.getString(path + ".spawns") + 
                            " is not a valid entity type for spawn eggs.");
                    spawnEggType = EntityType.PIG; 
                }
            }
            
            SpawnEgg spawnEgg = new SpawnEgg(spawnEggType);
            item = spawnEgg.toItemStack(1);
        }
        
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
                    
                    if (specEnchant.canEnchantItem(item) || 
                            item.getType().equals(Material.ENCHANTED_BOOK)) {
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
    
    // given a recipe where the currency is emeralds,
    // change it to the currency listed in the config for that world
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
