package net.lordofthecraft.aegis;

import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class AegisUser {

    private UUID uuid;
    private String secretKey;
    private List<Integer> scratchCodes;
    private long lastAuthenticated;
    private Map<String, Long> lastKnownIPs;
    private Configuration config;

    public AegisUser(ProxiedPlayer player, String secretKey, List<Integer> scratchCodes) {
        this.uuid = player.getUniqueId();
        this.secretKey = secretKey;
        this.scratchCodes = scratchCodes;
        lastAuthenticated = System.currentTimeMillis();
        lastKnownIPs = new HashMap<>();
        lastKnownIPs.put(player.getAddress().getAddress().getHostAddress(), System.currentTimeMillis());


        File file = new File(Aegis.INSTANCE.getDataFolder(), uuid + ".yml");
        try {
            file.createNewFile();
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        save();
    }

    public AegisUser(Configuration config) {
        load();
        this.config = config;
    }

    public void save() {
        config.set("uuid", uuid.toString());
        config.set("secretKey", secretKey);
        config.set("scratchCodes", scratchCodes);
        config.set("lastAuthenticated", lastAuthenticated);
        for (Map.Entry<String, Long> entry : lastKnownIPs.entrySet()) {
            config.set("ip." + entry.getKey(), entry.getValue());
        }

        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(Aegis.INSTANCE.getDataFolder(), uuid + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        uuid = UUID.fromString(config.getString("uuid"));
        secretKey = config.getString("secretKey");
        scratchCodes = config.getIntList("scratchCodes");
        lastAuthenticated = config.getLong("lastAuthenticated");
        lastKnownIPs = new HashMap<>();
        config.getSection("ip").getKeys().forEach(ip -> lastKnownIPs.put(ip, config.getLong("ip." + ip)));
    }

    public boolean isRecentIP(String ip) {
        return lastKnownIPs.containsKey(ip);
    }
}
