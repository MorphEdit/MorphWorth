# MorphWorth

**My first Minecraft plugin!**  
MorphWorth automatically displays the worth/value of an item inside its lore using a configurable YAML file.

This plugin is lightweight, simple, and works on **Minecraft 1.21.4+** for **Paper / Spigot / Purpur** servers.

---

## ✨ Features

- Adds a custom line to item lore showing the item's worth
- **Enchantment support** - automatically adds enchantment value to total worth
- Fully configurable via `worth.yml` and `config.yml`
- Updates item lores live while players have items in their inventory
- Customizable:
  - Lore format
  - Prefix
  - Decimal rounding
  - Update interval
- No external dependencies
- Built using **Java 21**, **Gradle**, and **Paper API 1.21.4**

---

## 📦 Installation

1. Download the latest plugin JAR from [Releases](../../releases)
2. Drop it into your server's `/plugins` folder
3. Start/restart the server
4. Edit `config.yml` and `worth.yml` in `/plugins/MorphWorth/`
5. Run `/worthlore reload` to apply changes

---

## ⚙️ Configuration

### config.yml
Controls formatting, prefix, decimals, and update interval:

```yaml
update-interval-ticks: 40        # How often to refresh lore (40 = 2 seconds)
lore-format: "&8Worth: &a$%price%"
round-decimals: 2                # Number of decimal places
lore-prefix: "&8Worth:"          # Used to detect existing worth lines
```

### worth.yml
Defines prices for items and enchantments:

```yaml
items:
  DIAMOND: 131.3
  IRON_INGOT: 13.8
  NETHERITE_SWORD: 6506.1
  # ... (1000+ items supported)

enchantments:
  SHARPNESS: 300.0     # Price per level
  MENDING: 200.0
  EFFICIENCY: 300.0
  # ... (50+ enchantments supported)
```

**Material reference:**  
https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html

**Enchantment reference:**  
https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html

---

## 💰 Worth Calculation

The plugin calculates total worth as:

```
Total = (Base Item Price × Quantity) + (Enchantment Price × Level × Quantity)
```

**Example:**
- Diamond Sword (x1) with Sharpness V and Mending I:
- = (277.2 × 1) + (300.0 × 5) + (200.0 × 1)
- = 277.2 + 1500.0 + 200.0
- = **$1977.20**

---

## 🎮 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/worthlore reload` | `morphworth.reload` | Reload configuration files |

---

## 🔐 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `morphworth.reload` | OP | Allows reloading the plugin |

---

## 🛠️ Building from Source

```bash
git clone https://github.com/MorphEdit/MorphWorth.git
cd MorphWorth
./gradlew build
```

The compiled JAR will be in `build/libs/`

---

## 📝 Changelog

### v2.0.0
- ✨ Added enchantment support
- ✨ New worth.yml structure with `items:` and `enchantments:` sections
- ✨ Automatic enchantment value calculation
- 📝 Improved configuration documentation
- 🐛 Fixed lore detection issues

### v1.0.1
- 🎉 Initial release
- ✅ Basic worth display
- ✅ Configurable lore format

---

## 🐛 Support

If you find any bugs or want features added:
- Open an issue on [GitHub](../../issues)
- Contact on Discord: **khungkhang**

---

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🙏 Credits

**Developer:** Morph  
**My first Minecraft plugin!** 🎉

Made with ❤️ for the Minecraft community
