package uk.submergedcore.estateagent.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class EstateAgentMemberEvent extends Event implements Cancellable {

	private boolean cancelled = false;
	
	private static final HandlerList handlers = new HandlerList();
	
	final private Player player;
	final private String mode;
	final private String newMember;
	final private ProtectedRegion protectedRegion;
	private int price;
	
	public EstateAgentMemberEvent(Player player, String modeString, String newMemberName, ProtectedRegion region, String priceString) {
		
		this.player = player;
		this.mode = modeString;
		this.newMember = newMemberName;
		this.protectedRegion = region;
		try {
			this.price = Integer.parseInt(priceString);
		} catch(Exception ex) {
			this.price = 0;
		}
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
	
	public Player getPlayer() {
		return this.player;
	}
	
	public String getMode() {
		return this.mode;
	}
	
	public String getNewMember() {
		return this.newMember;
	}
	
	public ProtectedRegion getRegion() {
		return this.protectedRegion;
	}
	
	public int getPrice() {
		return this.price;
	}

}
