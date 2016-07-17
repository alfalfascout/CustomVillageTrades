package com.alfalfascout.CustomVillageTrades;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CvtCommand implements CommandExecutor {
    private final CustomVillageTrades plugin;
    private final String noPermission = "You don't have permission to do that.";
    
    public CvtCommand(CustomVillageTrades plugin) {
        this.plugin = plugin;
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
            }
            
            return true;
        }
        
        return false;
    }
    
    private boolean reloadConfigs() {
        
        plugin.reloadConfig();
        plugin.populateWorlds();
        plugin.loadTreesByWorld();
        plugin.getDefaultConfigs();
        
        return true;
    }
    
}
