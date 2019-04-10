package net.lordofthecraft.aegis;

import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import static net.md_5.bungee.api.ChatColor.*;

public class AegisCommands extends CommandTemplate {

    private Aegis plugin;
    public AegisCommands(Aegis plugin) {
        this.plugin = plugin;
    }

    @Cmd(value = "Authenticate user using a ToTP", permission = "auth.use")
    public void auth(ProxiedPlayer player, int authCode) {
        if (!plugin.daemon.isAuthenticated(player.getUniqueId())) {
            player.sendMessage(new ComponentBuilder("Error: ").color(RED).append("You're already authenticated").color(WHITE).create());
            return;
        }
        AegisUser user = plugin.daemon.getUser(player.getUniqueId());
        if (plugin.gAuth.authorize(user.getSecretKey(), authCode)) {

        } else {

        }
    }

    @Cmd(value = "Setup 2fa", permission = "auth.use")
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
