package net.mctitan.market;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SignListener implements Listener {
    private DynamicSignMarket plugin;
    
    public SignListener() {
        plugin = DynamicSignMarket.getInstance();
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        DynamicSign sign = plugin.getSign(event.getBlock().getLocation());
        
        if(!event.getPlayer().hasPermission("DSM.breaksign"))
            return;
        
        if(sign != null)
            plugin.rmSign(sign);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        
        if(event.isCancelled())
            return;
        
        if(action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK)
            return;
        
        if(!(event.getClickedBlock().getState() instanceof Sign))
            return;
        
        DynamicSign dsign = plugin.getSign(event.getClickedBlock().getLocation());
        if(dsign == null)
            return;
        
        DynamicItem item = plugin.getItem(dsign.material);
        if(item == null)
            return;
        
        if(action == Action.LEFT_CLICK_BLOCK && dsign.buy > 0)
            buy(event.getPlayer(), dsign, item);
        else if(action == Action.RIGHT_CLICK_BLOCK && dsign.sell > 0)
            sell(event.getPlayer(), dsign, item);
        
        event.setCancelled(true);
    }
    
    private void buy(Player player, DynamicSign sign, DynamicItem item) {
        double balance = plugin.economy.getBalance(player.getName());
        if(balance < sign.buy) {
            player.sendRawMessage("Not enough money!");
            return;
        }
        
        //send confirmation
        player.sendRawMessage("Bought "+sign.amount+" "+sign.material.toString().toLowerCase()+
                              (sign.data==0?"":":"+sign.data)+" for "+sign.buy);
        
        //remove the money
        plugin.economy.withdrawPlayer(player.getName(), sign.buy);
        
        //actually buy the item
        item.buy(sign.amount);
        
        //give the items to the player
        player.getInventory().addItem(new ItemStack(sign.material,sign.amount,(short)0,sign.data));
    }
    
    private void sell(Player player, DynamicSign sign, DynamicItem item) {
        //check that the player has the items
        if(!checkInventory(player,sign.material,sign.data,sign.amount)) {
            player.sendRawMessage("Not enough items!");
            return;
        }
        
        //send confirmation message
        player.sendRawMessage("Sold "+sign.amount+" "+sign.material.toString().toLowerCase()+
                              (sign.data==0?"":":"+sign.data)+" for "+sign.sell);
        
        //add the money
        plugin.economy.depositPlayer(player.getName(), sign.sell);
        
        //actually sell the items
        item.sell(sign.amount);
        
        //remove the items from the inventory
        removeFromInventory(player,sign.material,sign.data,sign.amount);
    }
    
    public boolean checkInventory(Player player, Material mat, byte data, int amount) {
        int temp = 0;
        for(ItemStack is : player.getInventory().getContents())
            if(is == null)
                continue;
            else if(is.getType() == mat && is.getData().getData() == data)
                temp += is.getAmount();
        
        return temp >= amount;
    }
    
    public void removeFromInventory(final Player player, Material mat, byte data, int amount) {
        ItemStack temp;
        
        for(int i = 0; i < player.getInventory().getSize(); ++i) {
            if(amount == 0)
                break;
            
            temp = player.getInventory().getItem(i);
            if(temp == null)
                continue;
            
            if(temp.getType() == mat && temp.getData().getData() == data)
                if(amount >= temp.getAmount()) {
                    amount -= temp.getAmount();
                    player.getInventory().clear(i);
                } else {
                    final ItemStack change = new ItemStack(mat,temp.getAmount()-amount,(short)0,data);
                    final int pos = i;
                    player.getInventory().clear(i);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                        public void run() {
                            player.getInventory().setItem(pos, change);
                            //player.getInventory().addItem(change);
                        }
                    });
                    return;
                }
        }
        
        player.updateInventory();
    }
    
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPermission("dynamicsignmarket.admin"))
            return;
        
        String[] lines = event.getLines();
        Material material;
        byte data = 0;
        int amount = 1;
        
        if(!lines[0].equals(plugin.header))
            return;
        
        material = Material.getMaterial(lines[1]);
        if(material == null) {
            //material type error
            player.sendRawMessage("Invalid material");
            return;
        }
        
        //check to see if the material is a registered item
        if(plugin.getItem(material) == null) {
            //material not an item error
            player.sendRawMessage("Material is not a sellable item");
            return;
        }
        
        //get the meta data
        try {
            data = Byte.parseByte(lines[3]);
        } catch(Exception e) {
            //report error to player
            player.sendRawMessage("Invalid meta-data");
            return;
        }
        
        //get the amount of items per sale
        try {
            amount = Integer.parseInt(lines[2]);
        } catch(Exception e) {
            //report error to player
            player.sendRawMessage("Invalid amount");
            return;
        }
        
        //make the new sign
        DynamicSign sign = new DynamicSign(event.getBlock().getLocation(),
                                           material, amount, data);
        
        //change lines
        lines[1] = amount+" "+(data == 0?"":data+":")+material.name().toLowerCase();
    }
}
