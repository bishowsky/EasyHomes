# EasyHomes

**Professional home management system for PaperMC/Spigot servers**

Modern, high-performance home plugin with MySQL, caching, economy, and advanced integrations. Free and open-source!

**Author:** Bishyy  
**Discord Support:** https://discord.gg/mkyU3SgBUP  
**Version:** 1.0.0  
**Compatibility:** Paper/Spigot 1.8+ (recommended 1.13+)

## ✨ Features

### 🏠 Home Management
- **MySQL Storage** - Professional database with HikariCP connection pool
- **YAML Fallback** - Automatic fallback to YAML when MySQL unavailable
- **Guava Cache** - Intelligent caching with TTL and auto-eviction
- **Async Operations** - All IO operations are asynchronous
- **Per-Permission Limits** - `easyhomes.limit.1/3/5/10/15/25/50/unlimited`

### 🚀 Teleportation System
- **Configurable Delay** - Delay before teleportation (with move cancellation)
- **Particle Effects** - Particle effects (PORTAL, FLAME, etc.)
- **Sound Effects** - Teleportation sounds
- **Title/Subtitle** - Countdown on player screen
- **Combat Tag** - Block teleportation during combat

### 💰 Economy (Vault)
- **Teleportation Costs** - Fixed cost + distance cost
- **Home Creation Costs** - Paid home slots
- **Refund System** - Money refund when deleting homes
- **Bypass Permissions** - `easyhomes.bypass.cost`

### 🛡️ WorldGuard Integration
- **Region Permissions** - Check build permissions
- **Blocked Regions** - List of regions where homes cannot be set
- **Safe Zones** - Integration with region system

### 📊 PlaceholderAPI
- `%easyhomes_count%` - Player's home count
- `%easyhomes_limit%` - Home limit
- `%easyhomes_available%` - Available slots
- `%easyhomes_list%` - Home list (comma-separated)
- `%easyhomes_cooldown%` - Remaining cooldown
- `%easyhomes_combat%` - Remaining combat tag time
- `%easyhomes_has_homes%` - true/false
- `%easyhomes_in_combat%` - true/false
- `%easyhomes_at_limit%` - true/false

### 🐛 Debug System
- **Toggle Command** - `/easyhomes debug on/off`
- **File Logging** - Logs saved to `plugins/EasyHomes/logs/`
- **Performance Tracking** - Database operation monitoring
- **Command Logging** - Command execution history

## 📥 Installation

1. Download the latest `easyhomes.jar`
2. Place JAR in `plugins/` folder
3. **(Optional)** Configure MySQL in `config.yml`
4. Start/restart server
5. Done!

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/home [name]` | Teleport to home (or list homes) | `easyhomes.home` |
| `/sethome <name>` | Set home at current location | `easyhomes.sethome` |
| `/delhome <name>` | Delete home | `easyhomes.delhome` |
| `/easyhomes reload` | Reload configuration | `easyhomes.reload` |
| `/easyhomes debug <on/off>` | Enable/disable debug mode | `easyhomes.debug` |

### Aliases
- `/h` = `/home`
- `/eh` = `/easyhomes`
- `/remhome` = `/delhome`

## 🔑 Permissions

### Basic
| Permission | Description | Default |
|------------|-------------|---------|
| `easyhomes.*` | All permissions | op |
| `easyhomes.home` | `/home` command | true |
| `easyhomes.sethome` | `/sethome` command | true |
| `easyhomes.delhome` | `/delhome` command | true |
| `easyhomes.reload` | Reload config | op |
| `easyhomes.debug` | Debug mode | op |

### Bypasses
| Permission | Description |
|------------|-------------|
| `easyhomes.bypass.cooldown` | Bypass cooldown |
| `easyhomes.bypass.combat` | Bypass combat tag |
| `easyhomes.bypass.cost` | Bypass economy costs |

### Home Limits
| Permission | Homes |
|------------|-------|
| `easyhomes.limit.1` | 1 home |
| `easyhomes.limit.3` | 3 homes |
| `easyhomes.limit.5` | 5 homes |
| `easyhomes.limit.10` | 10 homes |
| `easyhomes.limit.15` | 15 homes |
| `easyhomes.limit.25` | 25 homes |
| `easyhomes.limit.50` | 50 homes |
| `easyhomes.unlimited` | Unlimited |

## ⚙️ Configuration

### Storage (MySQL or YAML)

```yaml
storage:
  type: MYSQL  # or YAML for fallback
  mysql:
    host: localhost
    port: 3306
    database: minecraft
    username: root
    password: ""
    use-ssl: false
    pool-size: 10
    connection-timeout: 5000

cache:
  enabled: true
  ttl-seconds: 300  # 5 minutes
  max-size: 10000   # max players in cache
```

### Economy (requires Vault)

```yaml
economy:
  enabled: false
  teleport-cost: 100      # teleport cost
  cost-per-distance: 0.1  # 0.1 per block
  sethome-cost: 500       # home creation cost
  refund-on-delete: true  # refund on deletion
  refund-percentage: 50   # 50% refund
```

### Full config.yml

Check `config.yml` in the plugin for full configuration with English comments!

## 🔧 For Developers

### Building

```bash
git clone https://github.com/bishowsky/EasyHomes.git
cd EasyHomes
gradlew shadowJar
```

JAR will be in `build/libs/easyhomes.jar`

### Project Structure

```
EasyHomes/
├── commands/         - Commands (/home, /sethome, /delhome)
├── database/         - MySQL (DatabaseManager, MySQLStorage)
├── hooks/            - Integrations (Vault, PlaceholderAPI, WorldGuard)
├── listeners/        - Event listeners
├── manager/          - Business logic (Home, Combat, Cooldown, Teleport)
├── model/            - Data models (Home)
├── storage/          - Storage abstraction (YAML/MySQL)
└── util/             - Utilities (Debug, Message, etc.)
```

## 🆘 Support

- **Discord:** https://discord.gg/mkyU3SgBUP
- **Issues:** [GitHub Issues](https://github.com/bishowsky/EasyHomes/issues)
- **Wiki:** [GitHub Wiki](https://github.com/bishowsky/EasyHomes/wiki)

## 📜 License

Free and open-source! Use as needed.

## 🙏 Credits

- **Paper Team** - For great API
- **PlaceholderAPI** - For placeholder system
- **Vault** - For economy unification
- **WorldGuard** - For region system
- **HikariCP** - For efficient connection pooling
- **Guava** - For cache system

## 📝 Changelog

### v1.0.0 (2026-01-11)
- ✨ MySQL storage with HikariCP
- ✨ Guava cache with TTL and auto-eviction
- ✨ Vault integration (economy)
- ✨ PlaceholderAPI support
- ✨ WorldGuard integration
- ✨ Debug system (`/easyhomes debug`)
- ✨ Per-permission limits (1/3/5/10/15/25/50/unlimited)
- ✨ English messages
- ✨ Auto-fallback to YAML when MySQL unavailable
- 🐛 Many performance fixes
- 📝 Full documentation

---

**Made with ❤️ by Bishyy**  
**Discord:** discord.gg/mkyU3SgBUP
