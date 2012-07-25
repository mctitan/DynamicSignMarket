package net.mctitan.market;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class DynamicItem {
    //members saved
    private Material item;
    private double basePrice;
    private int baseCount;
    private double sellMulti;
    private double incMulti;
    private int count;
    
    //temporary members
    private double price;
    private double increment;
    
    public DynamicItem(Material item, FileConfiguration config) {
        this.item = item;
        load(config);
        
        calcIncrement();
        calcPrice();
    }
    
    private void calcIncrement() {
        increment = incMulti*basePrice/baseCount;
    }
    
    private void calcPrice() {
        price = basePrice*(incMulti*(baseCount-count)/baseCount+1);
    }
    
    public void buy(int amount) {
        count -= amount;
        calcIncrement();
        calcPrice();
    }
    
    public void sell(int amount) {
        count += amount;
        calcIncrement();
        calcPrice();
    }
    
    public double getBuyPrice(int amount) {
        return (price+increment)*amount+(amount*(amount-1)/2)*increment;
    }
    
    public double getSellPrice(int amount) {
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
        config.set("Items."+item.name()+".item", item);
        config.set("Items."+item.name()+".basePrice", basePrice);
        config.set("Items."+item.name()+".baseCount", baseCount);
        config.set("Items."+item.name()+".sellMulti", sellMulti);
        config.set("Items."+item.name()+".incMulti", incMulti);
        config.set("Items."+item.name()+".count", count);
    }
}
