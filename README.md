# EasyHomes

A simple and efficient home management plugin for PaperMC servers (compatible with versions 1.8 to 1.21.10). Allows players to set, teleport to, and manage their homes with features like cooldowns, teleport delays, particle effects, and permission-based limits.

## Features

- **Home Management**: Set, teleport to, and delete homes.
- **Teleportation System**: Configurable delay with movement cancellation, particle and sound effects.
- **Title Display**: Shows countdown title during teleportation.
- **Cooldowns**: Prevent spamming with configurable cooldowns.
- **Combat Protection**: Blocks teleportation during combat.
- **Permission-Based Limits**: Flexible home limits via permissions or config.
- **Tab Completion**: Auto-complete home names in commands.
- **Admin Commands**: Reload config without server restart.
- **Multi-Version Support**: Works on Paper 1.8+.

## Installation

1. Download the latest `easyhomes.jar` from the [Releases](https://github.com/bishowsky/EasyHomes/releases) page.
2. Place the JAR file in your server's `plugins/` directory.
3. Restart or reload your server.
4. Configure the plugin in `plugins/EasyHomes/config.yml`.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/home [name]` | Teleport to a home (or list homes if no name) | `easyhomes.home` |
| `/sethome <name>` | Set a home at your location | `easyhomes.sethome` |
| `/delhome <name>` | Delete a home | `easyhomes.delhome` |
| `/easyhomes reload` | Reload the configuration | `easyhomes.reload` |

### Aliases
- `/h` for `/home`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `easyhomes.home` | Use `/home` command | true |
| `easyhomes.sethome` | Use `/sethome` command | true |
| `easyhomes.delhome` | Use `/delhome` command | true |
| `easyhomes.reload` | Use `/easyhomes reload` | op |
| `easyhomes.bypass.cooldown` | Bypass teleport cooldowns | op |
| `easyhomes.bypass.combat` | Bypass combat restrictions | op |
| Custom limits (see config) | Set home limits | varies |

## Configuration

Edit `plugins/EasyHomes/config.yml` to customize the plugin. Below is an example configuration:

```yaml
# EasyHomes Configuration

# Teleportation settings
teleport:
  delay: 3
  cancel-on-move: true
  title:
    enabled: true
    title: "&aTeleporting..."
    subtitle: "&e{time} seconds remaining"
  particles:
    enabled: true
    type: "PORTAL"
    amount: 50
    show-at-start: true
    show-at-destination: true
  sounds:
    enabled: true
    type: "ENDERMAN_TELEPORT"
    volume: 1.0
    pitch: 1.0

# Cooldown settings (in seconds)
cooldowns:
  default: 60
  groups:
    vip: 30
    premium: 15

# Combat tag settings
combat:
  enabled: true
  duration: 10
  block-teleport: true

# Home limit settings
homes:
  default-limit: 1
  limits:
    - permission: "easyhomes.limit.1"
      limit: 1
    - permission: "easyhomes.limit.3"
      limit: 3
    - permission: "easyhomes.limit.5"
      limit: 5
    - permission: "easyhomes.unlimited"
      limit: -1

# Messages (English)
messages:
  prefix: "&8[&6EasyHomes&8]&r "
  home-list: "&aList of your homes: &e{homes}"
  home-teleporting: "&aTeleporting to home &e{home}&a in &e{delay}&a seconds..."
  home-teleport-success: "&aSuccessfully teleported to home &e{home}&a!"
  home-not-found: "&cHome &e{home}&c not found!"
  no-homes: "&cYou have no homes! Use &e/sethome <name>&c to create one."
  sethome-success: "&aHome &e{home}&a has been created!"
  sethome-updated: "&aHome &e{home}&a has been updated!"
  sethome-limit-reached: "&cHome limit reached! You can have a maximum of &e{limit}&c homes."
  sethome-invalid-name: "&cInvalid home name! Use only letters, numbers, and underscores."
  delhome-success: "&aHome &e{home}&a has been deleted!"
  delhome-not-found: "&cHome &e{home}&c not found!"
  teleport-cancelled-move: "&cTeleportation cancelled - you moved!"
  teleport-cancelled-damage: "&cTeleportation cancelled - you took damage!"
  cooldown-active: "&cYou must wait &e{time}&c seconds before teleporting again!"
  combat-active: "&cYou cannot teleport while in combat! Wait &e{time}&c seconds."
  usage-home: "&cUsage: /home [name]"
  usage-sethome: "&cUsage: /sethome <name>"
  usage-delhome: "&cUsage: /delhome <name>"
  usage-easyhomes: "&cUsage: /easyhomes reload"
  no-permission: "&cYou do not have permission to use this command!"
  reload-success: "&aConfiguration reloaded successfully!"
  reload-no-permission: "&cYou do not have permission to reload the configuration!"
```
