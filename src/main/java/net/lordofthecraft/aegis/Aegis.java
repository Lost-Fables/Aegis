package net.lordofthecraft.aegis;

import co.lotc.core.bungee.command.BungeeCommandData;
import co.lotc.core.bungee.command.Commands;
import com.google.common.collect.ImmutableMap;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import de.exceptionflug.protocolize.api.protocol.ProtocolAPI;
import de.exceptionflug.protocolize.api.util.ProtocolVersions;
import lombok.Getter;
import net.lordofthecraft.aegis.cmd.AegisCommands;
import net.lordofthecraft.aegis.cmd.AuthCommands;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

@Getter
public final class Aegis extends Plugin {

    public static Aegis INSTANCE;

    private AuthenticationDaemon daemon;
    private GoogleAuthenticator gAuth;
    private Configuration config;

    @Override
    public void onEnable() {
        INSTANCE = this;
        registerPacket();

        generateDefaultConfig();
        loadConfig();
        gAuth = new GoogleAuthenticator();

        Commands.build(new BungeeCommandData(this, "auth", "auth.use", "ToTP authentication for bungee", new ArrayList<>()), () -> new AuthCommands(this));
        Commands.build(new BungeeCommandData(this, "aegis", "aegis.use", "Administration commands for Aegis", new ArrayList<>()), AegisCommands::new);

        getProxy().getPluginManager().registerListener(this, new Events(this));
        daemon = new AuthenticationDaemon(this);
    }

    @Override
    public void onDisable() {
        daemon.saveUsers();
        saveConfig();
    }

    private void generateDefaultConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerPacket() {
        ProtocolAPI.getPacketRegistration().registerPlayClientPacket(MapData.class, ImmutableMap.of(ProtocolVersions.MINECRAFT_1_13_2, 0x26));
    }
}
