package net.morphserver.worth;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class LoreUpdaterTask extends BukkitRunnable {

    private final MorphWorthPlugin plugin;
    private final WorthManager worthManager;

    public LoreUpdaterTask(MorphWorthPlugin plugin, WorthManager worthManager) {
        this.plugin = plugin;
        this.worthManager = worthManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack[] contents = player.getInventory().getContents();
            for (ItemStack item : contents) {
                if (item == null) continue;
                worthManager.applyWorthLore(item);
            }

            ItemStack[] armor = player.getInventory().getArmorContents();
            for (ItemStack item : armor) {
                if (item == null) continue;
                worthManager.applyWorthLore(item);
            }

            ItemStack off = player.getInventory().getItemInOffHand();
            if (off != null) {
                worthManager.applyWorthLore(off);
            }
        }
    }
}
