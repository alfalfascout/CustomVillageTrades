package com.alfalfascout.CustomVillageTrades;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class CareerTier {
	
	static CustomVillageTrades plugin;
	public String career;
	public int tier;
	
	public CareerTier(CustomVillageTrades instance) {
		this.career = "villager";
		this.tier = 0;
		plugin = instance;
	}
	
	// get villager's career and trade tier based on current trade
	public static CareerTier setCareerTier(CareerTier careertier,
			Villager villager, MerchantRecipe recipe) {
		ItemStack result = recipe.getResult();
		List<ItemStack> ingredients = recipe.getIngredients();
		
		// farmer
		if (result.getType().equals(Material.BREAD)) {
			careertier.career = "farmer";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.PUMPKIN_PIE)) {
			careertier.career = "farmer";
			careertier.tier = 2;
		}
		else if (result.getType().equals(Material.APPLE)) {
			careertier.career = "farmer";
			careertier.tier = 3;
		}
		else if (result.getType().equals(Material.COOKIE)) {
			careertier.career = "farmer";
			careertier.tier = 4;
		} // fisherman
		else if (result.getType().equals(Material.COOKED_FISH)) {
			careertier.career = "fisherman";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.FISHING_ROD)) {
			careertier.career = "fisherman";
			careertier.tier = 2;
		} // fletcher
		else if (result.getType().equals(Material.ARROW)) {
			careertier.career = "fletcher";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.BOW)) {
			careertier.career = "fletcher";
			careertier.tier = 2;
		} // shepherd
		else if (result.getType().equals(Material.SHEARS)) {
			careertier.career = "shepherd";
			careertier.tier = 1;
		}
		else if (result.equals(new ItemStack(Material.WOOL, 1, (short)1))) {
			careertier.career = "shepherd";
			careertier.tier = 2;
		} // librarian
		else if (ingredients.get(0).getType().equals(Material.PAPER)) {
			careertier.career = "librarian";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.BOOKSHELF)) {
			careertier.career = "librarian";
			careertier.tier = 2;
		}
		else if (result.getType().equals(Material.GLASS)) {
			careertier.career = "librarian";
			careertier.tier = 3;
		}
		else if (result.getType().equals(Material.NAME_TAG)) {
			careertier.career = "librarian";
			careertier.tier = 6;
		}
		else if (result.getType().equals(Material.ENCHANTED_BOOK)) {
			careertier.career = "librarian";
			careertier.tier = 0;
		}// butcher
		else if (ingredients.get(0).getType().equals(Material.RAW_CHICKEN)) {
			careertier.career = "butcher";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.COOKED_CHICKEN)) {
			careertier.career = "butcher";
			careertier.tier = 2;
		}// leatherworker
		else if (result.getType().equals(Material.LEATHER_LEGGINGS)) {
			careertier.career = "leatherworker";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.LEATHER_CHESTPLATE)) {
			careertier.career = "leatherworker";
			careertier.tier = 2;
		}
		else if (result.getType().equals(Material.SADDLE)) {
			careertier.career = "leatherworker";
			careertier.tier = 3;
		}// armorer
		else if (result.getType().equals(Material.IRON_HELMET)) {
			careertier.career = "armorer";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.IRON_CHESTPLATE)) {
			careertier.career = "armorer";
			careertier.tier = 2;
		}
		else if (result.getType().equals(Material.DIAMOND_CHESTPLATE)) {
			careertier.career = "armorer";
			careertier.tier = 3;
		}
		else if (result.getType().equals(Material.CHAINMAIL_CHESTPLATE)) {
			careertier.career = "armorer";
			careertier.tier = 4;
		}// weaponsmith
		else if (result.getType().equals(Material.IRON_AXE)) {
			careertier.career = "weaponsmith";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.IRON_SWORD)) {
			careertier.career = "weaponsmith";
			careertier.tier = 2;
		}
		else if (result.getType().equals(Material.DIAMOND_SWORD)) {
			careertier.career = "weaponsmith";
			careertier.tier = 3;
		}// toolsmith
		else if (result.getType().equals(Material.IRON_SPADE)) {
			careertier.career = "toolsmith";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.IRON_PICKAXE)) {
			careertier.career = "toolsmith";
			careertier.tier = 2;
		}
		else if (result.getType().equals(Material.DIAMOND_PICKAXE)) {
			careertier.career = "toolsmith";
			careertier.tier = 3;
		}// cleric
		else if (ingredients.get(0).getType().equals(Material.ROTTEN_FLESH)) {
			careertier.career = "cleric";
			careertier.tier = 1;
		}
		else if (result.getType().equals(Material.REDSTONE)) {
			careertier.career = "cleric";
			careertier.tier = 2;
		}
		else if (result.getType().equals(Material.GLOWSTONE)) {
			careertier.career = "cleric";
			careertier.tier = 3;
		}
		else if (result.getType().equals(Material.EXP_BOTTLE)) {
			careertier.career = "cleric";
			careertier.tier = 4;
		}
		
		
		if (careertier.career == "librarian" && careertier.tier == 0) {
			careertier.tier = getLastTier(careertier, villager, recipe) + 1;
		}
		
		return careertier;
	}
	
	//figure out what tier a librarian is in
	public static int getLastTier(CareerTier careertier,
			Villager villager, MerchantRecipe recipe) {
		int last = 0;
		
		//check the librarian file
		String villagerid = "id" + Integer.toString(villager.getEntityId());
		if (plugin.getLibrarians().contains(villagerid)) {
			last = plugin.getLibrarians().getInt(villagerid);
		}
		//check librarian tier list against their trade list
		else if (plugin.getConfig().contains("librarian")) {
			List<Integer> tradesbytier = new ArrayList<Integer>();
			int currenttrades = villager.getRecipeCount();
			
			if (plugin.getConfig().getString("librarian") != "default") {
				for (String tier : plugin.getConfig().getStringList(
						"librarian")) {
					tradesbytier.add(plugin.getConfig().getList(tier).size() + 
							tradesbytier.get(tradesbytier.size() - 1));
				}
			}
			else {
				for (String tier : plugin.getVanilla().getStringList(
						"librarian")) {
					tradesbytier.add(plugin.getVanilla().getList(tier).size() +
							tradesbytier.get(tradesbytier.size() - 1));
				}
			}
			for (int trades : tradesbytier) {
				if (currenttrades < trades) {
					last = tradesbytier.indexOf(trades);
				}
			}
		}
		else {
			plugin.getLogger().warning("Librarians missing from config.yml");
		}
		
		
		
		return last;
	}
}
