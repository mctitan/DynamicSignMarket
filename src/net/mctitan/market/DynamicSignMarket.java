package net.mctitan.market;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicSignMarket extends JavaPlugin {
    private static DynamicSignMarket instance;
    private HashMap<Material, DynamicItem> items;
    
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        instance = this;
    }
    
    @Override
    public void onDisable() {
        
    }
    
    public static DynamicSignMarket getInstance() {
        return instance;
    }
}
