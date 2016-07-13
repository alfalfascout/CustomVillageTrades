package com.alfalfascout.CustomVillageTrades;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CvtCommand implements CommandExecutor {
    private final CustomVillageTrades plugin;
    
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
                }
            }
            
            return false;
        }
        
        return false;
    }
    
    private boolean reloadConfigs() {
        
        plugin.reloadConfig();
        plugin.populateWorlds();
        plugin.loadTreesByWorld();
        
        return true;
    }
    
}
