# MaceControl

> Fine-grained control over Mace damage, smash-attack scaling, Density, and Breach — for **Paper 1.21.11 (Mounts of Mayhem)**.

[![Build MaceControl](https://github.com/vulgarmc/MaceControl/actions/workflows/build.yml/badge.svg)](https://github.com/vulgarmc/MaceControl/actions/workflows/build.yml)
![Paper](https://img.shields.io/badge/Paper-1.21.11-blue)
![Java](https://img.shields.io/badge/Java-21-orange)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Features

| What you control | Vanilla default |
|---|---|
| Mace base (ground) damage | 6 HP (3 hearts) |
| Smash-attack Tier 1 damage per block (first 3 blocks) | 4.0 HP/block |
| Smash-attack Tier 2 damage per block (next 5 blocks) | 2.0 HP/block |
| Smash-attack Tier 3 damage per block (all remaining) | 1.0 HP/block |
| Minimum fall distance to trigger a smash attack | 1.5 blocks |
| Density I–V bonus damage per block fallen | 0.5 / 1.0 / 1.5 / 2.0 / 2.5 HP/block |
| Breach I–IV armor-reduction fraction | 15% / 30% / 45% / 60% |

Every value defaults to vanilla — installing the plugin changes nothing until you edit `config.yml`.

---

## Requirements

- **Paper 1.21.11** (Mounts of Mayhem)
- **Java 21**
- No external dependencies

---

## Installation

1. Download the latest JAR from [Releases](https://github.com/vulgarmc/MaceControl/releases) or grab a dev build from [Actions](https://github.com/vulgarmc/MaceControl/actions).
2. Drop it into your server's `plugins/` folder.
3. Start or reload the server — `config.yml` is generated automatically.
4. Edit `plugins/MaceControl/config.yml` to taste.
5. Run `/macecontrol reload` to apply changes without a restart.

---

## Building from source

Requires **Maven 3.8+** and **JDK 21**.

```bash
git clone https://github.com/vulgarmc/MaceControl.git
cd MaceControl
mvn package
# output: target/MaceControl-1.0.0.jar
```

GitHub Actions builds the JAR automatically on every push — see the [Actions tab](https://github.com/vulgarmc/MaceControl/actions).

---

## Commands & Permissions

| Command | Description | Permission | Default |
|---|---|---|---|
| `/macecontrol reload` | Reload config.yml live | `macecontrol.reload` | OP |

---

## Configuration

`plugins/MaceControl/config.yml` — every comment shows the vanilla 1.21 default:

```yaml
mace:
  # Default (vanilla): 6.0 HP  (3 hearts)
  base-damage: 6.0

  smash-attack:
    tier-1-max-blocks: 3          # Default (vanilla): 3
    damage-per-block-tier-1: 4.0  # Default (vanilla): 4.0 HP/block
    tier-2-max-blocks: 5          # Default (vanilla): 5
    damage-per-block-tier-2: 2.0  # Default (vanilla): 2.0 HP/block
    damage-per-block-tier-3: 1.0  # Default (vanilla): 1.0 HP/block
    min-fall-blocks: 1.5          # Default (vanilla): 1.5 blocks

enchantments:
  density:
    level-1-bonus-per-block: 0.5   # Default (vanilla): 0.5
    level-2-bonus-per-block: 1.0   # Default (vanilla): 1.0
    level-3-bonus-per-block: 1.5   # Default (vanilla): 1.5
    level-4-bonus-per-block: 2.0   # Default (vanilla): 2.0
    level-5-bonus-per-block: 2.5   # Default (vanilla): 2.5

  breach:
    level-1-armor-reduction: 0.15  # Default (vanilla): 15% armor ignored
    level-2-armor-reduction: 0.30  # Default (vanilla): 30% armor ignored
    level-3-armor-reduction: 0.45  # Default (vanilla): 45% armor ignored
    level-4-armor-reduction: 0.60  # Default (vanilla): 60% armor ignored
```

### Example: PvP nerf preset

```yaml
mace:
  base-damage: 6.0
  smash-attack:
    tier-1-max-blocks: 3
    damage-per-block-tier-1: 2.0  # halved
    tier-2-max-blocks: 5
    damage-per-block-tier-2: 1.0  # halved
    damage-per-block-tier-3: 0.5  # halved
    min-fall-blocks: 3.0          # harder to trigger

enchantments:
  density:
    level-1-bonus-per-block: 0.25
    level-2-bonus-per-block: 0.5
    level-3-bonus-per-block: 0.75
    level-4-bonus-per-block: 1.0
    level-5-bonus-per-block: 1.25
  breach:
    level-1-armor-reduction: 0.08
    level-2-armor-reduction: 0.15
    level-3-armor-reduction: 0.23
    level-4-armor-reduction: 0.30
```

---

## Authors

- [vulgarmc](https://github.com/vulgarmc)
- [Lostgalaxy](https://github.com/Lostgalaxy)

## License

[MIT](LICENSE)
