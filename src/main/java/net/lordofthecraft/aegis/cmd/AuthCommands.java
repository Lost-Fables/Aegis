package net.lordofthecraft.aegis.cmd;

import co.lotc.core.command.CommandTemplate;
import co.lotc.core.command.annotate.Cmd;
import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AegisUser;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AuthCommands extends CommandTemplate {

    private Aegis plugin;
    public AuthCommands(Aegis plugin) {
        this.plugin = plugin;
    }

    @Cmd(value = "Authenticate user using a ToTP", permission = "auth.use")
    public void auth(ProxiedPlayer player, int authCode) {
        validate(plugin.getDaemon().hasUser(player.getUniqueId()), "You don't have two factor authentication setup");
        validate(!plugin.getDaemon().isAuthenticated(player.getUniqueId()), "You're already authenticated");


        AegisUser user = plugin.getDaemon().getUser(player.getUniqueId());
        if (plugin.getGAuth().authorize(user.getSecretKey(), authCode)) {

        } else {

        }
    }

    @Cmd(value = "Setup 2fa", permission = "auth.use")
    public void setup(ProxiedPlayer player) {
        validate(!plugin.getDaemon().isAuthenticated(player.getUniqueId()), "You're already authenticated");
        plugin.getDaemon().setupUser(player);
    }

}
