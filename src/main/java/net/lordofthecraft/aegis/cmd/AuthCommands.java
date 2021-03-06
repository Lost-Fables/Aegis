package net.lordofthecraft.aegis.cmd;

import co.lotc.core.bungee.convo.ChatStream;
import co.lotc.core.bungee.util.ChatBuilder;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import de.exceptionflug.protocolize.items.InventoryManager;
import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AegisUser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import static net.md_5.bungee.api.ChatColor.*;

public class AuthCommands extends CommandTemplate {

    private Aegis plugin;
    public AuthCommands(Aegis plugin) {
        this.plugin = plugin;
    }

    public void invoke(ProxiedPlayer player, int authCode) {
        validate(plugin.getDaemon().hasAuthentication(player.getUniqueId()), "You don't have two factor authentication setup");
        validate(!plugin.getDaemon().isAuthenticated(player), "You're already authenticated");

        AegisUser user = plugin.getDaemon().getUser(player.getUniqueId());
        if (plugin.getGAuth().authorize(user.getSecretKey(), authCode)) {
	        if (plugin.getDaemon().getFirstTimeSetup().contains(player)) {
		        user.save();
		        plugin.getDaemon().getFirstTimeSetup().remove(player);
	        }

        	plugin.getDaemon().authorize(player);
        	plugin.getDaemon().sendQueuedChat(player);
	        new ChatBuilder("SUCCESSFULLY ").color(GREEN).append("authenticated!").color(AQUA).send(player);
	        new ChatBuilder("[Click me to go to the main server]").color(GOLD).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server main")).send(player);

	        InventoryManager.getInventory(player.getUniqueId()).removeItem(36);
	        InventoryManager.getInventory(player.getUniqueId()).update();

	        if (user.getScratchCodes().isEmpty()) { // This should only happen due to exporting data from Cerberus
	        	new ChatBuilder("Error: ").color(RED).append("No backup codes were found for you. Regenerating them now").color(WHITE).send(player);
		        plugin.getDaemon().sendBackupCodes(player);
	        }

	        user.updateIP(player.getAddress().getAddress().getHostAddress()); // They've authenticated so we want to record down their IP
        } else if (user.getScratchCodes().contains(authCode)) {
            plugin.getDaemon().authorize(player);
	        new ChatBuilder("SUCCESSFULLY ").color(GREEN).append("authenticated with a backup code. You're being required to setup up your authentication again.").color(AQUA).send(player);
	        plugin.getDaemon().setupUser(player);
        } else {
        	player.sendMessage(new ComponentBuilder("Error: ").color(RED).append("Invalid auth code").color(WHITE).create());
        }
    }

    @Cmd(value = "Setup 2fa", permission = "auth.setup")
    public void setup(ProxiedPlayer player) {
        validate(!plugin.getDaemon().hasAuthentication(player.getUniqueId()), "You already have two factor authentication setup. Do '/auth disable' to remove it");
        plugin.getDaemon().setupUser(player);
    }

    @Cmd(value = "Disable your 2fa")
    public void disable(ProxiedPlayer player) {
	    validate(plugin.getDaemon().hasAuthentication(player.getUniqueId()), "You don't have two factor authentication setup");
	    validate(!plugin.getDaemon().isAwaitingAuthentication(player), "You can't run that command right now");

	    new ChatStream(player).confirmPrompt().activate((context) -> {
		    plugin.getDaemon().removeUser(player.getUniqueId(), true);
		    player.disconnect(new TextComponent("2FA removed! Please relog."));
	    });

    }

    @Cmd(value = "Disable 2FA for another player", permission = "auth.disable.others")
    public void disable(ProxiedPlayer player, ProxiedPlayer target) {
	    validate(plugin.getDaemon().hasAuthentication(target.getUniqueId()), target.getName() + " doesn't have two factor authentication setup");
	    validate(!plugin.getDaemon().isAwaitingAuthentication(player), "You can't run that command right now");

	    new ChatStream(player).confirmPrompt().activate((context) -> {
		    plugin.getDaemon().removeUser(target.getUniqueId(), true);
		    target.disconnect(new ComponentBuilder("2FA removed! Please relog.").create());
		    player.sendMessage(new ComponentBuilder(target.getName()).color(GOLD).append(" has had their 2FA removed").color(AQUA).create());
	    });
    }

    @Cmd(value = "Recreate your backup codes")
    public void getBackupCodes(ProxiedPlayer player) {
	    validate(plugin.getDaemon().hasAuthentication(player.getUniqueId()), "You don't have two factor authentication setup");
	    validate(!plugin.getDaemon().isAwaitingAuthentication(player), "You can't run that command right now");

	    plugin.getDaemon().sendBackupCodes(player);
    }


}
