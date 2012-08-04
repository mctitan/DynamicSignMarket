package net.mctitan.market;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DynamicSignMarket extends JavaPlugin {
    private static DynamicSignMarket instance;
    private HashMap<Material, DynamicItem> items;
    private HashMap<Location, DynamicSign> signs;
    public String header;
    public Economy economy;
    
    @Override
    public void onEnable() {
        instance = this;
        Material material;
        
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        items = new HashMap<>();
        if(getConfig().getConfigurationSection("Items") != null)
            for(String str : getConfig().getConfigurationSection("Items").getKeys(false)) {
                material = Material.getMaterial(str);
                if(material == null)
                    continue;
                items.put(material, new DynamicItem(material, getConfig()));
            }
        header = getConfig().getString("SignHeader");
        
        signs = new HashMap<>();
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getSignFile()));
            for(DynamicSign sign : (Collection<DynamicSign>)ois.readObject()) {
                Material mat = sign.location.getLocation().getBlock().getType();
                if(mat != Material.SIGN_POST || mat != Material.WALL_SIGN)
                    rmSign(sign);
                signs.put(sign.location.getLocation(), sign);
            }
        } catch(Exception e) {}
        
        setupEconomy();
        
        this.getCommand("ds").setExecutor(new Commands());
        
        getServer().getPluginManager().registerEvents(new SignListener(), this);
    }
    
    private File getSignFile() {
        File folder = getDataFolder();
        if(!folder.exists())
            folder.mkdir();
        
        return new File(folder.getPath()+File.separator+"signs.dat");
    }
    
    @Override
    public void onDisable() {
        for(Material material : items.keySet())
            items.get(material).save(getConfig());
        saveConfig();
        
        HashSet<DynamicSign> output = new HashSet<>();
        for(DynamicSign sign : signs.values())
            output.add(sign);
        
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getSignFile()));
            oos.writeObject(output);
        } catch(Exception e) {}
    }
    
    public static DynamicSignMarket getInstance() {
        return instance;
    }
    
    public DynamicItem getItem(Material material) {
        return items.get(material);
    }
    
    public Set<Material> getItems() {
        return items.keySet();
    }
    
    public void addItem(Material mat, DynamicItem item) {
        items.put(mat,item);
        item.save(getConfig());
        saveConfig();
    }
    
    public void rmItem(Material mat) {
        getItem(mat).removeAllSigns();
        items.remove(mat);
        getConfig().getConfigurationSection("Items").set(mat.name(), null);
        saveConfig();
    }
    
    public void rmItem(DynamicItem item) {
        rmItem(item.item);
    }
    
    public DynamicSign getSign(Location location) {
        return signs.get(location);
    }
    
    public void addSign(Location location, DynamicSign sign) {
        signs.put(location, sign);
    }
    
    public void rmSign(DynamicSign sign) {
        signs.remove(sign.location.getLocation());
        getItem(sign.material).removeSign(sign);
    }
    
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
}
