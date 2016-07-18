package com.alfalfascout.CustomVillageTrades;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
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
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomVillageTrades extends JavaPlugin implements Listener {
    static Random rand = new Random();
    private static FileConfiguration villagers;
    static Map<String,FileAndConfig> trees;
    FileConfiguration defaultBanner;
    MetaHelper metaHelper = new MetaHelper(this);
    
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("customvillagetrades").setExecutor(
                new CvtCommand(this));
        createFiles();
        getDefaultConfigs();
    }
    
    public void onDisable() {
        saveVillagers();
        saveConfig();
        for (String world : trees.keySet()) {
            getTree(world).save();
        }
    }
    
    public FileConfiguration getVillagers() {
        return CustomVillageTrades.villagers;
    }
    
    public void saveVillagers() {
        try {
            villagers.save(new File(getDataFolder(), "villagers.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // empty villagers file
    public void resetVillagers() {
        for (String key : villagers.getKeys(false)) {
            villagers.set(key, null);
        }
        saveVillagers();
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
    
    public void getDefaultConfigs() {
        populateTree(getTree("config"));
        if (!getConfig().contains("overwrite_unknown_villagers", true)) {
            getConfig().set("overwrite_unknown_villagers", false);
        }
        saveConfig();
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
        
        // default banner (not modifiable by the user)
        Reader bannerReader = getTextResource("default_banner.yml");
        defaultBanner = YamlConfiguration.loadConfiguration(bannerReader);
        
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
        
        if (!tree.conf.contains("currency", true)) {
            tree.conf.createSection("currency");
            tree.conf.set("currency", "emerald");
        }
        
        if (!tree.conf.contains("allow_vanilla_trades", true)) {
            tree.conf.createSection("allow_vanilla_trades");
            tree.conf.set("allow_vanilla_trades", "false");
        }
        tree.save();
    }
    
    // get the currency of the given world configuration
    public Material getCurrency(FileConfiguration f) {
        if (!f.contains("currency", true)) {
            f.set("currency", "emerald");
        }
        
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
        if (f.contains("allow_vanilla_trades") &&
                f.isBoolean("allow_vanilla_trades")) {
            vanillaTrades = f.getBoolean("allow_vanilla_trades");
        }
        else {
            f.set("allow_vanilla_trades", false);
        }
        
        return vanillaTrades;
    }
    
    // when the player opens an unmet villager inventory, overwrite the villager
    @EventHandler
    public void onOpenInventory(InventoryOpenEvent e) {
        final InventoryHolder holder = 
                e.getView().getTopInventory().getHolder();
        
        if (holder instanceof Villager) {
            // delay handling to allow new villagers to populate trade lists
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                    this, new Runnable() {
                public void run() {
                    try {
                        handleMetVillager((Villager) holder);
                    }
                    catch (ConcurrentModificationException e) {
                        e.printStackTrace();
                        getLogger().warning("Concurrent modification?");
                    }
                }
            }, 10);
        }
        
    }
    
    // when the villager replenishes their trades, give them new ones
    // if they're out of vanilla trades
    @EventHandler
    public void onTradeReplenish(VillagerReplenishTradeEvent e) {
        final Villager villager = e.getEntity();
        String villagerId = "id" + villager.getUniqueId().toString();
        long lastNew = villagers.getLong(villagerId + ".lastnew");
        
        if (villagers.getBoolean(villagerId + ".lastvanilla")) {
            if (lastNew + (long)2000 < System.currentTimeMillis()) {
                String world = villager.getEyeLocation().getWorld().getName();
                FileConfiguration file;
                if (trees.containsKey(world)) {
                    file = getTree(world).conf;
                }
                else {
                    return;
                }
                
                CareerTier career = new CareerTier(this);
                career.getLastCareerTier(villager);
                career.tier += 1;
                
                final List<MerchantRecipe> newTrades = 
                        new ArrayList<MerchantRecipe>();
                
                String tradePath = career.career + ".tier" + career.tier;
                newTrades.addAll(getTradesInTier(file, tradePath));
                tradePath = "all_villagers.tier" + career.tier;
                newTrades.addAll(getTradesInTier(file, tradePath));
                
                // delay adding trades to avoid concurrent modification
                if (newTrades.size() > 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(
                            this, new Runnable() {
                        public void run() {
                            try {
                                addRecipes(villager, newTrades);
                            }
                            catch (ConcurrentModificationException e) {
                                e.printStackTrace();
                                getLogger().warning("Concurrent modification?");
                            }
                        }
                    }, 5);
                }
                
                CareerTier.saveVillager(career, villager);
            }
        }
        
        villagers.set(villagerId + ".lastvanilla", true);
        villagers.set(villagerId + ".lastnew", System.currentTimeMillis());
        saveVillagers();
    }
    
    
    // when a villager gets a vanilla trade, find out what tier it is
    // and give them the trades from their world's tree
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
        
        String villagerId = "id" + villager.getUniqueId().toString();
        villagers.set(villagerId + ".lastnew", System.currentTimeMillis());
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
        List<MerchantRecipe> recipes = new ArrayList<MerchantRecipe>();
        
        // if this tier is marked default, get pseudo vanilla trades instead
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
            
            recipes.add(new MerchantRecipe(result, 7));
            
            for (ItemStack ingredient : ingredients) {
                recipes.get(recipes.size() - 1).addIngredient(ingredient);
            }
            
            tradeNum++;
        }
        
        return recipes;
    }
    
    // build individual ItemStack at the given path of the given world file
    public ItemStack getItemInTrade(FileConfiguration f, String path) {
        Material itemType = getItemType(f, path);
        ItemStack item = new ItemStack(itemType);
        
        if (f.contains(path + ".name")) {
            metaHelper.getItemName(f, item, path);
        }
        
        if (f.contains(path + ".lore")) {
            metaHelper.getItemLore(f, item, path);
        }
        
        // handle complex items like potions, spawn eggs, etc
        switch (itemType) {
            case MONSTER_EGG:
                metaHelper.handleSpawnEgg(f, item, path);
                break;
            case POTION:
            case SPLASH_POTION:
                metaHelper.handlePotion(f, item, path);
                break;
            case BANNER:
                metaHelper.handleBanner(f, item, path);
                break;
            default:
                break;
        }
        
        if (f.contains(path + ".min") &&
                f.contains(path + ".max")) {
            getItemAmount(f, item, path);
        }
        
        if (f.contains(path + ".data")) {
            getItemData(f, item, path);
        }
        
        if (f.contains(path + ".enchantment")) {
            metaHelper.handleEnchantment(f, item, path);
        }
        
        return item;
    }
    
    public Material getItemType(FileConfiguration f, String path) {
        if (!f.contains(path + ".material") ||
                !f.isString(path + ".material")) {
            getLogger().info(path + " needs a valid material.");
            f.set(path + ".material", "stone");
        }
        
        String materialName = f.getString(path + ".material");
        Material itemType;
        
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
        return itemType;
    }
    
    public ItemStack getItemAmount(FileConfiguration f, ItemStack item,
                                  String path) {
        if (!f.isInt(path + ".min") || !f.isInt(path + ".max")) {
            getLogger().info("Min and max should both be integers at " + path);
            return item;
        }
        
        int min = f.getInt(path + ".min");
        if (min < 0) {
            getLogger().warning("min must be greater than zero at " + path);
            min = 1;
        }
    
        int max = 1 + f.getInt(path + ".max") - min;
        if ((max + min) < min) {
            getLogger().warning("max must at least as great as min at " + path);
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
    
        return item;
    }
    
    public ItemStack getItemData(FileConfiguration f, ItemStack item,
                                  String path) {
        if (!f.isInt(path + ".data")) {
            getLogger().info(path + ".data should be a number.");
        }
        item.setDurability(
                (short)f.getInt(path + ".data"));
        
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
    
    // if we haven't met the villager, overwrite their trades
    public void handleMetVillager(Villager villager) {
        CareerTier villagerCareer = new CareerTier(this);
        
        // handle legacy configs?
        if (villagers.contains("id" +
                Integer.toString(villager.getEntityId()))) {
            villagerCareer.loadVillager(villager);
        }
        
        if (getConfig().getBoolean("overwrite_unknown_villagers") && 
                !villagers.contains("id" +
                        (villager).getUniqueId().toString())) {
            overwriteTrades(villager);
        }
    }
    
    // overwrites this villager with their first trade tier
    public void overwriteTrades(Villager villager) {
        CareerTier careerTier = new CareerTier(this);
        careerTier.tier = 1;
        
        if (villager.getProfession().equals(Profession.LIBRARIAN)) {
            careerTier.career = "librarian";
        }
        else if (villager.getProfession().equals(Profession.PRIEST)) {
            careerTier.career = "priest";
        }
        CareerTier.saveVillager(careerTier, villager);
        
        String world = villager.getEyeLocation().getWorld().getName();
        FileConfiguration file = getTree(world).conf;
        
        List<MerchantRecipe> trades = getTradesInTier(
                file, careerTier.career + ".tier1");
        
        if ((file.isString(careerTier.career) && 
                file.getString(careerTier.career).equals("default")) || 
                file.getBoolean("allow_vanilla_trades")) {
            List<MerchantRecipe> vanillaTrades = getTradesInTier(
                    getTree("vanilla").conf, careerTier.career + ".tier1");
            
            // correct the currency of the vanilla trades
            if (!getCurrency(file).equals(Material.EMERALD)) {
                for (MerchantRecipe vanillaTrade : vanillaTrades) {
                    MerchantRecipe newVanillaTrade = 
                            changeVanillaCurrency(file, vanillaTrade);
                    vanillaTrades.set(vanillaTrades.indexOf(vanillaTrade),
                            newVanillaTrade);
                }
            }
            trades.addAll(vanillaTrades);
        }
        trades.addAll(getTradesInTier(file, "all_villagers.tier1"));
        
        villager.setRecipes(trades);
    }
}
