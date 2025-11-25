# MorphWorth

A lightweight Minecraft plugin that automatically displays an item's worth in its lore.  
Supports **Paper / Spigot / Purpur 1.21.4+**.

---

## ✨ Features
- Shows item worth directly in lore  
- Enchantment value support (per-level)  
- Fully configurable (`config.yml`, `worth.yml`)  
- Live lore updating  
- Customizable:
  - Lore format  
  - Prefix  
  - Decimal rounding  
  - Update interval  
- No external dependencies  

---

## 💰 Worth Formula
```
Total Worth = (Item Price × Amount) + (Enchantment Price × Level × Amount)
```

---

## 📦 Installation
1. Drop the JAR into `/plugins/`  
2. Start the server to generate config files  
3. Edit `config.yml` & `worth.yml`  
4. Run `/worthlore reload`  

---

## 🎮 Commands
| Command               | Description           |
|-----------------------|-----------------------|
| `/worthlore reload`   | Reload plugin configs |

---

## ⚙️ Configuration

### `config.yml`
```yaml
update-interval-ticks: 40
lore-format: "&8Worth: &a$%price%"
round-decimals: 2
lore-prefix: "&8Worth:"
```

### `worth.yml`
```yaml
items:
  DIAMOND: 131.3
  IRON_INGOT: 13.8

enchantments:
  SHARPNESS: 300.0
  MENDING: 200.0
```

---

## 🛠️ Build
```bash
./gradlew build
```

---

## 👤 Developer
Made by **Morph** — My first Minecraft plugin! 🎉
