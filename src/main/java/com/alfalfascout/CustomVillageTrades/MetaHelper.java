package com.alfalfascout.CustomVillageTrades;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

class MetaHelper {
    private static CustomVillageTrades plugin;
    private static EnchantHelper enchHelper;
    private static final Random rand = new Random();
    
    MetaHelper(CustomVillageTrades instance) {
        plugin = instance;
        enchHelper = plugin.enchHelper;
    }
    
    ItemStack getItemName(FileConfiguration f, ItemStack item,
                          String path) {
        ItemMeta meta = item.getItemMeta();
        
        if (f.isString(path + ".name")) {
            meta.setDisplayName(f.getString(path + ".name"));
            item.setItemMeta(meta);
        }
        else {
            plugin.getLogger().warning(path + ".name should be a string.");
            meta.setDisplayName("a string");
            item.setItemMeta(meta);
        }
        return item;
    }
    
    ItemStack getItemLore(FileConfiguration f, ItemStack item,
                                  String path) {
        ItemMeta meta = item.getItemMeta();
        if (f.isString(path + ".lore")) {
            List<String> lore = Arrays.asList(
                    f.getString(path + ".lore").split(","));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        else {
            plugin.getLogger().warning(path + ".lore should be a string.");
            meta.setLore(Arrays.asList("a string"));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    ItemStack handlePotion(FileConfiguration f, ItemStack item,
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
                plugin.getLogger().warning(f.getString(path + ".potion.type") +
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
                plugin.getLogger().warning(
                        "'extended' should be true or false.");
            }
        }
    
        if (f.contains(path + ".potion.upgraded") &&
                potionType.isUpgradeable()) {
            try {
                potionUpgraded = f.getBoolean(path + ".potion.upgraded");
            }
            catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().warning(
                        "'upgraded' should be true or false.");
            }
        }
    
        if ((potionExtended && potionUpgraded)) {
            plugin.getLogger().warning(
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
    
    ItemStack handleBanner(FileConfiguration f, ItemStack item,
            String path) {
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        if (f.contains(path + ".banner") && f.isString(path + ".banner")) {
            File bannerFile = new File(plugin.getDataFolder(),
                    f.getString(path + ".banner"));
            try {
                bannerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileConfiguration b = 
                    YamlConfiguration.loadConfiguration(bannerFile);
            b.setDefaults(plugin.defaultBanner);
            
            // patterns
            int patternNum = 1;
            String pPath = "pattern" + Integer.toString(patternNum);
            List<Pattern> patterns = new ArrayList<>();
            
            while (b.contains(pPath) && patternNum <= 6) {
                DyeColor pColor = DyeColor.WHITE;
                PatternType pType = PatternType.BORDER;
                
                if (b.contains(pPath + ".color") &&
                        b.isString(pPath + ".color")) {
                    pColor = DyeColor.valueOf(
                            b.getString(pPath + ".color").toUpperCase());
                    
                    if (pColor == null) {
                        plugin.getLogger().info("Invalid pattern color.");
                        pColor = DyeColor.WHITE;
                    }
                }
                
                if (b.contains(pPath + ".type") &&
                        b.isString(pPath + ".type")) {
                    pType = PatternType.valueOf(
                            b.getString(pPath + ".type").toUpperCase());
                    
                    if (pType == null) {
                        plugin.getLogger().info("Invalid pattern type.");
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
    
    ItemStack handleSkull(FileConfiguration f, ItemStack item,
            String path) {
        if (f.contains(path + ".owner") && f.isString(path + ".owner")) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            
            meta.setOwningPlayer(f.getOfflinePlayer(path + ".owner"));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    ItemStack handleBook(FileConfiguration f, ItemStack item,
            String path) {
        BookMeta meta = (BookMeta) item.getItemMeta();
        if (f.contains(path + ".book") && f.isString(path + ".book")) {
            File bookFile = new File(plugin.getDataFolder(),
                    f.getString(path + ".book"));
            try {
                bookFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileConfiguration b = 
                    YamlConfiguration.loadConfiguration(bookFile);
            b.setDefaults(plugin.defaultBook);
            
            if (b.contains("author", true) && b.isString("author")) {
                meta.setAuthor(b.getString("author"));
            }
            
            if (b.contains("title", true) && b.isString("title")) {
                meta.setTitle(b.getString("title"));
            }
            
            if (b.contains("status", true) && b.isString("status")) {
                meta.setGeneration(Generation.valueOf(b.getString("status")));
            }
            
            if (b.contains("pages", true)) {
                List<String> pages = new ArrayList<>();
                int pageNum = 1;
                String pagePath = "pages.page" + Integer.toString(pageNum);
                
                while (b.contains(pagePath)) {
                    pages.add(b.getString(pagePath));
                    
                    pageNum++;
                    pagePath = "pages.page" + Integer.toString(pageNum);
                }
                
                meta.setPages(pages);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
     ItemStack handleRocket(FileConfiguration f, ItemStack item,
            String path) {
        FireworkMeta meta = (FireworkMeta) item.getItemMeta();
        if (f.contains(path + ".firework") && f.isString(path + ".firework")) {
            File fireworkFile = new File(plugin.getDataFolder(),
                    f.getString(path + ".firework"));
            try {
                fireworkFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileConfiguration b = 
                    YamlConfiguration.loadConfiguration(fireworkFile);
            
            int power = 1;
            List<FireworkEffect> effects = new ArrayList<>();
            
            if (b.contains("power") && b.isInt("power") &&
                    b.getInt("power") > -1) {
                power = b.getInt("power");
            }
            
            if (b.contains("effects")) {
                int effectNum = 1;
                String effectPath = "effects.effect" + 
                        Integer.toString(effectNum);
                while (b.contains(effectPath)) {
                    effects.add(buildFirework(
                            b.getConfigurationSection(effectPath)));
                    effectNum++;
                    effectPath = "effects.effect" + 
                            Integer.toString(effectNum);
                }
            }
            
            meta.addEffects(effects);
            meta.setPower(power);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
     ItemStack handleFireworkStar(FileConfiguration f, ItemStack item,
            String path) {
        FireworkEffectMeta meta = (FireworkEffectMeta) item.getItemMeta();
        if (f.contains(path + ".firework") && f.isString(path + ".firework")) {
            File fireworkFile = new File(plugin.getDataFolder(),
                    f.getString(path + ".firework"));
            try {
                fireworkFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileConfiguration b = 
                    YamlConfiguration.loadConfiguration(fireworkFile);
            
            meta.setEffect(buildFirework(b.getRoot()));
            
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private FireworkEffect buildFirework(ConfigurationSection f) {
        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        List<Color> colors = new ArrayList<>();
        List<Color> fades = new ArrayList<>();
        boolean flicker = false;
        boolean trail = false;
        
        if (f.contains("type") && f.isString("type")) {
            type = FireworkEffect.Type.valueOf(
                    f.getString("type").toUpperCase());
            
            if (type == null) {
                plugin.getLogger().warning("No such firework type as " + 
                        f.getString("type"));
                type = FireworkEffect.Type.BALL;
            }
        }
        
        if (f.contains("flicker") && f.isBoolean("flicker")) {
            flicker = f.getBoolean("flicker");
        }
        
        if (f.contains("trail") && f.isBoolean("trail")) {
            trail = f.getBoolean("trail");
        }
        
        if (f.contains("colors") && f.isList("colors")) {
            for (Object color : f.getList("colors")) {
                if (color instanceof Color) {
                    colors.add((Color) color);
                }
                else if (DyeColor.valueOf(
                        ((String) color).toUpperCase()) != null) {
                    colors.add(dyeToFirework(DyeColor.valueOf(
                            ((String) color).toUpperCase())));
                }
                else {
                    plugin.getLogger().info(
                            "Unknown color " + color.toString());
                }
            }
        }
        if (colors.isEmpty()) {
            colors.add(Color.WHITE);
            plugin.getLogger().info("empty color list");
        }
        
        if (f.contains("fade-colors") && f.isList("fade-colors")) {
            for (Object fade : f.getList("fade-colors")) {
                if (fade instanceof Color) {
                    fades.add((Color) fade);
                }
                else if (DyeColor.valueOf(
                        ((String) fade).toUpperCase()) != null) {
                    fades.add(dyeToFirework(DyeColor.valueOf(
                            ((String) fade).toUpperCase())));
                }
                else {
                    plugin.getLogger().info("Unknown color " + fade.toString());
                }
            }
        }
        
        FireworkEffect.Builder builder = FireworkEffect.builder().with(type);
        builder.flicker(flicker);
        builder.trail(trail);
        builder.withColor(colors);
        if (!fades.isEmpty()) {
            builder.withFade(fades);
        }
        
        return builder.build();
    }
    
     ItemStack handleEnchantment(FileConfiguration f, ItemStack item,
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
                        specType = enchHelper.getRandomEnchantment(item);
                    }
                    else {
                        specType = Enchantment.getByKey(new NamespacedKey(plugin, typeString));
                    }
                
                    if (specType == null) {
                        plugin.getLogger().warning("No valid type: " + 
                                typeString);
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
                        plugin, specType.getKey().getKey(), specLevel);
            
                if (specEnchant.canEnchantItem(item) ||
                        item.getType().equals(Material.ENCHANTED_BOOK)) {
                    enchHelper.applyEnchantment(item, specEnchant);
                }
                else {
                    plugin.getLogger().warning("The enchantment " +
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
                    plugin.getLogger().info("Got enchantment level: " + level);
                    if (level < 0) {
                        plugin.getLogger().warning(
                                "Enchantment level must be greater than zero.");
                        level = 1;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("The value in " + path +
                            ".enchantment.level should be an integer.");
                }
            }
            
            
            if (f.contains(path + ".enchantment.allow_treasure")) {
                try {
                    allowTreasure = f.getBoolean(path
                            + ".enchantment.allow_treasure");
                } catch (Exception e) {
                    e.printStackTrace();
                    plugin.getLogger().warning("The value in " + path +
                            ".enchantment.allow_treasure should be " +
                            "true or false.");
                }
            }
            
            if (item.getType().equals(Material.ENCHANTED_BOOK) ||
                    item.getType().equals(Material.BOOK)) {
                item.setType(Material.ENCHANTED_BOOK);
                item = enchHelper.randomlyEnchantBook(item, allowTreasure);
            } else {
                plugin.getLogger().info("calling enchHelper.randomlyEnchant");
                item = enchHelper.randomlyEnchant(item, level, allowTreasure);
            }
        }

        plugin.getLogger().info("Item has enchantments? " + item.getEnchantments().size());
        return item;
    }

     ItemStack handleFilledMap(FileConfiguration f, ItemStack mapItem, String path, Location villagerLocation) {
        String structureIconType;
        String structureType = "";

        // get the user's specified map type
        if (f.contains(path + ".type")) {
             structureType = f.getString(path + ".type");
        }
        List<String> validTypes = Arrays.asList("Buried_Treasure", "EndCity", "Fortress", "Mansion", "Mineshaft",
                "Monument", "Ocean_Ruin", "Shipwreck", "Stronghold", "Desert_Pyramid", "Igloo", "Jungle_Pyramid",
                "Swamp_Hut", "Village");
        for (String validType : validTypes) { // check if it's roughly equal to a valid type, and if so, correct the case
            if (structureType.equalsIgnoreCase(validType)) {
                structureType = validType;
                plugin.getLogger().info("Recognized structure type: " + structureType);
            }
        }
        if (!validTypes.contains(structureType)) { // if it's not a valid type, make it a valid type
            plugin.getLogger().warning("Unrecognized structure type '" + structureType + "' at " + path);
            structureType = "Buried_Treasure";
        }

        // get the user's specified map icon if available, or pick one based on the structure type
        if (f.contains(path + ".icon")) { structureIconType = f.getString(path + ".icon"); }
        else if (structureType.equals("Mansion")) { structureIconType = "MANSION"; }
        else if (structureType.equals("Monument")) { structureIconType = "MONUMENT"; }
        else { structureIconType = "RED_X"; }

        try {
            mapItem = (ItemStack) createExplorerMap(villagerLocation.getWorld(), villagerLocation, structureType, structureIconType);
        } catch (NoSuchMethodException | InstantiationException | ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        MapMeta mapMeta = (MapMeta) mapItem.getItemMeta();

        // get the user's specified map name, if available, or pick one based on structure type
        if (f.contains(path + ".name")) { mapMeta.setDisplayName(f.getString(path + ".name")); }
        else if (structureType.equals("Buried_Treasure")) { mapMeta.setDisplayName("Buried Treasure Map"); }
        else if (structureType.equals("Monument")) { mapMeta.setDisplayName("Ocean Explorer Map"); }
        else if (structureType.equals("Mansion")) { mapMeta.setDisplayName("Woodland Explorer Map"); }
        else { mapMeta.setDisplayName("Explorer Map"); }

        return mapItem;
    }


    // make a yml that represents a banner item
    void makeBannerFile(ItemStack bannerItem) {
        // if the item isn't a banner, don't bother
        if (!bannerItem.getType().toString().contains("BANNER")) {
            plugin.getLogger().info("That's not a banner.");
            return;
        }
        BannerMeta bannerMeta = (BannerMeta) bannerItem.getItemMeta();

        // make the next banner file we can in order starting from 1
        Integer bannerNo = 1;
        String bannerName = "banner" + bannerNo.toString() + ".yml";
        File bannerFile = new File(plugin.getDataFolder(), bannerName);
        try {
            while (!bannerFile.createNewFile()) {
                bannerNo++;
                bannerName = "banner" + bannerNo.toString() + ".yml";
                bannerFile = new File(plugin.getDataFolder(), bannerName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        FileConfiguration bannerConf = new YamlConfiguration();

        // write material to the file
        bannerConf.set("material", bannerItem.getType().name().toLowerCase());

        // write each pattern to the file
        int pNum = 1;
        String pPath = "pattern" + Integer.toString(pNum);
        for (Pattern pattern : bannerMeta.getPatterns()) {
            bannerConf.set(pPath + ".color", pattern.getColor().toString());
            bannerConf.set(pPath + ".type", pattern.getPattern().toString());
            pNum++;
            pPath = "pattern" + Integer.toString(pNum);
        }

        // save the file
        try {
            bannerConf.save(bannerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // make a yml that represents a book item
    void makeBookFile(ItemStack bookItem) {
        // if it's not a book, don't bother
        if (!(bookItem.getType().equals(Material.WRITTEN_BOOK) ||
                bookItem.getType().equals(Material.WRITABLE_BOOK))) {
            plugin.getLogger().info("That's not a book.");
            return;
        }
        BookMeta bookMeta = (BookMeta) bookItem.getItemMeta();

        // write the next book file we possibly can starting from 1
        Integer bookNo = 1;
        String bookName = "book" + bookNo.toString() + ".yml";
        File bookFile = new File(plugin.getDataFolder(), bookName);
        try {
            while (!bookFile.createNewFile()) {
                bookNo++;
                bookName = "book" + bookNo.toString() + ".yml";
                bookFile = new File(plugin.getDataFolder(), bookName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        FileConfiguration bookConf = new YamlConfiguration();

        // write author, title, generation data to file
        if (bookMeta.hasAuthor()) {
            bookConf.set("author", bookMeta.getAuthor());
        }
        
        if (bookMeta.hasTitle()) {
            bookConf.set("title", bookMeta.getTitle());
        }
        
        if (bookMeta.getGeneration() != null) {
            bookConf.set("status", bookMeta.getGeneration().name());
        }

        // write each book page to file
        if (bookMeta.hasPages()) {
            bookConf.createSection("pages");
            String page = "";
            String path = "";
            for (int i = 1; i <= bookMeta.getPageCount(); i++) {
                page = bookMeta.getPage(i);
                path = "pages.page" + Integer.toString(i);
                bookConf.set(path, page);
            }
        }

        // save the file
        try {
            bookConf.save(bookFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // make a yml that represents a firework item
    void makeFireworkFile(ItemStack fwItem) {
        if (!(fwItem.getType().equals(Material.FIREWORK_ROCKET) ||
                fwItem.getType().equals(Material.FIREWORK_STAR))) {
            plugin.getLogger().info("That's not a firework.");
            return;
        }

        // create the next firework file we possibly can, starting from 1
        Integer fwNo = 1;
        String fwName = "firework" + fwNo.toString() + ".yml";
        File fwFile = new File(plugin.getDataFolder(), fwName);
        try {
            while (!fwFile.createNewFile()) {
                fwNo++;
                fwName = "firework" + fwNo.toString() + ".yml";
                fwFile = new File(plugin.getDataFolder(), fwName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        FileConfiguration fwConf = new YamlConfiguration();

        // write rocket power and effects to file
        if (fwItem.getType().equals(Material.FIREWORK_ROCKET)) {
            FireworkMeta fwMeta = (FireworkMeta) fwItem.getItemMeta();
            fwConf.set("power", fwMeta.getPower());

            // write each effect to file
            List<FireworkEffect> fwEffects = fwMeta.getEffects();
            int i = 1;
            String path = "effects.effect" + Integer.toString(i);
            for (FireworkEffect effect : fwEffects) {
                recordFireworkEffect(fwConf, effect, path);
                i += 1;
                path = "effects.effect" + Integer.toString(i);
            }
        }
        else { // write star effects to file
            FireworkEffectMeta fwMeta =
                    (FireworkEffectMeta) fwItem.getItemMeta();
            recordFireworkEffect(fwConf, fwMeta.getEffect(), "");
        }

        // save file
        try {
            fwConf.save(fwFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // utility for recording firework effects
    private void recordFireworkEffect(FileConfiguration fwConf,
                                      FireworkEffect effect, String path) {
        if (path.equals("")) {
            fwConf.addDefaults(effect.serialize());
            fwConf.options().copyDefaults(true);
        }
        else {
            fwConf.set(path, effect.serialize());
        }
    }

    // get firework color from dye color. used to be necessary in 1.10, not sure if necessary nowadays
    private org.bukkit.Color dyeToFirework(DyeColor dye) {
        Color color = dye.getColor();
        if (dye.equals(DyeColor.BLACK)) {
            color = Color.fromRGB(30, 27, 27);
        }
        else if (dye.equals(DyeColor.RED)) {
            color = Color.fromRGB(179, 49, 44);
        }
        else if (dye.equals(DyeColor.GREEN)) {
            color = Color.fromRGB(59, 81, 26);
        }
        else if (dye.equals(DyeColor.BROWN)) {
            color = Color.fromRGB(81, 48, 26);
        }
        else if (dye.equals(DyeColor.BLUE)) {
            color = Color.fromRGB(37, 49, 146);
        }
        else if (dye.equals(DyeColor.PURPLE)) {
            color = Color.fromRGB(123, 47, 190);
        }
        else if (dye.equals(DyeColor.CYAN)) {
            color = Color.fromRGB(40, 118, 151);
        }
        else if (dye.equals(DyeColor.LIGHT_GRAY)) {
            color = Color.fromRGB(171, 171, 171);
        }
        else if (dye.equals(DyeColor.GRAY)) {
            color = Color.fromRGB(67, 67, 67);
        }
        else if (dye.equals(DyeColor.PINK)) {
            color = Color.fromRGB(216, 129, 152);
        }
        else if (dye.equals(DyeColor.LIME)) {
            color = Color.fromRGB(65, 205, 52);
        }
        else if (dye.equals(DyeColor.YELLOW)) {
            color = Color.fromRGB(222, 207, 42);
        }
        else if (dye.equals(DyeColor.LIGHT_BLUE)) {
            color = Color.fromRGB(102, 137, 211);
        }
        else if (dye.equals(DyeColor.MAGENTA)) {
            color = Color.fromRGB(195, 84, 205);
        }
        else if (dye.equals(DyeColor.ORANGE)) {
            color = Color.fromRGB(235, 136, 68);
        }
        else if (dye.equals(DyeColor.WHITE)) {
            color = Color.fromRGB(240, 240, 240);
        }
        return color;
    }

    // create an explorer map with a guesstimated icon based on map type
    private Object createExplorerMap(World world, Location loc, String structureType) throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (structureType.equals("Mansion")) { return createExplorerMap(world, loc, structureType, "MANSION"); }
        else if (structureType.equals("Monument")) { return createExplorerMap(world, loc, structureType, "MONUMENT"); }
        else { return createExplorerMap(world, loc, structureType, "RED_X"); }
    }

    // Code by Ugleh on spigotmc.org forums to search for nearby structures and generate map, partly modified to support more structure icons
    private Object createExplorerMap(World world, Location loc, String structureType, String structureIconType) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        Object structurePosition = getStructure(loc, structureType);
        int structureX = (int) getNMSClass("BlockPosition").getMethod("getX").invoke(structurePosition);
        int structureZ = (int) getNMSClass("BlockPosition").getMethod("getZ").invoke(structurePosition);

        Method getHandle = loc.getWorld().getClass().getMethod("getHandle");
        Object nmsWorld = getHandle.invoke(loc.getWorld());

        structureIconType = structureIconType.toUpperCase();
        if (!(Arrays.asList("PLAYER", "FRAME","RED_MARKER","BLUE_MARKER","TARGET_X","TARGET_POINT","PLAYER_OFF_LIMITS",
                "MANSION","MONUMENT","RED_X","BANNER_BLACK","BANNER_BLUE","BANNER_BROWN","BANNER_CYAN","BANNER_GRAY",
                "BANNER_GREEN","BANNER_LIGHT_BLUE","BANNER_LIGHT_GRAY","BANNER_LIME","BANNER_MAGENTA","BANNER_ORANGE",
                "BANNER_PINK","BANNER_PURPLE","BANNER_RED","BANNER_WHITE","BANNER_YELLOW").contains(structureIconType))) {
            plugin.getLogger().warning("Unrecognized map icon '" + structureIconType);
            structureIconType = "RED_X";
        }

        Object itemStack = getNMSClass("ItemWorldMap").getDeclaredMethod("a", new Class[] { getNMSClass("World"), int.class, int.class, byte.class, boolean.class, boolean.class }).invoke(getNMSClass("ItemWorldMap"), nmsWorld, structureX, structureZ, (byte)2, true, true);
        getNMSClass("ItemWorldMap").getDeclaredMethod("a", new Class[] { getNMSClass("World"), getNMSClass("ItemStack") }).invoke(getNMSClass("ItemWorldMap"), nmsWorld, itemStack);
        Object icon = getNMSClass("MapIcon$Type").getMethod("valueOf", new Class[] {String.class}).invoke(getNMSClass("MapIcon$Type"), structureIconType);
        getNMSClass("WorldMap").getMethod("a", new Class[] {getNMSClass("ItemStack"), getNMSClass("BlockPosition"),  String.class, getNMSClass("MapIcon$Type")}).invoke(getNMSClass("WorldMap"), itemStack, structurePosition, "+", icon);

        return getBukkitClass("inventory.CraftItemStack").getMethod("asBukkitCopy", new Class[] {getNMSClass("ItemStack")}).invoke(getBukkitClass("inventory.CraftItemStack"), itemStack);
    }




    private Object getStructure(Location l, String structure) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, InstantiationException {
        Method getHandle = l.getWorld().getClass().getMethod("getHandle");
        Object nmsWorld = getHandle.invoke(l.getWorld());
        Object blockPosition = nmsWorld.getClass().getMethod("a", new Class[] { String.class, getNMSClass("BlockPosition"), int.class, boolean.class }).invoke(nmsWorld, structure,getBlockPosition(l), 100,false);
        return blockPosition;
    }

    private Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    private Class<?> getBukkitClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    private Object getBlockPosition(Location loc) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
        Class<?> nmsBlockPosition = getNMSClass("BlockPosition");
        Object nmsBlockPositionInstance = nmsBlockPosition
                .getConstructor(new Class[] { Double.TYPE, Double.TYPE, Double.TYPE })
                .newInstance(new Object[] { loc.getX(), loc.getY(), loc.getZ() });
        return nmsBlockPositionInstance;
    }
    // end code by Ugleh, thanks Ugleh!!

}
