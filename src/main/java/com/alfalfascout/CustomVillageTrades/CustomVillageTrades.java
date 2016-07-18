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
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
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
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class CustomVillageTrades extends JavaPlugin implements Listener {
    static Random rand = new Random();
    private static FileConfiguration villagers;
    static Map<String,FileAndConfig> trees;
    static FileConfiguration defaultBanner;
    
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
            getItemName(f, item, path);
        }
        
        if (f.contains(path + ".lore")) {
            getItemLore(f, item, path);
        }
        
        // handle complex items like potions, spawn eggs, etc
        switch (itemType) {
            case MONSTER_EGG:
                handleSpawnEgg(f, item, path);
                break;
            case POTION:
            case SPLASH_POTION:
                handlePotion(f, item, path);
                break;
            case BANNER:
                handleBanner(f, item, path);
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
            handleEnchantment(f, item, path);
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
    
    public ItemStack getItemName(FileConfiguration f, ItemStack item,
                                  String path) {
        ItemMeta meta = item.getItemMeta();
        
        if (f.isString(path + ".name")) {
            meta.setDisplayName(f.getString(path + ".name"));
            item.setItemMeta(meta);
        }
        else {
            getLogger().info(path + ".name should be a string.");
            meta.setDisplayName("a string");
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public ItemStack getItemLore(FileConfiguration f, ItemStack item,
                                  String path) {
        ItemMeta meta = item.getItemMeta();
        if (f.isString(path + ".lore")) {
            List<String> lore = Arrays.asList(
                    f.getString(path + ".lore").split(","));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        else {
            getLogger().info(path + ".lore should be a string.");
            meta.setLore(Arrays.asList("a string"));
            item.setItemMeta(meta);
        }
        return item;
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
    
    public ItemStack handleSpawnEgg(FileConfiguration f, ItemStack item,
                                    String path) {
        EntityType spawnEggType = EntityType.PIG;
        if (f.contains(path + ".spawns")) {
            try {
                spawnEggType = EntityType.valueOf(
                        f.getString(path + ".spawns").toUpperCase());
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
                getLogger().warning(f.getString(path + ".spawns") +
                        " is not a valid entity type for spawn eggs.");
                spawnEggType = EntityType.PIG;
            }
        }
    
        SpawnEgg spawnEgg = new SpawnEgg(spawnEggType);
        item = spawnEgg.toItemStack(1);
        return item;
    }
    
    public ItemStack handlePotion(FileConfiguration f, ItemStack item,
                                  String path) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        PotionType potionType = PotionType.WATER;
        boolean potionExtended = false;
        boolean potionUpgraded = false;
    
        if (f.contains(path + ".potion.type")) {
            try {
                potionType = PotionType.valueOf(
                        f.getString(path + ".potion.type").toUpperCase());
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
                getLogger().warning(f.getString(path + ".potion.type") +
                        " is not a valid potion type.");
            }
        }
    
        if (f.contains(path + ".potion.extended") &&
                potionType.isExtendable()) {
            try {
                potionExtended = f.getBoolean(path + ".potion.extended");
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("'extended' should be true or false.");
            }
        }
    
        if (f.contains(path + ".potion.upgraded") &&
                potionType.isUpgradeable()) {
            try {
                potionUpgraded = f.getBoolean(path + ".potion.upgraded");
            }
            catch (Exception e) {
                e.printStackTrace();
                getLogger().warning("'upgraded' should be true or false.");
            }
        }
    
        if ((potionExtended && potionUpgraded)) {
            getLogger().warning(
                    "Potion cannot be both extended and upgraded.");
            potionExtended = false;
            potionUpgraded = false;
        }
    
        PotionData potionData =
                new PotionData(potionType, potionExtended, potionUpgraded);
        meta.setBasePotionData(potionData);
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack handleBanner(FileConfiguration f, ItemStack item, 
            String path) {
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        if (f.contains(path + ".banner") && f.isString(path + ".banner")) {
            File bannerFile = new File(getDataFolder(),
                    f.getString(path + ".banner"));
            try {
                bannerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileConfiguration b = 
                    YamlConfiguration.loadConfiguration(bannerFile);
            b.setDefaults(defaultBanner);

            // base color
            if (!b.contains("base.color", true)) {
                b.set("base.color", "white");
                try {
                    b.save(bannerFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            DyeColor base = DyeColor.valueOf(
                    b.getString("base.color").toUpperCase());
            if (base == null) {
                getLogger().info("Invalid base color.");
                base = DyeColor.WHITE;
            }
            meta.setBaseColor(base);
            
            // patterns
            int patternNum = 1;
            String pPath = "pattern" + Integer.toString(patternNum);
            List<Pattern> patterns = new ArrayList<Pattern>();
            
            while (b.contains(pPath) && patternNum <= 6) {
                DyeColor pColor = DyeColor.WHITE;
                PatternType pType = PatternType.BORDER;
                
                if (b.contains(pPath + ".color") &&
                        b.isString(pPath + ".color")) {
                    pColor = DyeColor.valueOf(
                            b.getString(pPath + ".color").toUpperCase());
                    
                    if (pColor == null) {
                        getLogger().info("Invalid pattern color.");
                        pColor = DyeColor.WHITE;
                    }
                }
                
                if (b.contains(pPath + ".type") &&
                        b.isString(pPath + ".type")) {
                    pType = PatternType.valueOf(
                            b.getString(pPath + ".type").toUpperCase());
                    
                    if (pType == null) {
                        getLogger().info("Invalid pattern type.");
                        pType = PatternType.BORDER;
                    }
                }
                
                patterns.add(new Pattern(pColor, pType));
                
                patternNum++;
                pPath = "pattern" + Integer.toString(patternNum);
            }
            
            meta.setPatterns(patterns);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    public ItemStack handleEnchantment(FileConfiguration f, ItemStack item,
                                       String path) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                    getLogger().warning("The value in " + path +
                            ".enchantment.level should be an integer.");
                }
            }
    
    
            if (f.contains(path + ".enchantment.allow_treasure")) {
                try {
                    allowTreasure = f.getBoolean(path
                            + ".enchantment.allow_treasure");
                } catch (Exception e) {
                    e.printStackTrace();
                    getLogger().warning("The value in " + path +
                            ".enchantment.allow_treasure should be " +
                            "true or false.");
                }
            }
    
            if (item.getType().equals(Material.ENCHANTED_BOOK) ||
                    item.getType().equals(Material.BOOK)) {
                item = EnchantHelper.randomlyEnchantBook(this, item,
                        allowTreasure);
            } else {
                item = EnchantHelper.randomlyEnchant(this,
                        item, level, allowTreasure);
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
