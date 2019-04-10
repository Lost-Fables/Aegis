package net.lordofthecraft.aegis.cmd;

import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AegisUser;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import static net.md_5.bungee.api.ChatColor.*;

public class AuthCommands extends CommandTemplate {

    private Aegis plugin;
    public AuthCommands(Aegis plugin) {
        this.plugin = plugin;
    }

    @Cmd(value = "Authenticate user using a ToTP", permission = "auth.use")
    public void auth(ProxiedPlayer player, int authCode) {
        if (!plugin.getDaemon().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(new ComponentBuilder("Error: ").color(RED).append("You're already authenticated").color(WHITE).create());
            return;
        }
        AegisUser user = plugin.getDaemon().getUser(player.getUniqueId());
        if (plugin.getGAuth().authorize(user.getSecretKey(), authCode)) {

        } else {

        }
    }

    @Cmd(value = "Setup 2fa", permission = "auth.use")
    public void setup(ProxiedPlayer player) {
        if (!plugin.getDaemon().isAuthenticated(player.getUniqueId())) {
            player.sendMessage(new ComponentBuilder("Error: ").color(RED)
                                                              .append("You're already authenticated. Use ").color(WHITE)
                                                              .append("/auth disable").color(AQUA)
                                                              .append(" to disable your currently authentication").color(WHITE).create());
            return;
        }
        plugin.getDaemon().setupUser(player);
    }

}
