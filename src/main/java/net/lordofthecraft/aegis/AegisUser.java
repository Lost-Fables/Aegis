package net.lordofthecraft.aegis;

import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class AegisUser {

    private UUID uuid;
    private String secretKey;
    private List<Integer> scratchCodes;
    private long lastAuthenticated;
    @Getter
    private Map<String, Long> lastKnownIPs;
    private Configuration config;
    private File file;

    public AegisUser(ProxiedPlayer player, String secretKey, List<Integer> scratchCodes) {
        this.uuid = player.getUniqueId();
        this.secretKey = secretKey;
        this.scratchCodes = scratchCodes;
        lastAuthenticated = 0;
        lastKnownIPs = new ConcurrentHashMap<>();
        lastKnownIPs.put(player.getAddress().getAddress().getHostAddress(), System.currentTimeMillis());


        file = new File(Aegis.INSTANCE.getDataFolder() + File.separator + "users", uuid + ".yml");
    }

    public AegisUser(Configuration config) {
        this.config = config;
        load();
        file = new File(Aegis.INSTANCE.getDataFolder() + File.separator + "users", uuid + ".yml");
    }

    public void save() {
        if (!file.exists()) {
            try {
                file.createNewFile();
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config.set("uuid", uuid.toString());
        config.set("secretKey", secretKey);
        config.set("scratchCodes", scratchCodes);
        config.set("lastAuthenticated", lastAuthenticated);
        for (Map.Entry<String, Long> entry : lastKnownIPs.entrySet()) {
            config.set("ip." + entry.getKey().replaceAll("\\.", "-"), entry.getValue());
        }

        saveConfig();
    }

    public void saveConfig () {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        uuid = UUID.fromString(config.getString("uuid"));
        secretKey = config.getString("secretKey");
        scratchCodes = config.getIntList("scratchCodes");
        lastAuthenticated = config.getLong("lastAuthenticated");
        lastKnownIPs = new ConcurrentHashMap<>();
        config.getSection("ip").getKeys().forEach(ip -> lastKnownIPs.put(ip.replaceAll("-", "."), config.getLong("ip." + ip)));
    }

    public boolean isRecentIP(String ip) {
        return lastKnownIPs.containsKey(ip);
    }

    public void setLastAuthenticated(long lastAuthenticated) {
        this.lastAuthenticated = lastAuthenticated;
        config.set("lastAuthenticated", lastAuthenticated);
        saveConfig();
    }

    public List<Integer> recreateScratchCodes() {
        scratchCodes = Aegis.INSTANCE.getGAuth().createCredentials().getScratchCodes();
        return scratchCodes;
    }

    public void delete() {
        file.delete();
    }
}
