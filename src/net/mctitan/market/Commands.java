package net.mctitan.market;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
    DynamicSignMarket plugin;
    
    public Commands() {
        plugin = DynamicSignMarket.getInstance();
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(!cs.hasPermission("dynamicmarket.admin")) {
            cs.sendMessage("You do not have permissions for this command");
            return true;
        }
        
        if(strings.length == 0)
            return true;
        
        String sub = strings[0];
        
        if(sub.equals("add") && strings.length == 7)
            addItem(cs,strings);
        else if(sub.equals("rm") && strings.length == 2)
            rmItem(cs, strings[1]);
        else if(sub.equals("list"))
            listItems(cs);
        else if(sub.equals("check") && strings.length == 2)
            checkItem(cs,strings[1]);
        
        return true;
    }
    
    public void addItem(CommandSender sender, String[] args) {
        Material mat = null;
        double basePrice = -1;
        int baseCount = -1;
        double sellMulti = -1;
        double incMulti = -1;
        int count = -1;
        
        //get material
        mat = Material.getMaterial(args[1]);
        
        //get other params
        try {
            basePrice = Double.parseDouble(args[2]);
            baseCount = Integer.parseInt(args[3]);
            sellMulti = Double.parseDouble(args[4]);
            incMulti = Double.parseDouble(args[5]);
            count = Integer.parseInt(args[6]);
        } catch(Exception e) {}
        
        //do some basic checking
        if(mat == null) {
            sender.sendMessage("Invalid material given!");
            return;
        }
        
        if(basePrice < 0 || baseCount < 0 || sellMulti < 0 || incMulti < 0 || count < 0) {
            sender.sendMessage("Invalid parameter given!");
            return;
        }
        
        //add the item
        plugin.addItem(mat, new DynamicItem(mat,basePrice,baseCount,sellMulti,incMulti,count));
        sender.sendMessage(mat.name()+" added!");
    }
    
    public void rmItem(CommandSender sender, String sMaterial) {
        Material mat = null;
        
        //get the material
        mat = Material.getMaterial(sMaterial);
        
        //do some basic checking
        if(mat == null) {
            sender.sendMessage("Invalid material given!");
            return;
        }
        
        //remove the item
        plugin.rmItem(mat);
        sender.sendMessage(mat.name()+" removed!");
    }
    
    public void listItems(CommandSender sender) {
        String output = "";
        for(Material mat : plugin.getItems())
            output += mat.name()+" ";
        sender.sendMessage(output);
    }
    
    public void checkItem(CommandSender sender, String sMaterial) {
        Material mat = null;
        DynamicItem item = null;
        
        //get the material
        mat = Material.getMaterial(sMaterial);
        
        //get the item
        item = plugin.getItem(mat);
        
        //do some basic checking
        if(mat == null || item == null) {
            sender.sendMessage("Invalid material given!");
            return;
        }
        
        //output answer to the sender
        sender.sendMessage("Item: "+mat.name());
        sender.sendMessage("     Base Price: "+item.basePrice);
        sender.sendMessage("     Base Count: "+item.baseCount);
        sender.sendMessage("     Sell Multi: "+item.sellMulti);
        sender.sendMessage("     Inc Multi: "+item.incMulti);
        sender.sendMessage("     Count: "+item.count);
    }
}
