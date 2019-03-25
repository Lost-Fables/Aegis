package net.lordofthecraft.aegis;

import static net.md_5.bungee.api.ChatColor.AQUA;
import static net.md_5.bungee.api.ChatColor.RED;
import static net.md_5.bungee.api.ChatColor.WHITE;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@CommandAlias("auth")
public class AegisCommands extends BaseCommand {


    @Dependency
    private Aegis plugin;


    @Default
    @Description("Use to authenticate yourself")
    @CommandPermission("aegis.auth")
    public void auth(ProxiedPlayer player, Integer authCode) {
        if (!plugin.daemon.isAuthenticated(player.getUniqueId())) {
            player.sendMessage(new ComponentBuilder("Error: ").color(RED).append("You're already authenticated").color(WHITE).create());
            return;
        }
        AegisUser user = plugin.daemon.getUser(player.getUniqueId());
        if (plugin.gAuth.authorize(user.getSecretKey(), authCode)) {

        } else {

        }
    }

    @Subcommand("setup")
    public void setup(ProxiedPlayer player) {
        if (!plugin.daemon.isAuthenticated(player.getUniqueId())) {
            player.sendMessage(new ComponentBuilder("Error: ").color(RED)
                                                              .append("You're already authenticated. Use ").color(WHITE)
                                                              .append("/auth disable").color(AQUA)
                                                              .append(" to disable your currently authentication").color(WHITE).create());
            return;
        }
        plugin.daemon.setupUser(player);
    }

}
