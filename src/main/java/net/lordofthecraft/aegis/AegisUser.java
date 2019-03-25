package net.lordofthecraft.aegis;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

@Getter
public class AegisUser {

    private UUID uuid;
    private String secretKey;
    private List<Integer> scratchCodes;
    private long lastAuthenticated;
    private String lastIP;
    private Configuration config;

    public AegisUser(ProxiedPlayer player, String secretKey, List<Integer> scratchCodes) {
        this.uuid = player.getUniqueId();
        this.secretKey = secretKey;
        this.scratchCodes = scratchCodes;
        lastAuthenticated = System.currentTimeMillis();
        lastIP = player.getAddress().getAddress().getHostAddress();
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
        config.set("lastIP", lastIP);
    }

    public void load() {
        uuid = UUID.fromString(config.getString("uuid"));
        secretKey = config.getString("secretKey");
        scratchCodes = config.getIntList("scratchCodes");
        lastAuthenticated = config.getLong("lastAuthenticated");
        lastIP = config.getString("lastIP");
    }
}
