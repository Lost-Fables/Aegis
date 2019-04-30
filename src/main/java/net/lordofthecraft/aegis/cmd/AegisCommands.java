package net.lordofthecraft.aegis.cmd;

import co.lotc.core.bungee.util.ChatBuilder;
import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AegisUser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.AQUA;

public class AegisCommands extends CommandTemplate {

	Aegis plugin;

	public AegisCommands(Aegis plugin) {
		this.plugin = plugin;
	}

	@Cmd("Gets information about an aegis user")
	public void info(ProxiedPlayer player, ProxiedPlayer target) {
		validate(plugin.getDaemon().hasAuthentication(target.getUniqueId()), target.getName() + " doesn't have two factor authentication setup");
		AegisUser user = plugin.getDaemon().getUser(target.getUniqueId());

		new ChatBuilder("Last Authenticated: ").append(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(user.getLastAuthenticated())).newline()
				.append("Last Known IPs: ").append(user.getLastKnownIPs().keySet().stream().map(s -> s.replaceAll("-", ".")).collect(Collectors.joining(" ")))
		.send(player);
	}

	@Cmd("Reload users from disk")
	public void reloadUsers(CommandSender sender) {
		plugin.loadConfig();
		plugin.getDaemon().loadUsers();
		new ChatBuilder("Users reloaded!").color(AQUA).send(sender);
	}
}
