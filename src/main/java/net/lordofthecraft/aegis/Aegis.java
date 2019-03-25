package net.lordofthecraft.aegis;

import co.aikar.commands.BungeeCommandManager;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public final class Aegis extends Plugin {

    BungeeCommandManager commandManager;
    AuthenticationDaemon daemon;
    GoogleAuthenticator gAuth;
    Configuration config;

    @Override
    public void onEnable() {
        registerPacket();

        generateDefaultConfig();
        loadConfig();
        gAuth = new GoogleAuthenticator();

        commandManager = new BungeeCommandManager(this);
        commandManager.registerCommand(new AegisCommands());
        commandManager.registerDependency(Aegis.class, "plugin", this);

        getProxy().getPluginManager().registerListener(this, new Events(this));
        daemon = new AuthenticationDaemon(this);
    }

    @Override
    public void onDisable() {
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

    }
}
