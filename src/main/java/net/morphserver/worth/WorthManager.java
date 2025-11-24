package net.morphserver.worth;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * Calculate total worth including base item price + enchantments
     */
    public double getWorth(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return -1;

        // Get base item worth
        double baseWorth = getBaseItemWorth(item);
        if (baseWorth < 0) return -1;

        // Calculate total for stack
        double total = baseWorth * item.getAmount();

        // Add enchantment values
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            double enchantWorth = getEnchantmentWorth(item);
            total += enchantWorth * item.getAmount();
        }

        return total;
    }

    /**
     * Get base worth of a single item from items section
     */
    private double getBaseItemWorth(ItemStack item) {
        ConfigurationSection itemsSection = worthConfig.getConfigurationSection("items");
        if (itemsSection == null) return -1;

        String key = item.getType().name();
        if (!itemsSection.isSet(key)) return -1;

        return itemsSection.getDouble(key);
    }

    /**
     * Calculate total enchantment worth for an item
     */
    private double getEnchantmentWorth(ItemStack item) {
        ConfigurationSection enchSection = worthConfig.getConfigurationSection("enchantments");
        if (enchSection == null) return 0;

        double total = 0;
        Map<Enchantment, Integer> enchants = item.getEnchantments();

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            String enchantKey = entry.getKey().getKey().getKey().toUpperCase();
            int level = entry.getValue();

            if (enchSection.isSet(enchantKey)) {
                double pricePerLevel = enchSection.getDouble(enchantKey);
                total += pricePerLevel * level;
            }
        }

        return total;
    }

    /**
     * Apply worth lore to an item
     */
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

        // Add new worth line if item has value
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