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
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.protocol.packet.Chat;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.AQUA;
import static net.md_5.bungee.api.ChatColor.GOLD;

public class AuthenticationDaemon {
    private static final int AUTHENTICATION_MAP_ID = 1337;
    public static final String BACKUP_CODES = "Your backup codes. Save these in a secure location!";
    
    private Aegis plugin;
    private Set<ProxiedPlayer> awaitingAuthentication;
    @Getter
    private Set<ProxiedPlayer> firstTimeSetup;
    private Map<UUID, AegisUser> users;
    private Map<ProxiedPlayer, Queue<Chat>> queuedChat;
    @Setter
    private List<String> lowSecurityServers;
    @Setter
    private List<String> pendingServers;

    public AuthenticationDaemon(Aegis plugin) {
        queuedChat = new ConcurrentHashMap<>();
        this.plugin = plugin;
        loadUsers();
        awaitingAuthentication = ConcurrentHashMap.newKeySet();
        firstTimeSetup = ConcurrentHashMap.newKeySet();
    }

    public void loadConfig() {
        lowSecurityServers = plugin.getConfig().getStringList("lowSecurityServers");
        pendingServers = plugin.getConfig().getStringList("pendingServers");
    }

    public boolean isAuthenticated(ProxiedPlayer player) {
        return !awaitingAuthentication.contains(player);
    }

    public boolean isAwaitingAuthentication(ProxiedPlayer player) {
        return awaitingAuthentication.contains(player);
    }

    public void authorize(ProxiedPlayer player) {
        awaitingAuthentication.remove(player);
        firstTimeSetup.remove(player);
        AegisUser user = getUser(player.getUniqueId());
        user.setLastAuthenticated(System.currentTimeMillis());
        user.save();
        player.sendTitle(ProxyServer.getInstance().createTitle().clear());
    }

    public ServerInfo getPendingServer() {
        for (Map.Entry<String, ServerInfo> servers : plugin.getProxy().getServers().entrySet()) {
            if (pendingServers.contains(servers.getKey())) {
                return servers.getValue();
            }
        }
        return null;
    }

    public boolean isLowSecurityServer(String name) {
        return lowSecurityServers.contains(name) || pendingServers.contains(name);
    }

    public boolean isLowSecurityServer(ServerInfo server) {
        return isLowSecurityServer(server.getName());
    }

    public void requireAuthentication(ProxiedPlayer player) {
        awaitingAuthentication.add(player);
    }

    public void createAuthentication(ProxiedPlayer player) {
        final GoogleAuthenticatorKey key = plugin.getGAuth().createCredentials();
        AegisUser aegisUser = new AegisUser(player, key.getKey(), key.getScratchCodes());
        users.put(player.getUniqueId(), aegisUser);
    }

    public AegisUser getUser(UUID uuid) {
        return users.get(uuid);
    }

    public boolean hasAuthentication(UUID uuid) {
        return users.containsKey(uuid);
    }

    public void removeUser(UUID uuid, boolean deleteFile) {
        if (deleteFile) {
            users.get(uuid).delete();
        }
        users.remove(uuid);
    }

    public void removeAwaitingAuthentication(ProxiedPlayer player) {
        awaitingAuthentication.remove(player);
    }

    public void loadUsers() {
        users = new ConcurrentHashMap<>();
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
        new ChatBuilder("Two factor authentication is a second layer of security for your account. Instead of only requiring just your minecraft account's password, it also requires a code generated from an app on your phone." +
                "If you ever lose access to your phone, there are backup codes that you can use to reset your authentication. If you lose both your phone and your backup codes, contact the administration to verify your identity.")
                .color(GOLD).send(player);
        new ChatBuilder("Download a 2fa app on your phone and scan the QR code on the map. Here are some recommended apps").color(GOLD).send(player);
        for (String key : plugin.getConfig().getSection("suggestedApps").getKeys()) {
            new ChatBuilder("[").append(key).append("]").color(AQUA).event(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.getConfig().getString("suggestedApps." + key)));
        }

        createAuthentication(player);
        awaitingAuthentication.add(player);

        plugin.getProxy().getScheduler().schedule(plugin, () -> sendMap(player), 2, TimeUnit.SECONDS);
        sendBackupCodes(player);
        firstTimeSetup.add(player);
    }

    private byte[] getQRCode(ProxiedPlayer pp) {
        String secret = getUser(pp.getUniqueId()).getSecretKey();
        String topbar = "LordOfTheCraft";
        return new QRRenderer(pp.getName(), secret, topbar).render();
    }
    
    private void sendMap(ProxiedPlayer player) {
        
        byte[] data = getQRCode(player);
        MapData md = new MapData(AUTHENTICATION_MAP_ID, (byte) 0, false, new MapData.Icon[0],
                128, 128, 0, 0, data);
        player.unsafe().sendPacket(md);

        final PlayerInventory inventory = InventoryManager.getInventory(player.getUniqueId());

        final ItemStack map = new ItemStack(ItemType.FILLED_MAP);
        CompoundTag compoundTag = (CompoundTag) map.getNBTTag();
        compoundTag.getValue().put("tag", new IntTag("map", AUTHENTICATION_MAP_ID));
        map.setNBTTag(compoundTag);
        inventory.setItem(36, map);
        inventory.changeHeldItem((short) 0);
        inventory.update();
    }

    public void saveUsers() {
        users.values().forEach(AegisUser::save);
    }

    public void queueMessage(ProxiedPlayer player, Chat message) {
        Queue<Chat> chat = queuedChat.getOrDefault(player, new ConcurrentLinkedQueue<>());
        chat.add(message);
        queuedChat.put(player, chat);
    }

    public void sendQueuedChat(ProxiedPlayer player) {
        queuedChat.getOrDefault(player, new ConcurrentLinkedQueue<>()).forEach(p -> player.unsafe().sendPacket(p));
    }

    public void sendBackupCodes(ProxiedPlayer player) {
        List<Integer> scratchCodes = plugin.getDaemon().getUser(player.getUniqueId()).recreateScratchCodes();
        new ChatBuilder(BACKUP_CODES).color(AQUA).newline()
                .append(scratchCodes.stream().map(Object::toString).collect(Collectors.joining(" "))).color(GOLD).send(player);
    }
}
