package net.lordofthecraft.aegis.listener;

import net.lordofthecraft.aegis.Aegis;
import net.lordofthecraft.aegis.AegisUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Events implements Listener {

    private Aegis plugin;

    public Events(Aegis plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProxyChange(ServerConnectEvent event) {
        if (plugin.getDaemon().isLowSecurityServer(event.getTarget())) {
            return;
        }

        if (plugin.getDaemon().isAuthenticated(event.getPlayer())) {
            return;
        }

        sendTitle(event.getPlayer());

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


    @EventHandler
    public void onLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (!plugin.getDaemon().hasAuthentication(player.getUniqueId())) {
            if (player.hasPermission("auth.required")) {
                plugin.getDaemon().setupUser(player);
            }
            return;
        }
        AegisUser user = plugin.getDaemon().getUser(player.getUniqueId());
        if (System.currentTimeMillis() - user.getLastAuthenticated() > TimeUnit.DAYS.toMillis(plugin.getConfig().getInt("daysBetweenAuthenticating", 7)) || !user.isRecentIP(player.getAddress().getAddress().getHostAddress())) {
            plugin.getDaemon().requireAuthentication(player);
            sendTitle(player);
        }


        // This is terrible code and I'm sorry. I blame Fireheart

        Map.Entry<String, Long> oldestIP = null;
        while (user.getLastKnownIPs().size() > plugin.getConfig().getInt("savedIPs", 5)) {
            for (Map.Entry<String, Long> ipEntry : user.getLastKnownIPs().entrySet()) {

                if (oldestIP == null || ipEntry.getValue() < oldestIP.getValue()) {
                    oldestIP = ipEntry;
                }
            }
            user.getLastKnownIPs().remove(oldestIP.getKey(), oldestIP.getValue());
        }
        user.saveConfig();
    }

    @EventHandler
    public void logout(PlayerDisconnectEvent event) {
        plugin.getDaemon().removeAwaitingAuthentication(event.getPlayer());
        if (plugin.getDaemon().getFirstTimeSetup().contains(event.getPlayer())) {
            plugin.getDaemon().removeUser(event.getPlayer().getUniqueId(), false);
            plugin.getDaemon().getFirstTimeSetup().remove(event.getPlayer());
        }
    }

    private void sendTitle(ProxiedPlayer player) {
        Title title = ProxyServer.getInstance().createTitle();
        title.title(new ComponentBuilder("").create());
        title.subTitle(new ComponentBuilder("Authenticate with ").append(" /auth").color(ChatColor.RED).create());
        title.stay(200);
        player.sendTitle(title);
    }
}
