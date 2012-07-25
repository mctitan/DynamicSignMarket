package net.mctitan.market;

import java.io.Serializable;
import org.bukkit.Location;

public class SignLocation implements Serializable {
    public String world;
    public int x;
    public int y;
    public int z;
    
    public SignLocation(Location loc) {
        world = loc.getWorld().getName();
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
    }
    
    public Location getLocation() {
        return new Location(DynamicSignMarket.getInstance().getServer().getWorld(world),
                            x,y,z);
    }
}
