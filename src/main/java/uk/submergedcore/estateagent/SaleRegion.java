package uk.submergedcore.estateagent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SaleRegion {

	private ProtectedRegion m_region = null;
	private String m_player = null;
	private int m_price = 0;
	private boolean m_serverOwner = false;
	private PermissionGroup[] m_groups = null;
	
	public SaleRegion(String player, ProtectedRegion region, int price, boolean serverOwner) {
		m_player = player;
		m_region = region;
		m_price = price;
		m_serverOwner = serverOwner;
	}
	
	public ProtectedRegion getRegion() {
		return m_region;
	}
	
	public OfflinePlayer getPlayer() {
		return EAUtil.plugin.getServer().getOfflinePlayer(m_player);
	}
	
	public int getPrice() {
		return m_price;
	}
	
	public boolean getServerOwned() {
		return m_serverOwner;
	}

	public String getPlayerName() {
		return m_player;
	}

	public void setGroups(PermissionGroup[] groups) {
		m_groups = groups;
	}
	
	public boolean canBuy(Player player) {
		
		if (!RegionManager.hasPermission(player, "estateagent.use")) {
			EAUtil.outputToPlayer(player, "You don't haver permissions to buy or sell plots.");
			return false;
		}
		
		if (m_groups != null) {
			PermissionGroup[] playersGroups = PermissionsEx.getPermissionManager().getUser(player).getGroups();
			
			for (int i = 0; i < m_groups.length; i++) {
				for (int j = 0; j < playersGroups.length; ++j) {
					if (m_groups[i] == playersGroups[j])
						return true;
				}
			}
		}
		else
			return true;
		
		EAUtil.outputToPlayer(player, "Your rank does not have permission to buy this plot.");		
		return false;
	}
	
}
