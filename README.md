<div align="center">

# 🌱 Sapling Replanter

**Sapling Replanter** is a lightweight Minecraft plugin that automatically replants saplings after trees are chopped down.
With support for mcMMO, configurable permissions, and probability-based logic.

![Issues](https://img.shields.io/github/issues-raw/BitByLogics/SaplingReplanter)
[![Stars](https://img.shields.io/github/stars/BitByLogics/SaplingReplanter)](https://github.com/BitByLogics/SaplingReplanter/stargazers)

<a href="#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/supported/spigot_46h.png" height="35"></a>
<a href="#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/supported/paper_46h.png" height="35"></a>
<a href="#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v3/assets/compact/available/modrinth_vector.svg" height="35" href="https://modrinth.com/mod/inventory-slots"></a>

</div>

## 🔧 Features

- 🌲 Automatically replants trees after being broken
- 🎯 Configurable replanting chance
- 🧪 Supports mcMMO-based level requirements and XP scaling
- 🪓 Optional Treefeller-only activation
- 🧍 Permission-based usage and bypass controls
- 🌱 Optional sapling consumption
- 🚫 Ignores blocks placed by players (configurable)
- 🔁 `/saplingreplanter reload` command to reload config and levels

> ⚠️ **Note:** It is **intended behavior** that the plugin may break blocks to make room for saplings, especially for large or mega trees. However, the tree detection logic isn’t flawless and may occasionally spawn extra saplings for the same tree.
> 
> These are known limitations, and contributions are welcome! If you have improvements or fixes, feel free to open a pull request. 🌱

## ✅ Requirements

- **Minecraft 1.21+** (or compatible Spigot versions)
- [mcMMO](https://www.spigotmc.org/resources/official-mcmmo-original-author-returns.64348/) (optional, for mcMMO support)

---

## 📥 Installation

1. Download the latest version from [Modrinth](https://modrinth.com/project/sapling-replanter/).
2. Place it in your server’s `plugins/` directory.
3. Restart your server.
4. Edit the configuration file to your needs.
5. (Optional) Install [mcMMO] if you want level-based control.

---

## ⚙️ Configuration

Located in `plugins/SaplingReplanter/config.yml`:

```yaml
Settings:
  Use-Permission: true
  Consume-Saplings: true
  Ignore-Player-Placed-Blocks: true
  MCMMO-Support: true
  Treefeller-Activation-Only: false
  Base-Replant-Chance: 50.0

Permissions:
  Use-Replanter: "replanter.use"
  Bypass-Consume: "replanter.bypassconsume"
  MCMMO-Level-Skip: "replanter.mcmmoskip"
  Reload-Permission: "replanter.reload"

MCMMO-Levels:
  beginner:
    Required-Level: 0
    Chance: 30.0
    Consume-Saplings: true
  advanced:
    Required-Level: 250
    Chance: 60.0
    Consume-Saplings: false
  master:
    Required-Level: 500
    Chance: 90.0
    Consume-Saplings: false
```
---

## 🧪 mcMMO Integration

If mcMMO is installed **and** enabled in the config:

- The plugin reads the player’s `WOODCUTTING` skill level.
- Configured replant chances and sapling consumption rules are applied based on thresholds defined in `MCMMO-Levels`.
- Players with the `replanter.mcmmoskip` permission ignore mcMMO checks and use base replant logic.

---

## 🧾 Commands

### `/saplingreplanter reload`
Reloads the configuration and mcMMO level data.

- **Permission**: `replanter.reload`

---

## 🔐 Permissions

| Permission | Description |
|-----------|-------------|
| `replanter.use` | Allows a player to trigger automatic sapling replanting |
| `replanter.bypassconsume` | Prevents saplings from being consumed on replant |
| `replanter.mcmmoskip` | Skips mcMMO level checks and uses base logic |
| `replanter.reload` | Allows usage of `/saplingreplanter reload` |

---

## 💡 How It Works

1. A player breaks a tree log.
2. The plugin checks:
    - Block is not player-placed (optional)
    - Player has permission (if enabled)
    - mcMMO level meets requirements (if enabled)
3. A random chance (based on config or mcMMO level) is rolled.
4. If passed, nearby saplings are replanted after a short delay.
5. Saplings are optionally consumed from inventory.

---

## 📝 Notes

- A `placed_blocks.yml` file is used to track blocks placed by players.
- If no saplings are found in inventory (and sapling consumption is enabled), replanting will not occur.

---

## 🛠️ Development

Special thanks to `GaZZip` for the original idea and testing support! 🚀
