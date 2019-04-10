package net.lordofthecraft.aegis;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Events implements Listener {

    private Aegis plugin;

    public Events(Aegis plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProxyChange(ServerConnectEvent event) {
        if (plugin.daemon.isLowSecurityServer(event.getTarget())) {
            return;
        }

        if (plugin.daemon.isAuthenticated(event.getPlayer().getUniqueId())) {
            return;
        }

        Title title = ProxyServer.getInstance().createTitle();
        title.title(new ComponentBuilder("Authenticate with ").append(" /auth").color(ChatColor.RED).create());
        title.stay(50);
        event.getPlayer().sendTitle(title);

        ServerInfo lowSecurityServer = plugin.getDaemon().getLowSecurityServer();
        if (lowSecurityServer == null) {
            plugin.getLogger().warning("None of the low security servers are online!");
            return;
        }

        if (event.getReason() == ServerConnectEvent.Reason.COMMAND || event.getReason() == ServerConnectEvent.Reason.PLUGIN || event.getReason() == ServerConnectEvent.Reason.PLUGIN_MESSAGE) {
            event.setCancelled(true);
            return;
        }
        event.setTarget(lowSecurityServer);

    }
}
