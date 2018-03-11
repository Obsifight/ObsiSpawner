package fr.thisismac.spawner;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnerCommand implements CommandExecutor{

	private Core core;
	
	public SpawnerCommand(Core c) {
		this.core = c;
	}
	
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String name, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player)sender;
			
			// Hes op and
			if(args != null && args.length == 3 && p.isOp()) {
				
				// want to add spawner from player
				if(args[0].equalsIgnoreCase("add")) {
					if(EntityType.fromName(args[1]) == null) {
						p.sendMessage(core.getPrefix() + ChatColor.RED + "Le type de spawner que vous essayez d'utiliser n'existe pas."); return true;
					}
					else if(args[2] == null || !Bukkit.getOfflinePlayer(args[2]).hasPlayedBefore()) {
						p.sendMessage(core.getPrefix() + ChatColor.RED + "Le joueur a qui vous voulez donné le spawner ne s'est jamais connecté"); return true;
					}
					String pName = args[2];
					
						UniqueSpawner sp = new UniqueSpawner(pName, EntityType.fromName(args[1]).getTypeId(), null, core.getSpawners().size() + 1, false);
						core.getSpawners().add(sp);
						
						if(core.getPlayerSpawners().get(pName) == null) {
							core.getPlayerSpawners().put(pName, new ArrayList<UniqueSpawner>());
						}
						core.getPlayerSpawners().get(pName).add(sp);
						
						Bukkit.getPlayer(pName).sendMessage(core.getPrefix() + ChatColor.GREEN + "Vous venez de recevoir un spawner de " + args[1] + ", pour l'utiliser /" + core.cmd);
						return true;
						
				}
				// want to remove spawner from player
				else if(args[0].equalsIgnoreCase("remove")) {
					if(!core.getPlayerSpawners().containsKey(args[2])) {
						p.sendMessage(core.getPrefix() + ChatColor.RED + "Le joueur n'a pas de spawner personnel.");
						return true;
					}
					for(UniqueSpawner sp : core.getPlayerSpawners().get(args[2])) {
						if(sp.getType() == EntityType.fromName(args[1]).getTypeId()) {
							if(sp.isInMap()) {
								sp.getLoc().getWorld().getBlockAt(sp.getLoc()).breakNaturally();
							}
							core.getPlayerSpawners().get(args[2]).remove(sp);
							p.sendMessage(core.getPrefix() + ChatColor.GREEN + "Le spawner a bien été retiré des spawners de " + args[2] + " et a été retiré de la map.");
							return true;
						}
					}
					p.sendMessage(core.getPrefix()+ ChatColor.RED + "Aucun spawner du type " + args[1] + " a été trouvé pour le joueur " + args[2]);
				}
				
			}
			
			// Open himself spawner's inventory
			else if(args.length == 0){
				if(core.getPlayerSpawners().get(p.getName()) != null) {
					p.openInventory(core.createSpawnerInventory(p.getName()));
				}
				else {
					p.sendMessage(core.getPrefix() + ChatColor.RED + "Vous n'avez pas de spawner personnel."); return true;
				}
				
				return false;
			}
		 
		}
		else {
			// Hes console and
			if(args != null && args.length == 3) {
				
				// want to add spawner from player
				if(args[0].equalsIgnoreCase("add")) {
					if(EntityType.fromName(args[1]) == null) {
						core.logWithColor(core.getPrefix() + ChatColor.RED + "Le type de spawner que vous essayez d'utiliser n'existe pas."); return true;
					}
					else if(args[2] == null) {
						core.logWithColor(core.getPrefix() + ChatColor.RED + "Le joueur a qui vous voulez donné le spawner ne s'est jamais connecté"); return true;
					}
					String pName = args[2];
					
						UniqueSpawner sp = new UniqueSpawner(pName, EntityType.fromName(args[1]).getTypeId(), null, core.getSpawners().size() + 1, false);
						core.getSpawners().add(sp);
						
						if(core.getPlayerSpawners().get(pName) == null) {
							core.getPlayerSpawners().put(pName, new ArrayList<UniqueSpawner>());
						}
						core.getPlayerSpawners().get(pName).add(sp);
						core.logWithColor(core.getPrefix() + ChatColor.GREEN + "Vous avez ajouté un spawner de " + args[1] + " a la collection de " + args[2]);
						Bukkit.getPlayer(pName).sendMessage(core.getPrefix() + ChatColor.GREEN + "Vous venez de recevoir un spawner de " + args[1] + ", pour l'utiliser /" + core.cmd);
						return true;
						
				}
				// want to remove spawner from player
				else if(args[0].equalsIgnoreCase("remove")) {
					if(!core.getPlayerSpawners().containsKey(args[2])) {
						core.logWithColor(core.getPrefix() + ChatColor.RED + "Le joueur n'a pas de spawner personnel.");
						return true;
					}
					for(UniqueSpawner sp : core.getPlayerSpawners().get(args[2])) {
						if(sp.getType() == EntityType.fromName(args[1]).getTypeId()) {
							if(sp.isInMap()) {
								sp.getLoc().getWorld().getBlockAt(sp.getLoc()).breakNaturally();
							}
							core.getPlayerSpawners().get(args[2]).remove(sp);
							core.logWithColor(core.getPrefix() + ChatColor.GREEN + "Le spawner a bien été retiré des spawners de " + args[2] + " et a été retiré de la map.");
							return true;
						}
					}
					core.logWithColor(core.getPrefix()+ ChatColor.RED + "Aucun spawner du type " + args[1] + " a été trouvé pour le joueur " + args[2]);
				}
				
			}
}
		
		return true;
	}

}
