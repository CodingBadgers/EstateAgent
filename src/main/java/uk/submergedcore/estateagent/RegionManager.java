package uk.submergedcore.estateagent;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionManager {
	
	public static ArrayList<SaleRegion> m_saleRegions = new ArrayList<SaleRegion>();
	
	public static void setRegionForSale(Player player, ProtectedRegion region, int price, boolean serverOwner, boolean saveToFile) {
		setRegionForSale(player.getName(), region, price, serverOwner, saveToFile);
	}
	
	public static void setRegionForSale(String player, ProtectedRegion region, int price, boolean serverOwner, boolean saveToFile) {
		SaleRegion saleRegion = new SaleRegion(player, region, price, serverOwner);
		
		if (saveToFile)
			EAUtil.fileManager.addProperty(saleRegion);
		
		m_saleRegions.add(saleRegion);
	}
	
	public static boolean isRegionForSale(ProtectedRegion region) {
		
		for (int i = 0; i < m_saleRegions.size(); ++i) {
			if (m_saleRegions.get(i).getRegion() == region) {
				return true;
			}
		}
		
		return false;
		
	}

	public static boolean isOwner(Player player, ProtectedRegion region) {
		
		if (player == null || region == null)
			return false;
		
		if (region.isOwner(new BukkitPlayer(EAUtil.worldGuard, player)))
			return true;
		
		return false;
	}
	
	public static ProtectedRegion getChildRegionFromLocation(Player player, Location location) {
		
		ArrayList<ProtectedRegion> possibleRegions = new ArrayList<ProtectedRegion>();
		
		// loop through every region
		for(String regionName : EAUtil.worldGuard.getRegionManager(player.getWorld()).getRegions().keySet()){
			
			// get the region from its name
			ProtectedRegion currentRegion = EAUtil.worldGuard.getRegionManager(player.getWorld()).getRegion(regionName);
			
			// create a world edit vector for the signs position
			com.sk89q.worldedit.Vector v = new com.sk89q.worldedit.Vector(
					location.getX(), 
					location.getY(), 
					location.getZ());
			
			// if the current region contains the sign, add it to the list of possible regions
			if (currentRegion.contains(v)) {
				possibleRegions.add(currentRegion);
			}
		}
		
		// if we didnt get any regions, bail.
		if (possibleRegions.size() == 0)
			return null;
		
		// if we only got one region, it has to be the region we use
		if (possibleRegions.size() == 1)
			return possibleRegions.get(0);

		// work out the lowest child of the regions
		int childLevel = 0;
		ProtectedRegion lowestChild = null;
		for (int i = 0; i < possibleRegions.size(); ++i)
		{
			if (possibleRegions.get(i).getParent() != null)
			{
				ProtectedRegion tempRegion = possibleRegions.get(i);
				int tempChildLevel = 0;
				while(tempRegion.getParent() != null)
				{
					tempRegion = tempRegion.getParent();
					tempChildLevel++;
				}
				
				if (tempChildLevel > childLevel)
				{
					childLevel = tempChildLevel;
					lowestChild = possibleRegions.get(i);
				}
			}
		}
		
		// if we found the lowest child return that
		if (lowestChild != null)
			return lowestChild;

		// if we didn't find the lowest child, but found some regions, return the first region
		if (possibleRegions.size() > 0)
			return possibleRegions.get(0);
			
		// something went very wrong
		return null;
	}

	public static SaleRegion getRegionForSale(ProtectedRegion region) {

		for (int i = 0; i < m_saleRegions.size(); ++i) {
			if (m_saleRegions.get(i).getRegion() == region) {
				return m_saleRegions.get(i);
			}
		}
		
		return null;
	}

	public static void removeRegionForSale(SaleRegion saleRegion) {
		
		EAUtil.fileManager.removeProperty(saleRegion);
		m_saleRegions.remove(saleRegion);		

	}

	public static boolean hasPermission(Player player, String permission) {

				
		// Are we an op?
		if (player.isOp() == true)
			return true;
		
		// use vault to check perms
		if (EAUtil.permission.has(player, permission)){
			return true;
		}

		EAUtil.outputToPlayer(player, "You do not have the required permissions");
				
		return false;	
	}

}
