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

}
