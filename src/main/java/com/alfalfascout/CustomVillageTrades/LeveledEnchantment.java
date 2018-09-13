package com.alfalfascout.CustomVillageTrades;

import org.bukkit.enchantments.*;
import java.util.List;
import java.util.Random;

public class LeveledEnchantment extends EnchantmentWrapper {
    static CustomVillageTrades plugin;
    private int level;
    private int weight;
    
    public LeveledEnchantment(CustomVillageTrades instance, String name, int level) {
        super(name);
        this.level = level;
        this.setWeight(this.getEnchantment().getKey().toString());
        plugin = instance;
    }
    
    public int getLevel() {
        return this.level;
    }

    public int getWeight() {
        return this.weight;
    }

    public LeveledEnchantment setWeight(String name) {
        if (name == "PROTECTION_ENVIRONMENTAL" || name == "DAMAGE_ALL" || name == "DIG_SPEED"
            || name == "ARROW_DAMAGE") {
            this.weight = 10;
        }
        if (name == "PROTECTION_FIRE" || name == "PROTECTION_FALL" || name == "PROTECTION_PROJECTILE" ||
                name == "DAMAGE_UNDEAD" || name == "DAMAGE_ARTHROPODS" || name == "KNOCKBACK" ||
                name == "DURABILITY") {
            this.weight = 5;
        }
        if (name == "THORNS" || name == "SILK_TOUCH" || name == "ARROWS_INFINITE" || name == "BINDING_CURSE" ||
                name == "VANISHING_CURSE") {
            this.weight = 1;
        }
        else {
            this.weight = 2;
        }
        return this;
    }
    
    // get one (weighted) random leveled enchantment from a list
    public static LeveledEnchantment getRandomLeveledEnchant(
            CustomVillageTrades instance, 
            List<LeveledEnchantment> list, Random rand) {
        LeveledEnchantment chosenEnchant;
        int totalWeight = 0;
        int i = 0;
        int j;
        
        for (j = list.size(); i < j; ++i) {
            LeveledEnchantment enchantment = list.get(i);
            totalWeight += enchantment.weight;
        }
        
        int k = rand.nextInt(totalWeight);
        i = 0;
        
        for (j = list.size(); i < j; ++i) {
            chosenEnchant = list.get(i);
            k -= chosenEnchant.weight;
            
            if (k < 0) {
                return chosenEnchant;
            }
        }
        return null;
    }
}
