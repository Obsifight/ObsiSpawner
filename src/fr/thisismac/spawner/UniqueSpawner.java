package fr.thisismac.spawner;

import org.bukkit.Location;

public class UniqueSpawner {
	
	private String player;
	private short type;
	private Location loc;
	private int key;
	private boolean inMap;
	
	public UniqueSpawner(String p, short type, Location l, int key, boolean posed) {
		setPlayer(p);
		setType(type);
		setLoc(l);
		setKey(key);
		setInMap(posed);
	}

	public int getType() {
		return type;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public void setType(short type) {
		this.type = type;
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public boolean isInMap() {
		return inMap;
	}

	public void setInMap(boolean inMap) {
		this.inMap = inMap;
	}
}
