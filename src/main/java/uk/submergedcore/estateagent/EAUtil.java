package uk.submergedcore.estateagent;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class EAUtil {

	public static EstateAgent plugin = null;
	public static WorldGuardPlugin worldGuard = null;
	public static FileManager fileManager = null;
	public static Economy economy = null;
	public static Logger log = null;
	public static Permission permission;
	
	public static void outputToConsole(String message) {
		log.info(message);
	}
	
	public static void outputToPlayer(Player player, String message) {
		if (player != null) {
			if (player.isOnline()) {
				player.sendMessage(ChatColor.YELLOW + "[EstateAgent] " + ChatColor.WHITE + message);
			}
		}
	}

	public static void outputToPlayer(String playername, String message) {
		Player player = plugin.getServer().getPlayer(playername);
		if (player != null) {
			outputToPlayer(player, message);
		}
	}
	
	public static void popOutSign(Block block) {
		block.setTypeId(0);
		block.getWorld().dropItemNaturally(
				block.getLocation(), 
				new ItemStack(Material.SIGN, 1)
				);
	}
	
}
