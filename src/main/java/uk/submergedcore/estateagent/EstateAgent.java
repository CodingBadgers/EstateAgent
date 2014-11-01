package uk.submergedcore.estateagent;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class EstateAgent extends JavaPlugin {

	private EABlockListener m_blockListener = new EABlockListener(this);
	private EAPlayerListener m_playerListener = new EAPlayerListener(this);
	
    public void onEnable() {    
    	
    	EAUtil.plugin = this;
    	EAUtil.worldGuard = (WorldGuardPlugin)getServer().getPluginManager().getPlugin("WorldGuard");
    	EAUtil.fileManager = new FileManager(this);
    	EAUtil.log = getLogger();
    	
    	if (!EAUtil.fileManager.loadConfig()) {
    		EAUtil.log.info("An Error has Occurred Loading the Configuration.");
    		return;
    	}
    	
    	if (getServer().getPluginManager().getPlugin("Vault") != null) {
    		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
    		if (economyProvider != null) {
    			EAUtil.economy = economyProvider.getProvider();
    		}
    		
    		RegisteredServiceProvider<Permission> permissionsProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
    		if (permissionsProvider != null) {
    			EAUtil.permission = permissionsProvider.getProvider();
    		}
    	} else {
    		EAUtil.log.info("Shutting down as vault isn't enabled");
    		setEnabled(false);
    		return;
    	}
    	
    	this.getServer().getPluginManager().registerEvents(m_playerListener, this);
    	this.getServer().getPluginManager().registerEvents(m_blockListener, this);
    	
		EAUtil.log.info("Succesfully Loaded");
    }
     
    public void onDisable() { 
    	EAUtil.log.info("Succesfully Unloaded");
    }
    
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	
    	if (sender instanceof Player) {
    	   	return Commands.checkCommands(sender, command, commandLabel, args);
    	}
   
    	return false;
    }
    
}
