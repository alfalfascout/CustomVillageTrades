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
    private static final Random rand = new Random();
    private static FileConfiguration villagers;
    static Map<String,FileAndConfig> trees;
    FileConfiguration defaultBanner, defaultBook;
    final EnchantHelper enchHelper = new EnchantHelper(this);
    final MetaHelper metaHelper = new MetaHelper(this);
    final VillagerHelper villagerHelper = new VillagerHelper(this);
    
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("customvillagetrades").setExecutor(new CvtCommand(this));
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
    
    FileConfiguration getVillagers() {
        return CustomVillageTrades.villagers;
    }
    
    void saveVillagers() {
        try {
            villagers.save(new File(getDataFolder(), "villagers.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // empty villagers file
    void resetVillagers() {
        for (String key : villagers.getKeys(false)) {
            villagers.set(key, null);
        }
        saveVillagers();
    }
    
    // gets the specified trade list or the default config if not found
    private FileAndConfig getTree(String file) {
        
        if (trees.containsKey(file)) {
            return trees.get(file);
        }
        else {
            return trees.get("config");
        }
    }
    
    void getDefaultConfigs() {
        populateTree(getTree("config"));
        if (!getConfig().contains("overwrite_unknown_villagers", true)) {
            getConfig().set("overwrite_unknown_villagers", false);
        }
        saveConfig();
    }
    
    // makes sure the plugin has all the config flies it needs
    void createFiles() {
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
        
        // default banner (not modifiable by the user)
        Reader bookReader = getTextResource("default_book.yml");
        defaultBook = YamlConfiguration.loadConfiguration(bookReader);
        
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
    void populateWorlds() {
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
    void loadTreesByWorld() {
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
    void populateTree(FileAndConfig tree) {
        if (!tree.conf.contains("currency", true)) {
            tree.conf.createSection("currency");
            tree.conf.set("currency", "emerald");
        }
        
        if (!tree.conf.contains("allow_vanilla_trades", true)) {
            tree.conf.createSection("allow_vanilla_trades");
            tree.conf.set("allow_vanilla_trades", "false");
        }
        
        if (!tree.conf.contains("override_vanilla_acquire", true)) {
            tree.conf.createSection("override_vanilla_acquire");
            tree.conf.set("override_vanilla_acquire", "false");
        }
        
        if (!tree.conf.contains("acquire", true)) {
            tree.conf.createSection("acquire");
            tree.conf.set("acquire", 7);
        }
        
        if (!tree.conf.contains("replenish", true)) {
            tree.conf.createSection("replenish");
            tree.conf.set("replenish", "default");
        }
        
        List<String> villagerList = Arrays.asList("armorer", "butcher", "cartographer",
                "cleric", "farmer", "fisherman", "fletcher", "leatherworker",
                "librarian", "shepherd", "tool_smith", "weapon_smith");
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
        
        tree.save();
    }
    
    // get the currency of the given world configuration
    Material getCurrency(FileConfiguration f) {
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
    boolean getAllowVanilla(FileConfiguration f) {
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
    public void onMeetVillager(InventoryOpenEvent e) {
        final InventoryHolder holder =
                e.getView().getTopInventory().getHolder();
        
        if (holder instanceof Villager) {
            // delay handling to allow new villagers to populate trade lists
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                    this, new Runnable() {
                public void run() {
                    try {
                        handleMeetVillager((Villager) holder);
                    }
                    catch (ConcurrentModificationException e) {
                        e.printStackTrace();
                        getLogger().warning("Concurrent modification?");
                    }
                }
            }, 5);
        }
        
    }
    
    // when the villager replenishes their trades, give them new ones if they're out of vanilla trades
    @EventHandler
    public void onTradeReplenish(VillagerReplenishTradeEvent e) {
        final Villager villager = e.getEntity();
        String villagerId = "id" + villager.getUniqueId().toString();
        String world = villager.getEyeLocation().getWorld().getName();
        FileConfiguration file = getTree(world).conf;
        long lastNew = villagers.getLong(villagerId + ".lastnew");
        int villagerTier;
        
        int replenish = rand.nextInt(6) + rand.nextInt(6) + 2;
        if (getIntOrMinMax(file, "replenish") != -1) {
            replenish = getIntOrMinMax(file, "replenish");
        }
        e.setBonus(replenish);
        
        if (villagers.getBoolean(villagerId + ".lastvanilla")) {
            if (lastNew + (long)2000 < System.currentTimeMillis()) {

                villagerTier = villagers.getInt(villagerId + ".tier") + 1;
                
                final List<MerchantRecipe> newTrades = 
                        new ArrayList<MerchantRecipe>();
                
                String tradePath = villager.getCareer().name().toLowerCase() + ".tier" + villagerTier;
                newTrades.addAll(getTradesInTier(file, tradePath, villager));
                tradePath = "all_villagers.tier" + villagerTier;
                newTrades.addAll(getTradesInTier(file, tradePath, villager));
                
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
                
                villagerHelper.saveVillager(villager, villagerTier);
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

        // this is a new trade, so it being the last vanilla trade is probably false
        villagerHelper.saveVillager(villager, false);
        
        if (trees.containsKey(world)) {
            FileConfiguration file = getTree(world).conf;
            handleTrade(file, e);
        }
        else if (getConfig().getString("worlds." + world).equals("none")) {
            e.setCancelled(true);
        }
    }
    
    // get trades the villager should have based on the trade it's acquiring
    void handleTrade(FileConfiguration f, VillagerAcquireTradeEvent e) {
        Villager villager = e.getEntity();
        MerchantRecipe recipe = e.getRecipe();
        String villagerId = "id" + villager.getUniqueId().toString();
        getLogger().info("recipe result: " + recipe.getResult().toString());

        int villagerTier;

        // get all the trades appropriate for villager's career and tier
        if (villagerHelper.lastTradeInTier(villager, recipe)) {

            // determine villager's career tier
            if (villagerHelper.knownVillager(villager)) {
                villagerTier = villagerHelper.getCareerTier(villager) + 1;
            }
            else {
                villagerTier =  villagerHelper.getCareerTier(villager, recipe);
            }
            getLogger().info("Got known tier " + villagerTier);

            // specific trades listed in world's trade file
            String path = villager.getCareer().name().toLowerCase() + ".tier" +
                        Integer.toString(villagerTier);

            getLogger().info("getting trades in villager tier " + path);
            List<MerchantRecipe> newTrades = getTradesInTier(f, path, villager);

            // pseudo vanilla trades if career's tree is set to "default"
            if (f.getString(villager.getCareer().name().toLowerCase()).equals("default") &&
                    !f.getBoolean("allow_vanilla_trades")) {
                List<MerchantRecipe> vanillaTrades = getTradesInTier(
                        getTree("vanilla").conf, path, villager);

                for (MerchantRecipe vanillaTrade : vanillaTrades) {

                    // correct the currency of the vanilla trades
                    if (!getCurrency(f).equals(Material.EMERALD)) {
                        MerchantRecipe newVanillaTrade = 
                                changeVanillaCurrency(f, vanillaTrade);
                        vanillaTrades.set(vanillaTrades.indexOf(vanillaTrade),
                                newVanillaTrade);
                    }

                    // set how many uses each trade will have
                    if (f.isBoolean("override_vanilla_acquire") && 
                            f.getBoolean("override_vanilla_acquire")) {
                        if (getIntOrMinMax(f, "acquire") != -1) {
                            vanillaTrade.setMaxUses(
                                    getIntOrMinMax(f, "acquire"));
                        }
                    }
                }
                newTrades.addAll(vanillaTrades);
            } // end pseudo vanilla trades
            
            // get trades given to all villagers of this tier
            path = "all_villagers.tier" + Integer.toString(villagerTier);
            if (f.contains(path)) {
                newTrades.addAll(getTradesInTier(f, path, villager));
            }
            
            // add all the above trades to the villager
            addRecipes(villager, newTrades);

            villagers.set(villagerId + ".tier", villagerTier);
        }

        // cancel vanilla trades if config forbids them
        if (!getAllowVanilla(f)) {
            e.setCancelled(true);
        }
        else {
            // if config allows real vanilla trades, update their currency to match config
            if (!getCurrency(f).equals(Material.EMERALD)) {
                recipe = changeVanillaCurrency(f, recipe);
                e.setRecipe(recipe);
            }
            // and change how many uses they have, if config overrides
            if (f.getBoolean("override_vanilla_acquire")) {
                recipe.setMaxUses(getUsesAcquired(f, "acquire"));
                e.setRecipe(recipe);
            }
        }

        villagers.set(villagerId + ".lastnew", System.currentTimeMillis());
        saveVillagers();
    }
    
    // add recipes to the end of the villager's recipe list
    void addRecipes(Villager villager, List<MerchantRecipe> recipes) {
        List<MerchantRecipe> newRecipes = new ArrayList<MerchantRecipe>();
        
        newRecipes.addAll(villager.getRecipes());
        newRecipes.addAll(recipes);
        
        villager.setRecipes(newRecipes);
    }
    
    // get all the trades in the given path (tier) of the given world file
    List<MerchantRecipe> getTradesInTier(
            FileConfiguration f, String path, Villager villager) {
        List<MerchantRecipe> recipes = new ArrayList<MerchantRecipe>();

        getLogger().info("getting trades for " + path);

        // if this tier is marked default, get pseudo vanilla trades instead
        if (f.isString(path) &&
                f.getString(path).equals("default")) {
            recipes = getTradesInTier(getTree("vanilla").conf, path, villager);
            if (f.isBoolean("override_vanilla_acquire") && 
                    f.getBoolean("override_vanilla_acquire")) {
                for (MerchantRecipe recipe : recipes) {
                    recipe.setUses(getUsesAcquired(f, "acquire"));
                }
            }
            return recipes;
        }
        
        int tradeNum = 1;
        
        // for every trade, get result and ingredients
        while (f.contains(path + ".trade" + 
                Integer.toString(tradeNum))) {
            String tradePath = path + ".trade" + Integer.toString(tradeNum);
            ItemStack result = new ItemStack(Material.DIRT);
            List<ItemStack> ingredients = new ArrayList<ItemStack>();
            
            if (f.contains(tradePath + ".result")) {
                result = new ItemStack(getItemInTrade(f, 
                        tradePath + ".result", villager));
            }
            else {
                getLogger().warning("Result missing. It's dirt now.");
            }
            
            if (f.contains(tradePath + ".ingredient1")) {
                
                if (f.getString(
                        tradePath + ".ingredient1").equals("auto")) {
                    ingredients.add(
                            enchHelper.appraiseEnchantedBook(f, result));
                }
                
                else {
                    ingredients.add(new ItemStack(getItemInTrade(f, 
                            tradePath + ".ingredient1", villager)));
                }
                
            }
            else {
                getLogger().warning("Main ingredient missing. It's stone now.");
                ingredients.add(new ItemStack(Material.STONE));
            }
            
            if (f.contains(tradePath + ".ingredient2")) {
                ingredients.add(new ItemStack(getItemInTrade(f, 
                        tradePath + ".ingredient2", villager)));
            }

            // assemble the recipe and add it to the recipe list
            recipes.add(new MerchantRecipe(result, 
                    getUsesAcquired(f, tradePath + ".acquire")));
            for (ItemStack ingredient : ingredients) {
                recipes.get(recipes.size() - 1).addIngredient(ingredient);
            }
            
            tradeNum++;
        }
        
        return recipes;
    }
    
    // Get max uses for new trade, either by custom or global value
    int getUsesAcquired(FileConfiguration f, String path) {
        if (getIntOrMinMax(f, path) != -1) {
            return getIntOrMinMax(f, path);
        }
        else {
            if (path.equals("acquire")) {
                return 7;
            }
            else {
                return getUsesAcquired(f, "acquire");
            }
        }
    }
    
    // Get int at path or random int between path.min & path.max as appropriate
    int getIntOrMinMax(FileConfiguration f, String path) {
        if (f.isInt(path) && f.getInt(path) > -1) {
            return f.getInt(path);
        }
        else if (f.isInt(path + ".min") && f.isInt(path + ".max")) {
            int min = f.getInt(path + ".min");
            int max = f.getInt(path + ".max");
            
            if (min > max || max < 1 || min < 0) {
                getLogger().warning("Invalid max/min values at " + 
                        path);
            }
            else {
                return rand.nextInt(1 + max - min) + min;
            }
        }
        return -1;
    }
    
    // build individual ItemStack at the given path of the given world file
    ItemStack getItemInTrade(FileConfiguration f, String path, Villager villager) {
        Material itemType = getItemType(f, path);
        ItemStack item = new ItemStack(itemType);

        getLogger().info("handling item: " + itemType);
        
        if (f.contains(path + ".name")) {
            metaHelper.getItemName(f, item, path);
        }

        if (itemType == Material.FILLED_MAP) {
            item =  metaHelper.handleFilledMap(f, item, path, villager.getEyeLocation());
        }
        
        if (f.contains(path + ".lore")) {
            metaHelper.getItemLore(f, item, path);
        }
        
        // handle complex items like potions, banners, etc
        switch (itemType) {
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
                metaHelper.handlePotion(f, item, path);
                break;
            case BLACK_BANNER:
            case BLUE_BANNER:
            case BROWN_BANNER:
            case CYAN_BANNER:
            case GRAY_BANNER:
            case GREEN_BANNER:
            case LIGHT_BLUE_BANNER:
            case LIGHT_GRAY_BANNER:
            case LIME_BANNER:
            case MAGENTA_BANNER:
            case ORANGE_BANNER:
            case PINK_BANNER:
            case PURPLE_BANNER:
            case RED_BANNER:
            case WHITE_BANNER:
            case YELLOW_BANNER:
                metaHelper.handleBanner(f, item, path);
                break;
            case PLAYER_HEAD:
                metaHelper.handleSkull(f, item, path);
                break;
            case WRITTEN_BOOK:
            case WRITABLE_BOOK:
                metaHelper.handleBook(f, item, path);
                break;
            case FIREWORK_STAR:
                metaHelper.handleFireworkStar(f, item, path);
                break;
            case FIREWORK_ROCKET:
                metaHelper.handleRocket(f, item, path);
                break;
            default:
                break;
        }
        
        if (f.contains(path + ".min") &&
                f.contains(path + ".max")) {
            getItemAmount(f, item, path);
        }
        
        if (f.contains(path + ".enchantment")) {
            getLogger().info("Calling metaHelper.handleEnchantment");
            metaHelper.handleEnchantment(f, item, path);
        }
        
        return item;
    }
    
    // Determine item's material
    Material getItemType(FileConfiguration f, String path) {
        if (!f.contains(path + ".material") ||
                !f.isString(path + ".material")) {
            getLogger().warning(path + " needs a valid material.");
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
    
    // Determine how many in item stack
    ItemStack getItemAmount(FileConfiguration f, ItemStack item,
                                    String path) {
        int amount = getIntOrMinMax(f, path);
        if (amount < 1) {
            amount = 1;
        }
        
        if (amount > item.getMaxStackSize()) {
            if (!f.getName().equals("vanilla")) {
                getLogger().warning("The maximum stack size for " +
                        item.getType().toString() + " is " +
                        Integer.toString(item.getMaxStackSize()));
            }
            amount = item.getMaxStackSize();
        }
        
        item.setAmount(amount);
        
        return item;
    }
    
    // given a recipe where the currency is emeralds,
    // change it to the currency listed in the config for that world
    MerchantRecipe changeVanillaCurrency(FileConfiguration f,
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
    void handleMeetVillager(Villager villager) {
        if (getConfig().getBoolean("overwrite_unknown_villagers") && 
                !villagerHelper.knownVillager(villager)) {
            overwriteTrades(villager);
        }
    }
    
    // overwrites this villager with their first trade tier
    void overwriteTrades(Villager villager) {
        String world = villager.getEyeLocation().getWorld().getName();
        FileConfiguration file = getTree(world).conf;
        
        List<MerchantRecipe> trades = getTradesInTier(
                file, villager.getCareer().name().toLowerCase() + ".tier1", villager);
        
        if ((file.isString(villager.getCareer().name().toLowerCase()) &&
                file.getString(villager.getCareer().name().toLowerCase()).equals("default")) ||
                file.getBoolean("allow_vanilla_trades")) {
            List<MerchantRecipe> vanillaTrades = getTradesInTier(
                    getTree("vanilla").conf, villager.getCareer().name().toLowerCase() + ".tier1", villager);
            
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
        trades.addAll(getTradesInTier(file, "all_villagers.tier1", villager));
        
        villager.setRecipes(trades);
    }


}
