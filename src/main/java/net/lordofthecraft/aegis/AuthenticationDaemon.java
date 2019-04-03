package net.lordofthecraft.aegis;

import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntTag;
import com.google.common.io.Files;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import de.exceptionflug.protocolize.items.InventoryManager;
import de.exceptionflug.protocolize.items.ItemStack;
import de.exceptionflug.protocolize.items.ItemType;
import de.exceptionflug.protocolize.items.PlayerInventory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class AuthenticationDaemon {
    public static final int AUTHENTHICATION_MAP_ID = 1337;
    
    private Aegis plugin;
    private Set<UUID> awaitingAuthentication;
    private HashMap<UUID, AegisUser> users;
    private ServerInfo limboServer;

    public AuthenticationDaemon(Aegis plugin) {
        this.plugin = plugin;
        loadUsers();
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
        users.put(player.getUniqueId(), aegisUser);
    }

    public AegisUser getUser(UUID uuid) {
        return users.get(uuid);
    }


    public void loadUsers() {
        File folder = new File(plugin.getDataFolder() + File.separator + "users");
        folder.mkdirs();

        for (File userFile : folder.listFiles()) {
            if (Files.getFileExtension(userFile.getName()).equals("yml")) {
                try {
                    Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(userFile);
                    users.put(UUID.fromString(config.getString("uuid")), new AegisUser(config));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setupUser(ProxiedPlayer player) {

        sendMap(player);
    }

    private byte[] getQRCode(ProxiedPlayer pp, String secret, String topbar) {
        return new QRRenderer(pp.getName(), secret, topbar).render();
    }
    
    private void sendMap(ProxiedPlayer player) {
        
        byte[] data = getQRCode(player, "idk", "idk");//TODO
        MapData md = new MapData(AUTHENTHICATION_MAP_ID, (byte) 0, false, new MapData.Icon[0],
                (byte) 128, (byte) 128, (byte) 0, (byte) 0, data);
        player.unsafe().sendPacket(md);

        final PlayerInventory inventory = InventoryManager.getInventory(player.getUniqueId());

        final ItemStack map = new ItemStack(ItemType.FILLED_MAP);
        CompoundTag compoundTag = (CompoundTag) map.getNBTTag();
        compoundTag.getValue().put("tag", new IntTag("map", 1));
        map.setNBTTag(compoundTag);

        inventory.update();
    }
}
