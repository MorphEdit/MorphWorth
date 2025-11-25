package net.morphserver.worth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Periodic task that updates worth-lore for all items held by all players.
 *
 * Runs every X ticks as configured in config.yml (update-interval-ticks).
 * For each online player it updates:
 *  - main inventory contents
 *  - armor contents
 *  - off-hand item
 */
public class LoreUpdaterTask extends BukkitRunnable {

    private final MorphWorthPlugin plugin;
    private final WorthManager worthManager;

    public LoreUpdaterTask(MorphWorthPlugin plugin, WorthManager worthManager) {
        this.plugin = plugin;
        this.worthManager = worthManager;
    }

    @Override
    public void run() {
        // Simple and safe: loop over all players and update every visible item.
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Main inventory contents
            ItemStack[] contents = player.getInventory().getContents();
            for (ItemStack item : contents) {
                if (item == null) continue;
                worthManager.applyWorthLore(item);
            }

            // Armor contents (helmet, chestplate, leggings, boots)
            ItemStack[] armor = player.getInventory().getArmorContents();
            for (ItemStack item : armor) {
                if (item == null) continue;
                worthManager.applyWorthLore(item);
            }

            // Off-hand item
            ItemStack off = player.getInventory().getItemInOffHand();
            if (off != null) {
                worthManager.applyWorthLore(off);
            }
        }
    }
}
