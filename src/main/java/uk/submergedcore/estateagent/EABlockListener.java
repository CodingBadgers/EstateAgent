package uk.submergedcore.estateagent;

import java.text.NumberFormat;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import uk.submergedcore.estateagent.event.EstateAgentMemberEvent;

public class EABlockListener implements Listener {

	public EABlockListener(EstateAgent estateAgent) {

	}

	@EventHandler(priority = EventPriority.LOW)
	public void onSignChange(SignChangeEvent event) {
		
		String commandLine = event.getLine(0);
		
		if (commandLine.equalsIgnoreCase("[fs]") || commandLine.equalsIgnoreCase("[forsale]")) {
			handleRegionSale(event);
			return;
		}
		
		if (commandLine.equalsIgnoreCase("[member]")) {
			handleMemberSign(event);
			return;
		}
		
		if (commandLine.equalsIgnoreCase("[clear]")) {
			handleClearRequest(event);
			return;
		}
		
		if (commandLine.equalsIgnoreCase("[info]")) {
			handleInfoRequest(event);
			return;
		}
		
	}
	
	private void handleInfoRequest(SignChangeEvent event) {
		
		Player player = event.getPlayer();
		if (player == null) {
			EAUtil.outputToConsole("Player was null");
			return;
		}
		
		if (!RegionManager.hasPermission(player, "estateagent.admin.info")) {
			EAUtil.outputToPlayer(player, "You don't have permission to view plot info.");
			return;
		}
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				event.getBlock().getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(player, "Could not find a region at the signs location.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		EAUtil.plugin.getServer().dispatchCommand(player, "region info " + region.getId());
		
		EAUtil.popOutSign(event.getBlock());
	}

	private void handleClearRequest(SignChangeEvent event) {
		
		Player player = event.getPlayer();
		if (player == null) {
			EAUtil.outputToConsole("Player was null");
			return;
		}
		
		if (!RegionManager.hasPermission(player, "estateagent.admin.clear")) {
			EAUtil.outputToPlayer(player, "You don't have permission to clear sale regions.");
			return;
		}
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				event.getBlock().getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(player, "Could not find a region at the signs location.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		if (RegionManager.isRegionForSale(region)) {
			RegionManager.removeRegionForSale(RegionManager.getRegionForSale(region));
		}
		
		EAUtil.popOutSign(event.getBlock());
		
		EAUtil.outputToPlayer(player, "All sales information was cleared for region " + region.getId());
	}

	private void handleMemberSign(SignChangeEvent event) {
		
		Player player = event.getPlayer();
		if (player == null) {
			EAUtil.outputToConsole("Player was null");
			return;
		}
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				event.getBlock().getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(player, "Could not find a region at the signs location.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		if (!RegionManager.isOwner(player, region)) {
			EAUtil.outputToPlayer(player, "You don't own that region, so you can't edit its members.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		String modeString = event.getLine(1);
		
		if (modeString.length() == 0 || !(modeString.equalsIgnoreCase("add") || modeString.equalsIgnoreCase("remove")
				|| modeString.equalsIgnoreCase("addgroup") || modeString.equalsIgnoreCase("removegroup"))) {
			EAUtil.outputToPlayer(player, "Second line should be: add, remove, addgroup or removegroup.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		String newMemberName = event.getLine(2);
		
		String priceString = event.getLine(3);
	
		if (priceString.length() != 0) {
			try {
				Integer.parseInt(priceString);
			} catch(Exception ex) {
				EAUtil.outputToPlayer(player, "Please enter a valid number on the last line of the sign.");
				EAUtil.popOutSign(event.getBlock());
				return;
			}
		}
		
		// Fire a member event
		EstateAgentMemberEvent memberEvent = new EstateAgentMemberEvent(player, modeString, newMemberName, region, priceString);
		Bukkit.getPluginManager().callEvent(memberEvent);
		if (memberEvent.isCancelled()) {
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		// add a member for free.
		if (newMemberName.length() != 0 && priceString.length() == 0) {
			if (modeString.equalsIgnoreCase("add")) {
				region.getMembers().addPlayer(newMemberName);
				EAUtil.outputToPlayer(player, "Added player " + newMemberName + " to " + region.getId() + ".");
				EAUtil.outputToPlayer(newMemberName, "You have been added as a member to " + region.getId() + ".");
			} else if (modeString.equalsIgnoreCase("addgroup")) {
				region.getMembers().addGroup(newMemberName);
				EAUtil.outputToPlayer(player, "Added group " + newMemberName + " to " + region.getId() + ".");
			} else if (modeString.equalsIgnoreCase("remove")) {
				region.getMembers().removePlayer(newMemberName);
				EAUtil.outputToPlayer(player, "Removed player " + newMemberName + " from " + region.getId() + ".");
				EAUtil.outputToPlayer(newMemberName, "You have been removed as a member from " + region.getId() + ".");
			} else if (modeString.equalsIgnoreCase("removegroup")) {
				region.getMembers().removeGroup(newMemberName);
				EAUtil.outputToPlayer(player, "Removed group " + newMemberName + " from " + region.getId() + ".");
			}
			
			EAUtil.popOutSign(event.getBlock());
			return;
		}
	}
	
	private void handleRegionSale(SignChangeEvent event) {
		
		Player player = event.getPlayer();
		if (player == null) {
			EAUtil.outputToConsole("Player was null");
			return;
		}
		
		if (!RegionManager.hasPermission(player, "estateagent.use")) {
			EAUtil.outputToPlayer(player, "You don't have permission to sell plots.");
			return;
		}
		
		String priceString = event.getLine(1).trim();
		int price = 0;
		
		if (priceString.length() != 0)
		{
			try {
				price = Integer.parseInt(priceString);
			} catch(Exception ex) {
				EAUtil.outputToPlayer(player, "Please enter a valid number on the second line of the sign.");
				EAUtil.popOutSign(event.getBlock());
				return;
			}
		}
		
		if (price < 0 && priceString.length() != 0) {
			EAUtil.outputToPlayer(player, "Please enter a value greater than zero on the second line of the sign...");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
				
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				event.getBlock().getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(player, "Could not find a region to sell.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		boolean serverOwner = event.getLine(2).equalsIgnoreCase(EAUtil.fileManager.getServerSellerName());
		if (serverOwner && !RegionManager.hasPermission(player, "estateagent.admin.sell")) {
			EAUtil.outputToPlayer(player, "You don't have permission to sell on the behalf of the server.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		if (!RegionManager.isOwner(player, region) && !serverOwner) {
			EAUtil.outputToPlayer(player, "You don't own that region, so you can't sell it.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		if (RegionManager.isRegionForSale(region)) {
			EAUtil.outputToPlayer(player, "That region (" + region.getId() + ") is already for sale.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
				
		if (serverOwner && price == 0) {
			int volume = region.volume();
			if (volume == 0) {
				EAUtil.outputToPlayer(player, "Could not calculate the volume of the region.");
				EAUtil.popOutSign(event.getBlock());
				return;
			}
			price = (volume * EAUtil.fileManager.getPerBlockSellPrice());
		}
		
		if (price == 0) {
			EAUtil.outputToPlayer(player, "Please enter a valid price on the second line of the sign.");
			EAUtil.popOutSign(event.getBlock());
			return;
		}
		
		RegionManager.setRegionForSale(player, region, price, serverOwner, true);
		
		if (serverOwner) {
			EAUtil.outputToPlayer(player, region.getId() + " will be sold by the server.");
			
			Iterator<String> owners = region.getOwners().getPlayers().iterator();
			while (owners.hasNext())
			{
				String owner = owners.next();
				if (!player.getName().equalsIgnoreCase(owner))
					region.getOwners().removePlayer(owner);
			}
			
			Iterator<String> members = region.getMembers().getPlayers().iterator();
			if (members.hasNext())
			{
				String member = members.next();
				region.getMembers().removePlayer(member);
			}
		}
		
		EAUtil.outputToPlayer(player, region.getId() + " is now for sale, for " + 
			NumberFormat.getInstance().format(price) + " " + 
			EAUtil.fileManager.getCurrency() + ".");
		
		event.setLine(0, "[ForSale]");
		event.setLine(1, region.getId());
		event.setLine(2, "");
		event.setLine(3, NumberFormat.getInstance().format(price));
		
		try {
			EAUtil.worldGuard.getGlobalRegionManager().get(player.getWorld()).save();
		} catch (Exception e) {
			EAUtil.outputToConsole("Failed to save region settings");
		}
		
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event) {
				
		Block block = event.getBlock();
			
		// stop people destroying blocks that have a sign on them
		for (int x = -1; x <= 1; ++x)
		{
			for (int z = -1; z <= 1; ++z)
			{
				if (x == 0 && z == 0)
					continue;
				
				Location loc = block.getLocation();
				Location isSignLoc = loc.add(x, 0, z);
				Block b = isSignLoc.getBlock();
				if (b.getTypeId() == 63 || b.getTypeId() == 68) {
					Sign sign = (Sign)b.getState();
					if (sign != null && sign.getLine(0).equalsIgnoreCase("[ForSale]")) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		Location loc = block.getLocation();
		Location isSignLoc = loc.add(0, 1, 0);
		Block b = isSignLoc.getBlock();
		if (b.getTypeId() == 63 || b.getTypeId() == 68) {
			Sign sign = (Sign)b.getState();
			if (sign != null && sign.getLine(0).equalsIgnoreCase("[ForSale]")) {
				event.setCancelled(true);
				return;
			}
		}
		
		// is block a sign?
		if (block.getTypeId() == 63 || block.getTypeId() == 68) {
			handleRemoveSale(event);
			return;
		}
		
	}
	
	private void handleRemoveSale(BlockBreakEvent event) {
		
		Player player = event.getPlayer();
		if (player == null) {
			EAUtil.outputToConsole("Player is null");
			return;
		}
		
		Sign sign = (Sign)event.getBlock().getState();
		
		if (sign == null){
			EAUtil.outputToConsole("Sign was not a sign?");
			return;
		}
				
		String commandLine = sign.getLine(0);
		
		if (!commandLine.equalsIgnoreCase("[forsale]")) {
			return;
		}
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				player, 
				event.getBlock().getLocation());
		
		if (region == null) {
			return;
		}
		
		SaleRegion saleRegion = RegionManager.getRegionForSale(region);
		
		if (saleRegion == null) {
			return;
		}
		
		if (!RegionManager.hasPermission(player, "estateagent.admin.sell"))
		{
			if (!RegionManager.isOwner(player, region)) {
				if (!(saleRegion.getServerOwned() && RegionManager.hasPermission(player, "estateagent.admin"))) {
					EAUtil.outputToPlayer(player, "You don't own that region, so you can't remove its ForSale sign.");
					event.setCancelled(true);
					return;
				}
			}
		}
		
		RegionManager.removeRegionForSale(saleRegion);
		
		EAUtil.outputToPlayer(player, region.getId() + " is no longer for sale.");
		
	}
}
