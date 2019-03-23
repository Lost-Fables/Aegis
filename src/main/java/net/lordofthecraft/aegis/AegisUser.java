package net.lordofthecraft.aegis;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Getter
public class AegisUser {

    private UUID uuid;
    private String secretKey;
    private List<Integer> scratchCodes;
    private long lastAuththenticated;
    private String lastIP;

    public AegisUser(ProxiedPlayer player, String secretKey, List<Integer> scratchCodes) {
        this.uuid = player.getUniqueId();
        this.secretKey = secretKey;
        this.scratchCodes = scratchCodes;
        lastAuththenticated = System.currentTimeMillis();
        lastIP = player.getAddress().getAddress().getHostAddress();
    }
}
