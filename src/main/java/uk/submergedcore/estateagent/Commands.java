package uk.submergedcore.estateagent;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.text.NumberFormat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Commands {
	
	static boolean checkCommands(CommandSender sender, Command command, String commandLabel, String[] args) {
		
		Player player = (Player)sender;
		
		if (commandLabel.equalsIgnoreCase("ea")) {
    		
    		if (args.length == 0 || args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
	    		EAUtil.outputToPlayer(player, "Estate Agent Commands");
    			EAUtil.outputToPlayer(player, "/ea buy");
	    		EAUtil.outputToPlayer(player, "/ea sell");
	    		EAUtil.outputToPlayer(player, "/ea member");
	    		if (RegionManager.hasPermission(player, "estateagent.player.owner"))
	    			EAUtil.outputToPlayer(player, "/ea owner");
	    		if (RegionManager.hasPermission(player, "estateagent.admin.clear"))
	    			EAUtil.outputToPlayer(player, "/ea clear");
	    		if (RegionManager.hasPermission(player, "estateagent.admin.info"))
	    			EAUtil.outputToPlayer(player, "/ea info");
	    		if (RegionManager.hasPermission(player, "estateagent.admin.list"))
	    			EAUtil.outputToPlayer(player, "/ea list");
	    		if (RegionManager.hasPermission(player, "estateagent.admin.groups"))
	    			EAUtil.outputToPlayer(player, "/ea groups <groups>");
	    		
	    		return true;
    		}
    		
    		if (args.length == 1) {
    			
    			if (args[0].equalsIgnoreCase("buy")) {
    				EAUtil.outputToPlayer(player, "To buy a plot simply right click the [ForSale] sign");
    				EAUtil.outputToPlayer(player, "of the plot you want to buy.");
    				return true;
    			}
    			
				if (args[0].equalsIgnoreCase("sell")) {
					EAUtil.outputToPlayer(player, "Place a sign in the plot you want to sell.");
					EAUtil.outputToPlayer(player, "On the first line write [fs].");
					EAUtil.outputToPlayer(player, "On the second line write the price you want to sell it for.");
    				return true;
    			}
				
				if (args[0].equalsIgnoreCase("member")) {
					EAUtil.outputToPlayer(player, "Place a sign in the plot you want to add a member too.");
					EAUtil.outputToPlayer(player, "On the first line write [member].");
					EAUtil.outputToPlayer(player, "On the second line write addmember, removemember or addgroup. Depending on what you want to do.");
					EAUtil.outputToPlayer(player, "On the third line write the player name or group name.");
    				return true;
    			}
				
				if (args[0].equalsIgnoreCase("owner")) {
					handleOwner(player);
					return true;
				}
				
				if (args[0].equalsIgnoreCase("clear")) {
					handleClear(player);
					return true;
    			}
				
				if (args[0].equalsIgnoreCase("info")) {
					handleInfo(player);
					return true;
    			}
				
				if (args[0].equalsIgnoreCase("list")) {
					handleList(player);
					return true;
    			}
    		}

			if (args.length >= 1 && args[0].equalsIgnoreCase("groups")) {
				handleGroups(player, args);
				return true;
			}
    		
    		EAUtil.outputToPlayer(player, "Unknown command. /ea help");
    		
    		return true;
    	}
		
		return false;
	}

	private static void handleOwner(Player player) {
		
		if (!RegionManager.hasPermission(player, "estateagent.player.owner")) 
			return;
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				player.getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(player, "Could not find a region at the your location.");
			return;
		}
		
		String owners = "";
		boolean first = true;
		
		for (String owner : region.getOwners().getPlayers()) {
			owners += (first ? "" : ", " )+ owner;
			first = false;
		}
		
		String members = "";
		first = true;
		
		for (String owner : region.getMembers().getPlayers()) {
			members += (first ? "" : ", " )+ owner;
			first = false;
		}
		
		EAUtil.outputToPlayer(player, ChatColor.YELLOW + "Region: " + ChatColor.WHITE +  region.getId());
		EAUtil.outputToPlayer(player, ChatColor.YELLOW + "Owner: " +  ChatColor.WHITE + owners);
		EAUtil.outputToPlayer(player, ChatColor.YELLOW + "Members: " +  ChatColor.WHITE + members);
	}

	private static void handleGroups(Player player, String[] args) {
		
		if (!RegionManager.hasPermission(player, "estateagent.admin.groups")) {
			return;
		}
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				player.getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(player, "Could not find a region at the your location.");
			return;
		}
		
		if (!RegionManager.isRegionForSale(region)) {
			EAUtil.outputToPlayer(player, "This region is not for sale.");
			return;
		}
		
		SaleRegion saleRegion = RegionManager.getRegionForSale(region);
		
		if (args.length > 1) {
			PermissionGroup[] groups = new PermissionGroup[args.length-1];
			for (int i = 1; i < args.length; ++i) {
				groups[i-1] = PermissionsEx.getPermissionManager().getGroup(args[i]); 
				
				EAUtil.outputToPlayer(player, args[i] + " added to the regions buy groups.");
			}
			
			saleRegion.setGroups(groups);
		} else {
			EAUtil.outputToPlayer(player, "Cleared the regions buy groups.");
			saleRegion.setGroups(null);
		}
		
	}

	private static void handleList(Player player) {
		
		if (!RegionManager.hasPermission(player, "estateagent.admin.list")) {
			return;
		}
		
		if (RegionManager.m_saleRegions.isEmpty()) {
			EAUtil.outputToPlayer(player, "There are no plots for sale");
			return;
		}
		
		for (int i = 0; i < RegionManager.m_saleRegions.size(); ++i) {
			SaleRegion sr = RegionManager.m_saleRegions.get(i);
			EAUtil.outputToPlayer(player, 
					ChatColor.DARK_PURPLE + "[" + sr.getRegion().getId() + "] " + 
					ChatColor.YELLOW + sr.getPlayerName() + 
					" - " + NumberFormat.getInstance().format(sr.getPrice()) + 
					(sr.getServerOwned() ? " (Server Owned)": "") );
		}
	}

	private static void handleInfo(Player player) {
		
		if (!RegionManager.hasPermission(player, "estateagent.admin.info")) {
			return;
		}
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				player.getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(player, "Could not find a region at the your location.");
			return;
		}
		
		EAUtil.plugin.getServer().dispatchCommand(player, "region info " + region.getId());		
	}

	private static void handleClear(Player player) {
		
		if (!RegionManager.hasPermission(player, "estateagent.admin.clear")) {
			return;
		}
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				player.getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(player, "Could not find a region at the your location.");
			return;
		}
		
		if (RegionManager.isRegionForSale(region)) {
			RegionManager.removeRegionForSale(RegionManager.getRegionForSale(region));
		}

		EAUtil.outputToPlayer(player, "All sales information was cleared for region " + region.getId());
	
	}
}
