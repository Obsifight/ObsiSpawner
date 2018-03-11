package fr.thisismac.spawner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

@SuppressWarnings("deprecation")
public class Core extends JavaPlugin
{
	private HashMap<String, ArrayList<UniqueSpawner>> playerSpawners = new HashMap<String, ArrayList<UniqueSpawner>>();
	public String cmd = "obsispawner";
	private String prefix =  ChatColor.DARK_RED + "[" + ChatColor.GOLD + cmd + ChatColor.DARK_RED + "] " + ChatColor.RESET;
	private HashMap<String, UniqueSpawner> willSetSpawner = new HashMap<String, UniqueSpawner>();
	private ArrayList<UniqueSpawner> spawners = new ArrayList<UniqueSpawner>();
	private File dataFile;
	private FileConfiguration dataConfig;
	private WorldGuardPlugin wg;
		
	@Override
	public void onEnable() {
		  Bukkit.getPluginManager().registerEvents(new Listeners(this), this);
		  getCommand(cmd).setExecutor(new SpawnerCommand(this));
		  
		  	if(!getDataFolder().exists()) getDataFolder().mkdir();
			dataFile = new File(getDataFolder() + File.separator + "data.yml");
			dataConfig = YamlConfiguration.loadConfiguration(dataFile);
			
			if(!dataFile.exists()) {
				try {
					dataFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			loadData();
			loadSpawnerFromData();
			
			try {
				wg = getWorldGuard();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	@Override
	public void onDisable() {
		saveData();
	}


	@SuppressWarnings("serial")
	private void loadSpawnerFromData() {
		for(final UniqueSpawner sp : getSpawners()) {
			if(!playerSpawners.containsKey(sp.getPlayer())) {
				playerSpawners.put(sp.getPlayer(), new ArrayList<UniqueSpawner>() {{ add(sp);}});
			}
			else {
				playerSpawners.get(sp.getPlayer()).add(sp);
			}
		}
		
	}
	
	public  ArrayList<UniqueSpawner> getSpawners() {
		return spawners;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public HashMap<String, ArrayList<UniqueSpawner>> getPlayerSpawners() {
		return playerSpawners;
	}
	
	public HashMap<String, UniqueSpawner> getPlayerWhoWillSetSpawner() {
		return willSetSpawner;
	}
	public ItemStack applyLoreANDTitle(ItemStack item, UniqueSpawner sp) {
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.DARK_PURPLE + "Type : " + ChatColor.GOLD + EntityType.fromId(sp.getType()).getName());
		lore.add(ChatColor.DARK_PURPLE + "Appartient à : " + ChatColor.GOLD + sp.getPlayer());
		lore.add(ChatColor.DARK_PURPLE + "Identifiant : " + ChatColor.GOLD + sp.getKey());
		
		if(sp.isInMap()) {
			lore.add(ChatColor.DARK_PURPLE + "Position ? : " + ChatColor.GOLD + parseLocToStringWithoutWorld(sp.getLoc())); 
		}
		else {
			lore.add(ChatColor.DARK_PURPLE + "Position ? : " + ChatColor.GOLD + "Pas posé"); 
		}
		
		ItemMeta temp = item.getItemMeta();
		temp.setDisplayName(ChatColor.BOLD + "" +ChatColor.GREEN + "Spawner à " + EntityType.fromId(sp.getType()).getName());
		temp.setLore(lore);
		item.setItemMeta(temp);
		return item;
	}
	
	public int getKeyFromItemStack(ItemStack item) {
		if(item.getItemMeta().getLore() == null) {
			return -1;
		}
		return Integer.parseInt(item.getItemMeta().getLore().get(2).split(":")[1].substring(3));
	}
	
	public UniqueSpawner getSpawnerFromItemStack(ItemStack item) {
		for(UniqueSpawner spawner : spawners) {
			if(spawner.getKey() == getKeyFromItemStack(item)) {
				return spawner;
			}
		}
		return null;
	}
	public EntityType getSpawnerType(Block target)
	  {
	    CreatureSpawner testSpawner = (CreatureSpawner)target.getState();
	    return testSpawner.getSpawnedType();
	  }  
	
	public boolean isUniqueSpawner(ItemStack item) {
		if(item.getItemMeta().getLore().get(0).contains("Type :") && item.getItemMeta().getLore().get(1).contains("Appartient à :")  && item.getItemMeta().getLore().get(2).contains("Identifiant :")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setSpawnerType(Block target, EntityType entity)  {
	    CreatureSpawner testSpawner = (CreatureSpawner)target.getState();
	    testSpawner.setSpawnedType(entity);
	 }
	
	public Inventory createSpawnerInventory(String name) {
		Inventory temp = Bukkit.createInventory(new SpawnerInventory(name), 54, ChatColor.RED + "ObsiSpawner by ThisIsMac.fr");
		
		int numberOfSpawners = playerSpawners.get(name).size() -1;
		
		for(int x = 0; x <= numberOfSpawners; x++) {
			ItemStack item = new ItemStack(Material.MOB_SPAWNER);
			temp.setItem(x, applyLoreANDTitle(item, playerSpawners.get(name).get(x)));
		}
		
		return temp;
	}
	
	public void saveData() 
	{
		if(spawners.isEmpty()) return;
		
		  dataConfig.set("spawners", null);
		  
		  for(UniqueSpawner sp : spawners) {
			  dataConfig.set("spawners." + sp.getKey() + ".player",  sp.getPlayer());
			  dataConfig.set("spawners." + sp.getKey() + ".type",  sp.getType());
			  dataConfig.set("spawners." + sp.getKey() + ".loc",  parseLocToString(sp.getLoc()));
			  dataConfig.set("spawners." + sp.getKey() + ".inMap",  sp.isInMap());
		  }
	 
		  try {
			dataConfig.save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadData(){
		for(String key : dataConfig.getConfigurationSection("spawners").getKeys(false)){
				short type = (short) dataConfig.getInt("spawners." + key + ".type");
				String player = dataConfig.getString("spawners." + key + ".player");
				Location loc = parseStringToLoc(dataConfig.getString("spawners." + key + ".loc"));
				boolean inMap = dataConfig.getBoolean("spawners." + key + ".inMap");
				spawners.add(new UniqueSpawner(player, type, loc, Integer.parseInt(key), inMap));
		}
	}

	public static Location parseStringToLoc(String string) {
		if (string.equalsIgnoreCase("null")) return null;
		
		String[] locString = string.split(",");
		return new Location(Bukkit.getWorld(locString[0]), Double.parseDouble(locString[1]), Double.parseDouble(locString[2]), Double.parseDouble(locString[3]));
	}
	
	public String parseLocToString(Location location) {
		if(location == null) return "null";
		
		return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
	}
	
	public String parseLocToStringWithoutWorld(Location location) {
		if(location == null) return "null";
		
		return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
	}
	
	private WorldGuardPlugin getWorldGuard() throws Exception {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        throw new Exception();
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public WorldGuardPlugin getWG() {
		return wg;
	}
	
	public void logWithColor(String string) {
		Bukkit.getServer().getConsoleSender().sendMessage(string);
	}
}