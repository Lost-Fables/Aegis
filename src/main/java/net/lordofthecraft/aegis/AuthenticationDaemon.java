package net.lordofthecraft.aegis;

import co.lotc.core.bungee.util.ChatBuilder;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.IntTag;
import com.google.common.io.Files;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import de.exceptionflug.protocolize.items.InventoryManager;
import de.exceptionflug.protocolize.items.ItemStack;
import de.exceptionflug.protocolize.items.ItemType;
import de.exceptionflug.protocolize.items.PlayerInventory;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.AQUA;
import static net.md_5.bungee.api.ChatColor.GOLD;

public class AuthenticationDaemon {
    public static final int AUTHENTHICATION_MAP_ID = 1337;
    
    private Aegis plugin;
    private Set<UUID> awaitingAuthentication;
    private Map<UUID, AegisUser> users;
    @Getter
    private List<String> lowSecurityServers;

    public AuthenticationDaemon(Aegis plugin) {
        this.plugin = plugin;
        loadUsers();
        awaitingAuthentication = new HashSet<>();
        users = new HashMap<>();

        lowSecurityServers = plugin.getConfig().getStringList("lowSecurityServer");
    }

    public boolean isAuthenticated(UUID uuid) {
        return !awaitingAuthentication.contains(uuid);
    }

    public void authorize(ProxiedPlayer player) {
        awaitingAuthentication.remove(player.getUniqueId());
        getUser(player.getUniqueId()).setLastAuthenticated(System.currentTimeMillis());
        player.sendTitle(ProxyServer.getInstance().createTitle().clear());
    }

    public ServerInfo getLowSecurityServer() {
        for (Map.Entry<String, ServerInfo> servers : plugin.getProxy().getServers().entrySet()) {
            if (lowSecurityServers.contains(servers.getKey())) {
                return servers.getValue();
            }
        }
        return null;
    }

    public boolean isLowSecurityServer(String name) {
        return lowSecurityServers.contains(name);
    }

    public boolean isLowSecurityServer(ServerInfo server) {
        return isLowSecurityServer(server.getName());
    }

    public void requireAuthentication(ProxiedPlayer player) {
        awaitingAuthentication.add(player.getUniqueId());
    }

    public void createAuthentication(ProxiedPlayer player) {
        final GoogleAuthenticatorKey key = plugin.gAuth.createCredentials();
        AegisUser  aegisUser = new AegisUser(player, key.getKey(), key.getScratchCodes());
        users.put(player.getUniqueId(), aegisUser);
    }

    public AegisUser getUser(UUID uuid) {
        return users.get(uuid);
    }

    public boolean hasUser(UUID uuid) {
        return users.containsKey(uuid);
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
        createAuthentication(player);
        awaitingAuthentication.add(player.getUniqueId());
        sendMap(player);
        List<Integer> scratchCodes = plugin.getDaemon().getUser(player.getUniqueId()).recreateScratchCodes();
        new ChatBuilder("Your backup codes. Save these in a secure location!").color(AQUA).send(player);
        new ChatBuilder(scratchCodes.stream().map(Object::toString).collect(Collectors.joining(" "))).color(GOLD).send(player);
    }

    private byte[] getQRCode(ProxiedPlayer pp) {
        String secret = getUser(pp.getUniqueId()).getSecretKey();
        String topbar = "LordOfTheCraft";
        return new QRRenderer(pp.getName(), secret, topbar).render();
    }
    
    private void sendMap(ProxiedPlayer player) {
        
        byte[] data = getQRCode(player);
        MapData md = new MapData(AUTHENTHICATION_MAP_ID, (byte) 0, false, new MapData.Icon[0],
                128, 128, 0, 0, data);
        player.unsafe().sendPacket(md);

        final PlayerInventory inventory = InventoryManager.getInventory(player.getUniqueId());

        final ItemStack map = new ItemStack(ItemType.FILLED_MAP);
        CompoundTag compoundTag = (CompoundTag) map.getNBTTag();
        compoundTag.getValue().put("tag", new IntTag("map", AUTHENTHICATION_MAP_ID));
        map.setNBTTag(compoundTag);
        inventory.setItem(36, map);
        inventory.changeHeldItem((short) 0);
        inventory.update();
    }

    public void saveUsers() {
        users.values().forEach(AegisUser::save);
    }
}
