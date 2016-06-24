package com.alfalfascout.CustomVillageTrades;

import org.bukkit.enchantments.*;
import java.util.List;
import java.util.Random;

public class LeveledEnchantment extends EnchantmentWrapper {
    static CustomVillageTrades plugin;
    private int level;
    private int weight;
    
    public LeveledEnchantment(CustomVillageTrades instance, int id, int level) {
        super(id);
        this.level = level;
        this.setWeight(id);
        plugin = instance;
    }
    
    public int getLevel() {
        return this.level;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int id) {
        switch (id) {
            case 0: case 16: case 32: case 48:
                this.weight = 10;
                break;
            case 1: case 2: case 4: case 17: case 18: case 19:
            case 34:
                this.weight = 5;
                break;
            case 3: case 5: case 6: case 8: case 9: case 20:
            case 21: case 35: case 49: case 50: case 61:
            case 62: case 70:
                this.weight = 2;
                break;
            case 7: case 33: case 51:
                this.weight = 1;
                break;
            default:
                this.weight = 2;
        }
    }
    
    public static LeveledEnchantment getRandomLeveledEnchant(
    		CustomVillageTrades instance, 
            List<LeveledEnchantment> list, Random rand) {
        LeveledEnchantment chosenEnchant = new LeveledEnchantment(instance, 0, 0);
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
        return (LeveledEnchantment)null;
    }
}
