package com.alfalfascout.CustomVillageTrades;

import org.bukkit.enchantments.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LeveledEnchantment extends EnchantmentWrapper {
    private static CustomVillageTrades plugin;
    private int level;
    private int weight;
    
    public LeveledEnchantment(CustomVillageTrades instance, String name, int level) {
        super(name);
        this.level = level;
        this.setWeight(this.getEnchantment().getKey().getKey());
        plugin = instance;
    }
    
    public int getLevel() {
        return this.level;
    }

    public int getWeight() {
        return this.weight;
    }

    private LeveledEnchantment setWeight(String name) {
        if (name.equals("PROTECTION_ENVIRONMENTAL") || name.equals("DAMAGE_ALL") || name.equals("DIG_SPEED")
            || name.equals("ARROW_DAMAGE")) {
            this.weight = 10;
        }
        if (name.equals("PROTECTION_FIRE") || name.equals("PROTECTION_FALL") || name.equals("PROTECTION_PROJECTILE") ||
                name.equals("DAMAGE_UNDEAD") || name.equals("DAMAGE_ARTHROPODS") || name.equals("KNOCKBACK") ||
                name.equals("DURABILITY")) {
            this.weight = 5;
        }
        if (name.equals("THORNS") || name.equals("SILK_TOUCH") || name.equals("ARROWS_INFINITE") || name.equals("BINDING_CURSE") ||
                name.equals("VANISHING_CURSE")) {
            this.weight = 1;
        }
        else {
            this.weight = 2;
        }
        return this;
    }
    
    // get one (weighted) random leveled enchantment from a list
    static LeveledEnchantment getRandomLeveledEnchant(
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
