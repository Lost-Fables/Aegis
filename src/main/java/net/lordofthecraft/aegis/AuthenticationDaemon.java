package net.lordofthecraft.aegis;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AuthenticationDaemon {

    private Aegis plugin;
    private Set<UUID> awaitingAuthentication;
    private HashMap<UUID, AegisUser> cachedUsers;
    private ServerInfo limboServer;

    public AuthenticationDaemon(Aegis plugin) {
        this.plugin = plugin;
    }

    public boolean isAuthenticated(UUID uuid) {
        return !awaitingAuthentication.contains(uuid);
    }

    public boolean isLimboServer(ServerInfo server) {
        return limboServer.equals(server);
    }

    public boolean isLimboServer(String name) {
        return limboServer.getName().equalsIgnoreCase(name);
    }

    public ServerInfo getLimboServer() {
        return limboServer;
    }

    public void createAuthentication(ProxiedPlayer player) {
        final GoogleAuthenticatorKey key = plugin.gAuth.createCredentials();
        AegisUser  aegisUser = new AegisUser(player, key.getKey(), key.getScratchCodes());
        cachedUsers.put(player.getUniqueId(), aegisUser);
    }

    public AegisUser getUser(UUID uuid) {
        //TODO:
    }

}
