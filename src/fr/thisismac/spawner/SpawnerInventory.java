package fr.thisismac.spawner;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SpawnerInventory implements InventoryHolder {

    private String player;

    public SpawnerInventory(String player) {
        this.player = player;
    }
    
    public String getPlayer() {
        return player;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
