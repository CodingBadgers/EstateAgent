package uk.submergedcore.estateagent;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager;
import uk.thecodingbadgers.bDatabaseManager.bDatabaseManager.DatabaseType;
import uk.thecodingbadgers.bDatabaseManager.Database.BukkitDatabase;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileManager {

	private EstateAgent m_plugin = null;
	private String m_serverName = "Server";
	private String m_currency = "Pounds";
	private BukkitDatabase m_db = null;
	private int m_perBlockBuyPrice = 3;
	private int m_perBlockSellPrice = 5;
	
	public FileManager(EstateAgent plugin) {
		m_plugin = plugin;
	}
	
	public boolean loadConfig() {
		
		try {
			FileConfiguration config = m_plugin.getConfig();
			
			config.addDefault("serverSellerName", "Server");	
			config.addDefault("currency", "Pounds");	
			config.addDefault("perBlockBuyPrice", 3);	
			config.addDefault("perBlockSellPrice", 5);	
			
			config.options().copyDefaults(true);
			
			m_serverName = config.getString("serverSellerName");
			m_currency = config.getString("currency");
			m_perBlockBuyPrice = config.getInt("perBlockBuyPrice");
			m_perBlockSellPrice = config.getInt("perBlockSellPrice");
			
			m_plugin.saveConfig();
		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}
				
		// create an SQList object
		m_db = bDatabaseManager.createDatabase("Properties", EAUtil.plugin, DatabaseType.SQLite);
		
		// see if a table called properties exist
		if (!m_db.tableExists("properties")) {
			
			// the table doesn't exist, so make one.
			
			EAUtil.log.info("Could not find properties table, now creating one.");
			String query = "CREATE TABLE properties (" +
					"world VARCHAR(128)," +
					"seller VARCHAR(24)," +
					"region VARCHAR(128)," +
					"price INT," +
					"serverOwned INT" +
					");";
			
			// to create a table we pass an SQL query.
			m_db.query(query, true);
		}
		
		// load all properties
		
		// select every property from the table
		String query = "SELECT * FROM properties";
		ResultSet result = m_db.queryResult(query);
		
		try {
			// while we have another result, read in the data
			while (result.next()) {
	            String worldName = result.getString("world");
	            String sellerName = result.getString("seller");
	            String regionName = result.getString("region");
	            int price = result.getInt("price");
	            boolean serverOwned = result.getInt("serverOwned") == 1;
	            
	            // get the world and region from there names
	            World world = EAUtil.plugin.getServer().getWorld(worldName);
	            ProtectedRegion region = EAUtil.worldGuard.getRegionManager(world).getRegion(regionName);
	
	            // add the region, but set the last param as false, so the region isn't readded to the table
	            RegionManager.setRegionForSale(sellerName, region, price, serverOwned, false);
	            
	        }
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		m_db.freeResult(result);
		
		return true;
	}
	
	public void addProperty(SaleRegion region) {
				
		// insert the region into the properties table.
		// again its an sql query
		String query = "INSERT INTO 'properties' " +
				"('world','seller','region','price','serverOwned') VALUES (" + 
				"'" + region.getPlayer().getPlayer().getWorld().getName() + "'," +
				"'" + region.getPlayer().getName() + "'," +
				"'" + region.getRegion().getId() + "'," +
				"'" + region.getPrice() + "'," +
				"'" + (region.getServerOwned() == true ? 1 : 0) + 
				"');";
		
		m_db.query(query);
	}
	
	public void removeProperty(SaleRegion region) {
		
		// again an sql query to delete a region by a regions name
		String query = "DELETE FROM properties WHERE region = '" + region.getRegion().getId() + "';";
		m_db.query(query);
		
	}
		
	public String getServerSellerName() {
		return m_serverName;
	}
	
	public String getCurrency() {
		return m_currency;
	}
	
	public int getPerBlockSellPrice() {
		return m_perBlockSellPrice;
	}
	
	public int getPerBlockBuyPrice() {
		return m_perBlockBuyPrice;
	}
	
	public void addBuyLog(String playername, String regionname, int price)
	{	
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		
		String message = "[" + dateFormat.format(date) + "] " + playername + " bought " + regionname + " for " + price + " " + m_currency + "s.";
	
		try{
			  // Create file 
			  BufferedWriter out = new BufferedWriter(new FileWriter("plugins/EstateAgent/log.txt", true));
			  out.write(message + "\n");
			  //Close the output stream
			  out.close();
		  } catch (Exception e) {
			  EAUtil.outputToConsole("Error: " + e.getMessage());
		  }
	
	}
	
}
