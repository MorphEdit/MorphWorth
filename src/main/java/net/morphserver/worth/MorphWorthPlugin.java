package net.morphserver.worth;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MorphWorthPlugin extends JavaPlugin {

    private static MorphWorthPlugin instance;
    private WorthManager worthManager;
    private LoreUpdaterTask loreTask;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResourceIfNotExists("worth.yml");

        reloadAll();

        // /worthlore reload
        getCommand("worthlore").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("morphworth.reload")) {
                sender.sendMessage("§cYou don't have permission.");
                return true;
            }
            reloadAll();
            sender.sendMessage("§aMorphWorth reloaded.");
            return true;
        });
    }

    @Override
    public void onDisable() {
        if (loreTask != null) {
            loreTask.cancel();
        }
    }

    private void reloadAll() {
        reloadConfig();
        FileConfiguration worthConfig = loadWorthConfig();

        this.worthManager = new WorthManager(this, worthConfig);

        if (loreTask != null) {
            loreTask.cancel();
        }

        int interval = getConfig().getInt("update-interval-ticks", 40);
        loreTask = new LoreUpdaterTask(this, worthManager);
        loreTask.runTaskTimer(this, 20L, interval);
    }

    private FileConfiguration loadWorthConfig() {
        File file = new File(getDataFolder(), "worth.yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveResourceIfNotExists(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }

    public static MorphWorthPlugin getInstance() {
        return instance;
    }

    public WorthManager getWorthManager() {
        return worthManager;
    }
}
