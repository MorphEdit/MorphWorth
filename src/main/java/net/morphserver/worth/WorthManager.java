package net.morphserver.worth;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class WorthManager {

    private final MorphWorthPlugin plugin;
    private final FileConfiguration worthConfig;
    private final String loreFormat;
    private final String lorePrefix;
    private final int roundDecimals;

    public WorthManager(MorphWorthPlugin plugin, FileConfiguration worthConfig) {
        this.plugin = plugin;
        this.worthConfig = worthConfig;
        this.loreFormat = color(plugin.getConfig().getString("lore-format", "&8Worth: &a$%price%"));
        this.lorePrefix = ChatColor.stripColor(
                color(plugin.getConfig().getString("lore-prefix", "&8Worth:"))
        );
        this.roundDecimals = plugin.getConfig().getInt("round-decimals", 2);
    }

    public double getWorth(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return -1;

        ConfigurationSection sec = worthConfig.getConfigurationSection("worth");
        if (sec == null) return -1;

        String key = item.getType().name();
        if (!sec.isSet(key)) return -1;

        double perItem = sec.getDouble(key);
        return perItem * item.getAmount();
    }

    public void applyWorthLore(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        double worth = getWorth(item);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Remove old worth line
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith(lorePrefix);
        });

        if (worth >= 0) {
            double rounded = round(worth, roundDecimals);
            String line = loreFormat.replace("%price%", formatNumber(rounded));
            lore.add(color(line));
        }

        meta.setLore(lore.isEmpty() ? null : lore);
        item.setItemMeta(meta);
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private double round(double value, int places) {
        if (places < 0) return value;
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private String formatNumber(double value) {
        if (roundDecimals == 0) {
            return String.valueOf((long) value);
        }
        return String.format("%." + roundDecimals + "f", value);
    }
}
