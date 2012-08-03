package net.mctitan.market;

import java.io.Serializable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;

public class DynamicSign implements Serializable {
    //members saved
    public SignLocation location;
    public Material material;
    public int amount;
    public byte data;
    
    //temporary members
    public transient double sell;
    public transient double buy;
    
    public String toString() {
        return material+" sign: "+location.toString()+" "+amount+" "+data;
    }
    
    public DynamicSign(Location location, Material material, int amount, byte data) {
        this.location = new SignLocation(location);
        this.material = material;
        this.amount = amount;
        this.data = data;
        
        init();
        DynamicSignMarket.getInstance().addSign(location, this);
    }
    
    private void init() {
        DynamicSignMarket plugin = DynamicSignMarket.getInstance();
        //register sign with
        if(plugin.getItem(material) != null) {
            plugin.getItem(material).registerSign(this);
        }
    }
    
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        init();
    }
    
    public void update(DynamicItem item) {
        sell = round(item.getSellPrice(amount));
        buy = round(item.getBuyPrice(amount));
        
        if(!(location.getLocation().getBlock().getState() instanceof Sign)) {
            //System.out.println(location.getLocation().getBlock().getType());
            return;
        }
        
        Sign sign = (Sign)(location.getLocation().getBlock().getState());
        if(sell > 0)
            sign.setLine(2, "Sell: "+sell);
        else
            sign.setLine(2, "Sell: N/A");
        if(buy > 0)
            sign.setLine(3, "Buy: "+buy);
        else
            sign.setLine(3, "Buy: N/A");
        sign.update(true);
    }
    
    private double round(double d) {
        return ((double)((int)(100*d)))/100;
    }
    
    @Override
    public int hashCode() {
        return location.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        else if(obj instanceof DynamicSign)
            return location.equals(((DynamicSign)obj).location);
        else if(obj instanceof SignLocation)
            return location.equals(obj);
        
        return false;
    }
}
