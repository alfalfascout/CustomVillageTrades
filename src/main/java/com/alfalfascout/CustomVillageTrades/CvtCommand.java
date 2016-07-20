package com.alfalfascout.CustomVillageTrades;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CvtCommand implements CommandExecutor {
    private final CustomVillageTrades plugin;
    private final String noPermission = "You don't have permission to do that.";
    private final MetaHelper metaHelper;
    
    public CvtCommand(CustomVillageTrades plugin) {
        this.plugin = plugin;
        metaHelper = plugin.metaHelper;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
            String label, String[] args) {
        
        if (cmd.getName().equalsIgnoreCase("customvillagetrades")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("customvillagetrades.reload")) {
                        reloadConfigs();
                        sender.sendMessage("Configs and trade lists reloaded.");
                        return true;
                    }
                    else {
                        sender.sendMessage(noPermission);
                    }
                }
                
                if (args[0].equalsIgnoreCase("reset")) {
                    if (!sender.hasPermission("customvillagetrades.reset")) {
                        sender.sendMessage(noPermission);
                        return true;
                    }
                    
                    if (args.length > 1 && args[1].equalsIgnoreCase("true")) {
                        plugin.resetVillagers();
                        sender.sendMessage("All villagers erased from memory.");
                        return true;
                    }
                    else {
                        sender.sendMessage("This command will erase all " +
                                "known villagers from memory. \n If you're " +
                                "sure, do /cvt reset true");
                        return true;
                    }
                }
                
                if (args[0].equalsIgnoreCase("make")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Can't make a yml from your held " +
                                "object because you don't have hands.");
                        return true;
                    }
                    
                    if (!(args.length > 1)) {
                        sender.sendMessage("/cvt make <banner|book> \nMakes " + 
                                "a yml from the item in your hand.");
                        return true;
                    }
                    
                    Player player = (Player) sender;
                    Material mainItem = 
                            player.getInventory().getItemInMainHand().getType();
                    Material offItem = 
                            player.getInventory().getItemInOffHand().getType();
                    
                    if (args[1].equalsIgnoreCase("banner")) {
                        if (!player.hasPermission(
                                "customvillagetrades.make.banner")) {
                            sender.sendMessage(noPermission);
                            return true;
                        }
                        if (mainItem.equals(Material.BANNER)) {
                            ItemStack banner = 
                                    player.getInventory().getItemInMainHand();
                            metaHelper.makeBannerFile(banner);
                            sender.sendMessage("Banner file created.");
                            return true;
                        }
                        else if (offItem.equals(Material.BANNER)) {
                            ItemStack banner = 
                                    player.getInventory().getItemInOffHand();
                            metaHelper.makeBannerFile(banner);
                            sender.sendMessage("Banner file created.");
                            return true;
                        }
                        else {
                            sender.sendMessage("Hold the banner you want to " +
                                    "make a yml from.");
                            return true;
                        }
                    }
                    
                    if (args[1].equalsIgnoreCase("book")) {
                        if (!player.hasPermission(
                                "customvillagetrades.make.book")) {
                            sender.sendMessage(noPermission);
                            return true;
                        }
                        if (mainItem.equals(Material.BOOK_AND_QUILL) ||
                                mainItem.equals(Material.WRITTEN_BOOK)) {
                            ItemStack book = 
                                    player.getInventory().getItemInMainHand();
                            metaHelper.makeBookFile(book);
                            sender.sendMessage("Book file created.");
                            return true;
                        }
                        else if (offItem.equals(Material.BOOK_AND_QUILL) ||
                                offItem.equals(Material.WRITTEN_BOOK)) {
                            ItemStack book = 
                                    player.getInventory().getItemInOffHand();
                            metaHelper.makeBookFile(book);
                            sender.sendMessage("Book file created.");
                            return true;
                        }
                        else {
                            sender.sendMessage("Hold the book you want to " +
                                    "make a yml from.");
                            return true;
                        }
                    }
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    private boolean reloadConfigs() {
        
        CustomVillageTrades.trees.clear();
        plugin.reloadConfig();
        plugin.createFiles();
        plugin.getDefaultConfigs();
        
        return true;
    }
    
}
