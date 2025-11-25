package net.morphserver.worth;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Main plugin class for MorphWorth.
 *
 * Responsibilities:
 *  - Load main config and worth.yml
 *  - Create and expose WorthManager
 *  - Start and stop the periodic LoreUpdaterTask
 *  - Register listeners and handle /worthlore reload
 */
public class MorphWorthPlugin extends JavaPlugin {

    private static MorphWorthPlugin instance;
    private WorthManager worthManager;
    private LoreUpdaterTask loreTask;

    @Override
    public void onEnable() {
        instance = this;

        // Ensure config.yml and worth.yml exist
        saveDefaultConfig();
        saveResourceIfNotExists("worth.yml");

        // Load worth.yml
        File worthFile = new File(getDataFolder(), "worth.yml");
        FileConfiguration worthConfig = YamlConfiguration.loadConfiguration(worthFile);

        // Create manager
        this.worthManager = new WorthManager(this, worthConfig);

        // Start periodic lore updater
        long interval = getConfig().getLong("update-interval-ticks", 40L);
        if (interval < 1L) {
            interval = 40L;
        }
        this.loreTask = new LoreUpdaterTask(this, worthManager);
        this.loreTask.runTaskTimer(this, interval, interval);

        // Register stacking listener so items with different worth-lore
        // can still be merged when they are logically the same item.
        getServer().getPluginManager().registerEvents(new StackingListener(worthManager), this);

        getLogger().info("MorphWorth enabled.");
    }

    @Override
    public void onDisable() {
        if (loreTask != null) {
            loreTask.cancel();
            loreTask = null;
        }
        getLogger().info("MorphWorth disabled.");
    }

    /**
     * Handle /worthlore reload.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("worthlore")) {
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("morphworth.reload")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            // Reload main config
            reloadConfig();

            // Reload worth.yml (do NOT overwrite existing file)
            File worthFile = new File(getDataFolder(), "worth.yml");
            if (!worthFile.exists()) {
                // In case it was deleted, restore from jar
                saveResourceIfNotExists("worth.yml");
            }
            FileConfiguration worthConfig = YamlConfiguration.loadConfiguration(worthFile);

            // Recreate manager with fresh configs
            this.worthManager = new WorthManager(this, worthConfig);

            // Restart lore task with (potentially) new interval
            if (loreTask != null) {
                loreTask.cancel();
            }
            long interval = getConfig().getLong("update-interval-ticks", 40L);
            if (interval < 1L) {
                interval = 40L;
            }
            this.loreTask = new LoreUpdaterTask(this, worthManager);
            this.loreTask.runTaskTimer(this, interval, interval);

            sender.sendMessage(ChatColor.GREEN + "MorphWorth configuration reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Usage: /worthlore reload");
        return true;
    }

    /**
     * Save a default resource from the jar if it does not already exist on disk.
     */
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
