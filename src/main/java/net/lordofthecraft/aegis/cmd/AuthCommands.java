package net.lordofthecraft.aegis.cmd;

import co.lotc.core.bungee.util.ChatBuilder;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AegisUser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;

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
        	plugin.getDaemon().authorize(player);
	        new ChatBuilder("SUCCESSFULLY ").color(GREEN).append("authenticated!").color(AQUA).send(player);
	        new ChatBuilder("[Click me to go to the main server]").color(GOLD).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server main")).send(player);
        } else if (user.getScratchCodes().contains(authCode)) {
            plugin.getDaemon().authorize(player);
	        new ChatBuilder("SUCCESSFULLY ").color(GREEN).append("authenticated with a backup code. You're being required to setup up your authentication again.").color(AQUA).send(player);
	        plugin.getDaemon().setupUser(player);
        } else {
        	player.sendMessage(new ComponentBuilder("Error: ").color(RED).append("Invalid auth code").color(WHITE).create());
        }
    }

    @Cmd(value = "Setup 2fa", permission = "auth.use")
    public void setup(ProxiedPlayer player) {
        validate(!plugin.getDaemon().hasUser(player.getUniqueId()), "You already have two factor authentication setup. Do '/auth disable' to remove it");
        plugin.getDaemon().setupUser(player);
    }

    @Cmd(value = "Disable your 2fa", permission = "auth.use")
    public void disable(ProxiedPlayer player) {
	    validate(plugin.getDaemon().hasUser(player.getUniqueId()), "You don't have two factor authentication setup");
	    validate(!plugin.getDaemon().isAwaitingAuthentication(player.getUniqueId()), "You can't run that command right now");

	    plugin.getDaemon().removeUser(player.getUniqueId());
	    player.disconnect(new ComponentBuilder("2FA removed! Please relog.").create());
    }

    @Cmd(value = "Disable 2FA for another player", permission = "auth.disable.others")
    public void disable(ProxiedPlayer player, ProxiedPlayer target) {
	    validate(plugin.getDaemon().hasUser(target.getUniqueId()), target.getName() + " doesn't have two factor authentication setup");
	    validate(!plugin.getDaemon().isAwaitingAuthentication(player.getUniqueId()), "You can't run that command right now");

	    plugin.getDaemon().removeUser(target.getUniqueId());
	    target.disconnect(new ComponentBuilder("2FA removed! Please relog.").create());
	    player.sendMessage(new ComponentBuilder(target.getName()).color(GOLD).append(" has had their 2FA removed").color(AQUA).create());
    }

    @Cmd(value = "Recreate your backup codes", permission = "auth.use")
    public void getBackupCodes(ProxiedPlayer player) {
	    validate(plugin.getDaemon().hasUser(player.getUniqueId()), "You don't have two factor authentication setup");
	    validate(!plugin.getDaemon().isAwaitingAuthentication(player.getUniqueId()), "You can't run that command right now");

	    List<Integer> scratchCodes = plugin.getDaemon().getUser(player.getUniqueId()).recreateScratchCodes();
	    new ChatBuilder("Your new backup codes are").color(AQUA).send(player);
	    new ChatBuilder(scratchCodes.stream().map(Object::toString).collect(Collectors.joining(" "))).color(GOLD).send(player);
    }


}
