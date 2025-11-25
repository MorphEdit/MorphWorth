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
import java.util.Locale;
import java.util.Map;

/**
 * Central logic for calculating item worth and applying it to item lore.
 *
 * Worth formula (per stack):
 *   totalWorth = ((baseItemPrice) + (enchantPricePerItem)) * amount
 *
 * Where:
 *   - baseItemPrice       = price from worth.yml under items:
 *   - enchantPricePerItem = sum(enchantPrice * level) for all enchants
 *   - amount              = stack size
 */
public class WorthManager {

    private final MorphWorthPlugin plugin;
    private final FileConfiguration worthConfig;
    private final ConfigurationSection itemsSection;
    private final ConfigurationSection enchantmentsSection;

    // Raw config values
    private final String loreFormat;      // e.g. "&8Worth: &a$%price%"
    private final String lorePrefixPlain; // e.g. "Worth:" (color-stripped)
    private final int roundDecimals;

    public WorthManager(MorphWorthPlugin plugin, FileConfiguration worthConfig) {
        this.plugin = plugin;
        this.worthConfig = worthConfig;

        FileConfiguration mainConfig = plugin.getConfig();

        this.loreFormat = mainConfig.getString("lore-format", "&8Worth: &a$%price%");
        String lorePrefixRaw = mainConfig.getString("lore-prefix", "&8Worth:");
        String coloredPrefix = ChatColor.translateAlternateColorCodes('&', lorePrefixRaw);
        this.lorePrefixPlain = ChatColor.stripColor(coloredPrefix);

        this.roundDecimals = mainConfig.getInt("round-decimals", 2);

        this.itemsSection = worthConfig.getConfigurationSection("items");
        this.enchantmentsSection = worthConfig.getConfigurationSection("enchantments");
    }

    /**
     * Apply (or update/remove) the worth line on the given item.
     * The worth value is based on the current stack size and enchantments.
     */
    public void applyWorthLore(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        double totalWorth = getTotalWorth(item);

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        // Remove any existing worth lines so we can replace them cleanly
        lore.removeIf(line -> ChatColor.stripColor(line).startsWith(lorePrefixPlain));

        // If worth is <= 0 just clean up old worth lines and exit
        if (totalWorth <= 0) {
            if (lore.isEmpty()) {
                meta.setLore(null);
            } else {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
            return;
        }

        double rounded = round(totalWorth, roundDecimals);
        String priceText = formatNumber(rounded);

        String line = loreFormat.replace("%price%", priceText);
        line = ChatColor.translateAlternateColorCodes('&', line);

        lore.add(line);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Calculate the total worth of the given item stack
     * (base price + enchantments, multiplied by stack amount).
     */
    public double getTotalWorth(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return 0.0;
        }

        int amount = item.getAmount();
        if (amount <= 0) {
            return 0.0;
        }

        double basePricePerItem = getBaseItemWorth(item);
        double enchantPricePerItem = 0.0;

        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            enchantPricePerItem = getEnchantmentWorthPerItem(item);
        }

        double perItem = basePricePerItem + enchantPricePerItem;
        return perItem * amount;
    }

    /**
     * Get base worth of a single item from the items: section in worth.yml.
     */
    private double getBaseItemWorth(ItemStack item) {
        if (itemsSection == null) {
            return 0.0;
        }
        String key = item.getType().name();
        return itemsSection.getDouble(key, 0.0D);
    }

    /**
     * Calculate total enchantment worth PER ITEM (not multiplied by stack size yet).
     * Formula:
     *   sum(enchantPrice * level) for all enchants on the item.
     */
    private double getEnchantmentWorthPerItem(ItemStack item) {
        if (enchantmentsSection == null) {
            return 0.0;
        }
        if (!item.hasItemMeta()) {
            return 0.0;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchants()) {
            return 0.0;
        }

        double total = 0.0;
        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            // Modern Bukkit enchantments have a namespaced key like "minecraft:sharpness"
            // We only care about the key part and store it in UPPER_CASE in worth.yml,
            // e.g. SHARPNESS, EFFICIENCY, etc.
            String keyName = enchantment.getKey().getKey().toUpperCase(Locale.ROOT);

            double perLevelPrice = enchantmentsSection.getDouble(keyName, 0.0D);
            if (perLevelPrice <= 0) {
                continue;
            }

            total += perLevelPrice * level;
        }
        return total;
    }

    /**
     * Remove worth lore from a meta clone, used to compare two items while ignoring worth.
     */
    private ItemMeta metaWithoutWorth(ItemMeta original) {
        if (original == null) {
            return null;
        }
        ItemMeta clone = original.clone();
        if (!clone.hasLore()) {
            return clone;
        }

        List<String> lore = new ArrayList<>(clone.getLore());
        lore.removeIf(line -> ChatColor.stripColor(line).startsWith(lorePrefixPlain));
        if (lore.isEmpty()) {
            clone.setLore(null);
        } else {
            clone.setLore(lore);
        }
        return clone;
    }

    /**
     * Compare two items while ignoring the worth-lore line.
     * This is used by StackingListener to decide whether two stacks can be merged.
     */
    public boolean isSameItemIgnoringWorthLore(ItemStack a, ItemStack b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getType() != b.getType()) {
            return false;
        }

        ItemMeta metaA = a.getItemMeta();
        ItemMeta metaB = b.getItemMeta();

        if (metaA == null && metaB == null) {
            return true;
        }
        if (metaA == null || metaB == null) {
            return false;
        }

        ItemMeta cleanA = metaWithoutWorth(metaA);
        ItemMeta cleanB = metaWithoutWorth(metaB);

        return cleanA.equals(cleanB);
    }

    /**
     * Round a value to the configured number of decimal places.
     */
    private double round(double value, int places) {
        if (places < 0) {
            return value;
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Format the number according to the configured number of decimal places.
     * If roundDecimals == 0, it returns an integer string.
     */
    private String formatNumber(double value) {
        if (roundDecimals == 0) {
            return String.valueOf((long) value);
        }
        return String.format("%." + roundDecimals + "f", value);
    }
}
