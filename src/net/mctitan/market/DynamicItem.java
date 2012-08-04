package net.mctitan.market;

import java.util.HashSet;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class DynamicItem {
    //members saved
    public Material item;
    public double basePrice;
    public int baseCount;
    public double sellMulti;
    public double incMulti;
    public int count;
    
    //temporary members
    private double price;
    private double increment;
    private int maxCount;
    private HashSet<DynamicSign> signs;
    
    public DynamicItem(Material item, FileConfiguration config) {
        this.item = item;
        if(config != null)
            load(config);
        
        calcs();
        signs = new HashSet<>();
    }
    
    public DynamicItem(Material item, double bp, int bc, double sm,
                       double im, int c) {
        this.item = item;
        basePrice = bp;
        baseCount = bc;
        sellMulti = sm;
        incMulti = im;
        count = c;
        
        calcs();
        signs = new HashSet<>();
    }
    
    private void calcs() {
        increment = incMulti*basePrice/baseCount;
        price = basePrice*(incMulti*(baseCount-count)/baseCount+1);
        maxCount = (incMulti>0 ? (int)(baseCount*(1+1/incMulti)) : Integer.MAX_VALUE);
    }
    
    public void buy(int amount) {
        count -= amount;
        calcs();
        updateSigns();
    }
    
    public void sell(int amount) {
        count += amount;
        calcs();
        updateSigns();
    }
    
    public double getBuyPrice(int amount) {
        if((count - amount) < 0)
            return -1;
        return (price+increment)*amount+(amount*(amount-1)/2)*increment;
    }
    
    public double getSellPrice(int amount) {
        if((count + amount) > maxCount)
            return -1;
        return sellMulti*(price*amount-(amount*(amount-1)/2)*increment);
    }
    
    public void load(FileConfiguration config) {
        basePrice = config.getDouble("Items."+item.name()+".basePrice");
        baseCount = config.getInt("Items."+item.name()+".baseCount");
        sellMulti = config.getDouble("Items."+item.name()+".sellMulti");
        incMulti = config.getDouble("Items."+item.name()+".incMulti");
        count = config.getInt("Items."+item.name()+".count");
    }
    
    public void save(FileConfiguration config) {
        config.set("Items."+item.name()+".basePrice", basePrice);
        config.set("Items."+item.name()+".baseCount", baseCount);
        config.set("Items."+item.name()+".sellMulti", sellMulti);
        config.set("Items."+item.name()+".incMulti", incMulti);
        config.set("Items."+item.name()+".count", count);
    }
    
    public void registerSign(final DynamicSign sign) {
        signs.add(sign);
        DynamicSignMarket plugin = DynamicSignMarket.getInstance();
        final DynamicItem item = this;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                sign.update(item);
            }
        }, 1);
    }
    
    public void removeSign(DynamicSign sign) {
        signs.remove(sign);
    }
    
    public void updateSigns() {
        for(DynamicSign sign : signs)
            sign.update(this);
    }
    
    public void removeAllSigns() {
        for(DynamicSign sign : signs) {
            DynamicSignMarket.getInstance().rmSign(sign);
            sign.location.getLocation().getBlock().setType(Material.AIR);
        }
    }
}
