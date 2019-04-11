package net.lordofthecraft.aegis.cmd;

import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntTag;

import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import de.exceptionflug.protocolize.items.InventoryManager;
import de.exceptionflug.protocolize.items.ItemStack;
import de.exceptionflug.protocolize.items.ItemType;
import de.exceptionflug.protocolize.items.PlayerInventory;
import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AegisUser;
import net.lordofthecraft.aegis.MapData;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AuthCommands extends CommandTemplate {

    private Aegis plugin;
    public AuthCommands(Aegis plugin) {
        this.plugin = plugin;
    }

    public void invoke(ProxiedPlayer player, int authCode) {
        validate(plugin.getDaemon().hasUser(player.getUniqueId()), "You don't have two factor authentication setup");
        validate(!plugin.getDaemon().isAuthenticated(player.getUniqueId()), "You're already authenticated");


        AegisUser user = plugin.getDaemon().getUser(player.getUniqueId());
        if (plugin.getGAuth().authorize(user.getSecretKey(), authCode)) {

        } else {
            // TODO: Implement scratch codes. Should instantly have them setup a new totp
        }
    }

    @Cmd(value = "Setup 2fa", permission = "auth.use")
    public void setup(ProxiedPlayer player) {
        validate(!plugin.getDaemon().hasUser(player.getUniqueId()), "You already have two factor authentication setup. Do '/auth disable' to remove it");
        plugin.getDaemon().setupUser(player);
        
        
    }
    
    @Cmd("fuuuuuuuuuuuuuu")
    public void debug(ProxiedPlayer player, int col, int row) {
    	byte empty = 0;
    	byte[] things = new byte[col*row];
    	for(int i = 0; i < things.length; i++) {
    		things[i] = (byte) 8;
    	}
    	MapData md = new MapData(1337, empty, false, new MapData.Icon[0], (byte) col, (byte) row, empty, empty, things);
    	player.unsafe().sendPacket(md);
    	
      final PlayerInventory inventory = InventoryManager.getInventory(player.getUniqueId());

      final ItemStack map = new ItemStack(ItemType.FILLED_MAP);
      CompoundTag compoundTag = (CompoundTag) map.getNBTTag();
      compoundTag.getValue().put("tag", new IntTag("map", 1337));
      map.setNBTTag(compoundTag);
      inventory.setItem(36, map);
      inventory.update();
    }

}
