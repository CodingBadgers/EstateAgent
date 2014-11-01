package uk.submergedcore.estateagent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import uk.submergedcore.estateagent.event.EstateAgentBuyEvent;

public class EAPlayerListener implements Listener {

	public EAPlayerListener(EstateAgent estateAgent) {
	
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		
		Block block = event.getClickedBlock();
		if(!(block.getTypeId() == 63 || block.getTypeId() == 68)) {
			return;
		}
		
		Sign sign = (Sign)block.getState();
		String commandLine = sign.getLine(0);
		
		if (commandLine.equalsIgnoreCase("[ForSale]")) {
			handleBuyRequest(event);
			return;
		}
		
		if (commandLine.equalsIgnoreCase("[Member]")) {
			handleMemberRequest(event);
			return;
		}
					
	}

	private void handleMemberRequest(PlayerInteractEvent event) {
		
		Player clickPlayer = event.getPlayer();
		Block block = event.getClickedBlock();
		
		Sign sign = (Sign)block.getState();
		String memberOnSign = sign.getLine(2);
		
		if (!memberOnSign.equalsIgnoreCase(clickPlayer.getName())) {
			EAUtil.outputToPlayer(clickPlayer, "This sign wasn't intended for you");
			return;
		}
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				clickPlayer, 
				block.getLocation());
		
		if (region == null) {
			return;
		}
		
		String priceString = sign.getLine(3);
		
		int price = 0;
		if (priceString.length() != 0) {
			try {
				price = Integer.parseInt(priceString);
			} catch(Exception ex) {
				EAUtil.outputToPlayer(clickPlayer, "The price on the sign is incorrect.");
				return;
			}
		}	
		
		// Check can afford
		if (!EAUtil.economy.has(clickPlayer.getName(), price)) {
			EAUtil.outputToPlayer(clickPlayer, "You can't afford to become a member of " + region.getId() + ".");
			return;
		}
		
		// add the player as a member
		region.getMembers().addPlayer(clickPlayer.getName());
		EAUtil.economy.withdrawPlayer(clickPlayer.getName(), price);
		
		String regionOwner = (String)region.getOwners().getPlayers().toArray()[0];	
		if (EAUtil.economy.hasAccount(regionOwner))
			EAUtil.economy.depositPlayer(regionOwner, price);
		
		EAUtil.popOutSign(block);
		
		EAUtil.outputToPlayer(regionOwner, "Added player " + clickPlayer.getName() + " to " + region.getId() + ".");
		EAUtil.outputToPlayer(clickPlayer, "You have been added as a member to " + region.getId() + ".");
		
		try {
			EAUtil.worldGuard.getGlobalRegionManager().get(clickPlayer.getWorld()).save();
		} catch (Exception e) {
			EAUtil.outputToConsole("Failed to save region settings");
		}
	}

	@SuppressWarnings("rawtypes")
	private void handleBuyRequest(PlayerInteractEvent event) {
		
		Player buyer = event.getPlayer();
		Block block = event.getClickedBlock();
		
		ProtectedRegion region = RegionManager.getChildRegionFromLocation(
				buyer, 
				block.getLocation());
		
		if (region == null) {
			EAUtil.outputToPlayer(buyer, "A problem has occurred whilst finding the region, please inform staff.");
			return;
		}
		
		SaleRegion saleRegion = RegionManager.getRegionForSale(region);
		
		if (saleRegion == null) {
			EAUtil.outputToPlayer(buyer, "A problem has occurred whilst finding the sale region, please inform staff.");
			return;
		}
		
		if (!saleRegion.canBuy(buyer)) {
			return;
		}
		
		// Check noof regions owned
		ProtectedRegion parent = region.getParent();
		if (parent != null) {
	
			com.sk89q.worldguard.protection.managers.RegionManager rm = EAUtil.worldGuard.getGlobalRegionManager().get(buyer.getWorld());
			Map<String, ProtectedRegion> regions = rm.getRegions();
			
			Set set = regions.entrySet();
			Iterator it = set.iterator();
			
			ArrayList<ProtectedRegion> siblings = new ArrayList<ProtectedRegion>();
			
			while (it.hasNext()) {
				Map.Entry m =(Map.Entry)it.next();
	            ProtectedRegion child = (ProtectedRegion)m.getValue();          

	            if (child.getParent() != null && child.getParent() == parent) {
	            	if (child != region)
	            		siblings.add(child);	            	
	            }
			}
			
			int noofOwnedInParent = 0;
			it = siblings.iterator();
			while (it.hasNext()) {
				ProtectedRegion sibling = (ProtectedRegion) it.next();
				LocalPlayer lp = EAUtil.worldGuard.wrapPlayer(buyer);
				if (sibling.getOwners().contains(lp)) {
					noofOwnedInParent++;
				}				
			}
			
			int maxPlotsperParent = 2;
			if (noofOwnedInParent >= maxPlotsperParent) {
				EAUtil.outputToPlayer(buyer, "You already own " + maxPlotsperParent + " plots within " + parent.getId() + " you can't buy anymore.");
				return;
			}
			
		}
		
		String seller = saleRegion.getPlayerName();
		int price = saleRegion.getPrice();
		boolean serverSeller = saleRegion.getServerOwned();
		
		if (seller.equalsIgnoreCase(buyer.getName())) {
			EAUtil.outputToPlayer(buyer, "You are already the owner of this plot.");
			return;
		}

		if (!EAUtil.economy.hasAccount(buyer.getName()) || !EAUtil.economy.hasAccount(seller)) {
			EAUtil.outputToPlayer(buyer, "A problem occurred whilst transferring funds. Please try again later.");
			return;
		}
		
		if (!EAUtil.economy.has(buyer.getName(), price)) {
			EAUtil.outputToPlayer(buyer, "You can't afford this region.");
			return;
		}

		// Fire a buy event
		EstateAgentBuyEvent buyEvent = new EstateAgentBuyEvent(buyer, saleRegion.getPlayer(), serverSeller, saleRegion.getRegion(), saleRegion.getPrice());
		Bukkit.getPluginManager().callEvent(buyEvent);
		if (buyEvent.isCancelled()) {
			return;
		}
			
		// write the log before attempting saving and database writing, as these may crash or fail. this way we have proof if something goes tits up
		EAUtil.fileManager.addBuyLog(buyer.getName(), region.getId(), price);
		
		// remove region from database
		RegionManager.removeRegionForSale(saleRegion);
				
		if (!serverSeller) {
			EAUtil.economy.depositPlayer(seller, price);
			Iterator<String> owners = region.getOwners().getPlayers().iterator();
			while (owners.hasNext())
			{
				String owner = owners.next();
				region.getOwners().removePlayer(owner);
			}
			EAUtil.outputToPlayer(seller, buyer.getName() + " has bought " + region.getId() + ".");
		}
		else
		{
			Iterator<String> owners = region.getOwners().getPlayers().iterator();
			while (owners.hasNext())
			{
				String owner = owners.next();
				region.getOwners().removePlayer(owner);
			}
		}
		
		EAUtil.economy.withdrawPlayer(buyer.getName(), price);
		region.getOwners().addPlayer(buyer.getName());
		EAUtil.outputToPlayer(buyer, "You have bought " + region.getId() + ".");
		
		block.setTypeId(0);
		buyer.getWorld().dropItemNaturally(
				block.getLocation(), 
				new ItemStack(Material.COOKIE, 1)
				);
		
		try {
			EAUtil.worldGuard.getGlobalRegionManager().get(buyer.getWorld()).save();
		} catch (Exception e) {
			EAUtil.outputToConsole("Failed to save region settings");
		}
		
	}

}
