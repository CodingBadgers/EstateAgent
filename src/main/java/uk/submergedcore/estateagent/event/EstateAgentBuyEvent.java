package uk.submergedcore.estateagent.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class EstateAgentBuyEvent extends Event implements Cancellable {

	private boolean cancelled = false;
	
	private static final HandlerList handlers = new HandlerList();
	
	final private Player buyer;
	final private OfflinePlayer seller;
	final private boolean serverSeller;
	final private ProtectedRegion protectedRegion;
	final private int price;
		
	public EstateAgentBuyEvent(Player buyer, OfflinePlayer seller, boolean serverSeller, ProtectedRegion protectedRegion, int price) {
		this.buyer = buyer;
		this.seller = seller;
		this.serverSeller = serverSeller;
		this.protectedRegion = protectedRegion;
		this.price = price;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public Player getBuyer() {
		return this.buyer;
	}
	
	public OfflinePlayer getSeller() {
		return this.seller;
	}
	
	public boolean isServerOwned() {
		return this.serverSeller;
	}
	
	public ProtectedRegion getRegion() {
		return this.protectedRegion;
	}
	
	public int getPrice() {
		return this.price;
	}

}
