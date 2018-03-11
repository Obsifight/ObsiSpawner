package fr.thisismac.spawner;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

public class Listeners implements Listener{
	
	private Core core;
	
	public Listeners(Core c) {
		this.core = c;
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		Iterator<Block> it = e.blockList().iterator();
			while(it.hasNext()) {
				Block value = it.next();
				if(value.getType() == Material.MOB_SPAWNER) {
					it.remove();
					break;
				}
			}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if(e.isCancelled()) return;
		if(!(e.getBlock().getType() == Material.MOB_SPAWNER)) return;
		for(UniqueSpawner sp : core.getSpawners()) {
			
			if(core.parseLocToStringWithoutWorld(e.getBlock().getLocation()).equalsIgnoreCase(core.parseLocToStringWithoutWorld(sp.getLoc()))) {
				if(sp.getPlayer().equalsIgnoreCase(e.getPlayer().getName())) {
					core.getSpawners().get(sp.getKey() - 1).setInMap(false);
					core.getSpawners().get(sp.getKey() - 1).setLoc(null);
					e.getPlayer().sendMessage(core.getPrefix() + ChatColor.GREEN + "Le spawner a été rétiré de la map.");
					e.getBlock().breakNaturally();
					return;
				}
				else if(!sp.getPlayer().equalsIgnoreCase(e.getPlayer().getName())) {
					e.setCancelled(true);
					e.getPlayer().sendMessage(core.getPrefix() + ChatColor.RED + "Vous ne pouvez pas casser un spawner qui n'est pas a vous");
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(e.isCancelled()) return;
		if(!(e.getBlock().getType() == Material.MOB_SPAWNER)) return;
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(e.getAction() != Action.LEFT_CLICK_BLOCK) return;

		if(core.getPlayerWhoWillSetSpawner().containsKey(e.getPlayer().getName()) ) {
			if(e.getClickedBlock().getType() == Material.BEDROCK || e.getClickedBlock().getType() == Material.OBSIDIAN) {
				e.getPlayer().sendMessage(core.getPrefix() + ChatColor.RED + "Vous ne pouvez pas posez de spawner sur de la bedrock ou de l'obsidian.");
				return;
			}
			
			if(!core.getWG().canBuild(e.getPlayer(), e.getClickedBlock())) {
				e.getPlayer().sendMessage(core.getPrefix() + ChatColor.RED + "Vous ne pouvez pas posez de spawner là où vous n'avez pas la permission.");
				return;
			}
			
			FPlayer fp = FPlayers.i.get(e.getPlayer());
		    Faction faction = Board.getFactionAt(new FLocation(e.getClickedBlock().getLocation()));
		   
			if(faction.getId().equalsIgnoreCase(Factions.i.getNone().getId()) || fp.getFaction().getId().equalsIgnoreCase(faction.getId())) {
				 
			    if(!faction.isPlayerInOwnerList(fp.getName(), new FLocation(e.getClickedBlock().getLocation()))) {
			    	e.getPlayer().sendMessage(core.getPrefix() + ChatColor.RED + "Vous ne pouvez pas posez de spawner là où vous n'êtes pas en owner");
			    	return;
			    }
			    
					e.setCancelled(true);
					e.getClickedBlock().setType(Material.MOB_SPAWNER);
					UniqueSpawner spawner = core.getPlayerWhoWillSetSpawner().get(e.getPlayer().getName());
					core.setSpawnerType(e.getClickedBlock(), EntityType.fromId(spawner.getType()));
					
					spawner.setInMap(true);
					spawner.setLoc(e.getClickedBlock().getLocation());
					core.getPlayerWhoWillSetSpawner().remove(e.getPlayer().getName());
				
			}
			else {
				e.getPlayer().sendMessage(core.getPrefix() + ChatColor.RED + "Vous pouvez des spawners uniquement en nature et dans vos claims.");
			}
		}
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		 if(!(e.getWhoClicked() instanceof Player)) return;
		 if(!(e.getInventory().getHolder() instanceof SpawnerInventory)) return;
		 
		 if(!(e.getAction() == InventoryAction.PICKUP_ALL)){
			 e.setCancelled(true); return;
		 }
		 
		 if(e.getInventory().getItem(e.getSlot()).getType() != Material.AIR) {
			 UniqueSpawner spawner = core.getSpawnerFromItemStack(e.getInventory().getItem(e.getSlot()));
			 Player p = (Player)e.getWhoClicked();
			 
			 if(!spawner.isInMap()) {
				 e.setCancelled(true);
				 p.closeInventory();
				 p.sendMessage(core.getPrefix() + ChatColor.GREEN + "Le prochain bloc que vous tapperez ce transformera en ce spawner");
				 core.getPlayerWhoWillSetSpawner().put(p.getName(), spawner);
			 }
			 else {
				 e.setCancelled(true);
				 p.sendMessage(core.getPrefix() + ChatColor.GREEN + "Le spawner a été rétiré de la map.");
				 spawner.setInMap(false);
				 spawner.getLoc().getWorld().getBlockAt(spawner.getLoc()).breakNaturally();
				 spawner.setLoc(null);
				 p.closeInventory();
				 core.createSpawnerInventory(p.getName());
			 }
		 }
		 
		  
	}
	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		if(e.isCancelled()) return;
		if(e.getItemDrop().getItemStack().getType() != Material.MOB_SPAWNER) return;
		
		if(core.isUniqueSpawner(e.getItemDrop().getItemStack())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(core.getPrefix() + ChatColor.RED + "Vous ne pouvez pas jeter de spawner qui appartient a un joueur pour des raisons de sécurité.");
		}
	}
}
