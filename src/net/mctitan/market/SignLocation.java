package net.mctitan.market;

import java.io.Serializable;
import java.util.Objects;
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
        return new Location(DynamicSignMarket.getInstance().getServer().getWorld(world),x,y,z);
    }
    
    @Override
    public int hashCode() {
        return world.hashCode()+(x<<19)+(y<<7)+z;
    }

    @Override
    public boolean equals(Object obj) {
        if((obj == null) || !(obj instanceof SignLocation)) {
            return false;
        }
        
        final SignLocation o = (SignLocation) obj;
        
        if(world.equals(o.world) && x == o.x && y == o.y && z == o.z)
            return true;
        
        return false;
    }
    
    @Override
    public String toString() {
        return world+"@"+x+","+y+","+z;
    }
}
