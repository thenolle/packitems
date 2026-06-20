# PackItems

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Spigot](https://img.shields.io/badge/Spigot-1.21+-ED8106?style=for-the-badge)
![License](https://img.shields.io/badge/License-WTFPL-brightgreen?style=for-the-badge)

![GitHub Repo](https://img.shields.io/badge/GitHub-thenolle%2Fpackitems-181717?style=for-the-badge&logo=github)
![GitHub Release](https://img.shields.io/github/v/release/thenolle/packitems?style=for-the-badge)
![Downloads](https://img.shields.io/github/downloads/thenolle/packitems/total?style=for-the-badge)
![Issues](https://img.shields.io/github/issues/thenolle/packitems?style=for-the-badge)

PackItems is a high-performance custom item management and action routing system for Spigot 1.21+ servers.

It automatically extracts item assets, generates compressed resource packs, and deploys them to clients using a built-in HTTP hosting system, while providing an immersive YAML-driven execution layer for custom tools, weapons, and interactive items.

---

## Features

* Automatic asset extraction and item model definition generation
* Integrated high-performance HTTP server for automated distribution
* Single-pass compressed zip building with cryptographic SHA-1 validation
* Automatic prompt execution forcing pack installation on server join
* Directory-isolated per-item configuration structures
* Complete custom model data index mapping control
* Flexible trigger routing framework tracking interaction, movement, and combat states
* Rich sequential action execution pipeline with explicit flow control capabilities
* In-depth player attribute modifiers, custom stack sizing, and enchantment overrides
* Native compatibility with TextAPI tag formatting and placeholders
* Localized player cooldown tracking with fallback vanilla graphic overlays
* Optimized multi-threaded execution suite preventing runtime processing ticks

---

## Installation

### Build

```bash
mvn clean package

```

### Install

Place the generated jar inside:

```text
/plugins/PackItems-1.0.0.jar

```

Restart the server.

---

## Configuration

The main configuration file manages the integrated HTTP network listener and distribution policies for deployment.

```yml
resource-pack:
  enabled: true
  send-on-join: true
  force: false
  server:
    enabled: true
    host: "0.0.0.0"
    port: 8080
    path: "/packitems.zip"
    public-url: ""

```

---

## Item Directory Structure

Custom items are declared inside unique subfolders located within the items parent folder. Every item declaration requires a valid item.yml and a companion texture file.

```text
/plugins/PackItems/items/
    ├── ruby_sword/
    │   ├── item.yml
    │   ├── texture.png
    │   └── model.json
    └── heavy_shield/
        ├── item.yml
        └── texture.png

```

---

## Item Configuration Guide

The item.yml configuration manages metadata parameters, baseline item profiles, structural modifiers, and mapped trigger chains.

### Example Configuration

```yml
name: "<gradient:#ff0044:#ff00aa>Ruby Sword</gradient>"
lore:
  - "<gray>A legendary sword forged in deep crimson blood.</gray>"
  - "<dark_purple>Power Level: IX</dark_purple>"

material: DIAMOND_SWORD
render: HANDHELD

item:
  max-stack-size: 1
  unbreakable: true
  custom-model-data: 1001
  flags:
    - HIDE_ATTRIBUTES
    - HIDE_ENCHANTS
  enchantments:
    - type: SHARPNESS
      level: 5
    - type: UNBREAKING
      level: 3
  attributes:
    - attribute: GENERIC_ATTACK_DAMAGE
      amount: 8.5
      operation: ADD_NUMBER
      slot: HAND
    - attribute: GENERIC_ATTACK_SPEED
      amount: -2.4
      operation: ADD_NUMBER
      slot: HAND

actions:
  RIGHT_CLICK_AIR:
    - type: check_cooldown
    - type: message
      text: "<red>You have unleashed the crimson shockwave!</red>"
    - type: play_particle
      particle: FLAME
      count: 20
    - type: add_potion_effect
      effect: INCREASE_DAMAGE
      duration: 100
      amplifier: 1
    - type: set_cooldown
      ticks: 120

```

### Configuration Options Reference

| Key | Description | Valid Parameter Types |
| --- | --- | --- |
| `name` | Item display title matching TextAPI syntax | Raw or tag-formatted string |
| `lore` | Text collection assigned under the item metadata | Ordered text list entries |
| `material` | Underlying Minecraft baseline structural item | Standard Bukkit Material Enum name |
| `render` | Generation model formatting style in hands | `FLAT` or `HANDHELD` |
| `item.max-stack-size` | Maximum limit allowed inside inventory slots | Numeric values from `1` to `64` |
| `item.unbreakable` | Prevents degradation of internal items durability | `true` or `false` |
| `item.custom-model-data` | Custom model data identification index assignment | Explicit positive integers |
| `item.flags` | Meta flags managing visibility details on tags | Standard Bukkit ItemFlag list |
| `item.enchantments` | Enchantments added directly to the object | Explicit type and level mappings |
| `item.attributes` | Attribute modifier values injected into user traits | Attribute, amount, operation, slot |

---

## Triggers Reference

Triggers indicate exactly when action lists are parsed during player context interactions.

### Interaction Triggers

* `RIGHT_CLICK_AIR` : Player right-clicks while aiming at open air.
* `RIGHT_CLICK_BLOCK` : Player right-clicks an active physical block target.
* `LEFT_CLICK_AIR` : Player swings their item or left-clicks open air space.
* `LEFT_CLICK_BLOCK` : Player left-clicks or strikes an active block structure.
* `SHIFT_RIGHT_CLICK_AIR` : Player sneaks and performs a right-click into the sky.
* `SHIFT_RIGHT_CLICK_BLOCK` : Player sneaks and performs a right-click on a block.
* `SHIFT_LEFT_CLICK_AIR` : Player sneaks and performs a left-click into the air.
* `SHIFT_LEFT_CLICK_BLOCK` : Player sneaks and performs a left-click on a block.

### Combat and Entity Triggers

* `HIT_ENTITY` : Player delivers a standard melee attack onto an active entity target.
* `CRIT_ENTITY` : Player lands an official critical hit attack strike while airborne.
* `PROJECTILE_HIT` : Projectile launched from this active item strikes a block or entity.
* `BOW_SHOOT` : Player fires an arrow utilizing a standard bow base material item.
* `CROSSBOW_SHOOT` : Player fires an object utilizing a crossbow base material item.
* `TRIDENT_THROW` : Player launches a trident tracking item into the world.

### State and Movement Triggers

* `JUMP` : Player executes a standard vertical jump action into the air.
* `SNEAK_START` : Player toggles their state and starts crouching.
* `SNEAK_STOP` : Player toggles their state and stops crouching.
* `SWAP_HAND` : Player triggers the hotkey to switch items to their offhand slot.
* `DROP_ITEM` : Player drops the custom item out of their active inventory.

### World Interaction Triggers

* `BLOCK_BREAK` : Player successfully destroys a block using the active item.
* `BLOCK_PLACE` : Player positions and finishes setting down a block structure.
* `FALL_DAMAGE` : Player encounters kinetic impact or falling damage parameters.
* `DEATH` : Player health markers reach absolute zero values.

---

## Actions Reference

Actions represent distinct instructions processed in a sequential pipeline when a trigger conditions match.

### Chat and Audio

#### message

Transmits private styled text directly to the triggering user context.

```yml
type: message
text: "<green>Your weapon glows with holy light.</green>"

```

#### broadcast

Transmits a server-wide announcement incorporating standard player name formatting.

```yml
type: broadcast
text: "<yellow>%player% has discovered an ancient relic!</yellow>"

```

#### sound

Plays an auditory track centered at the exact location metrics of the player.

```yml
type: sound
sound: ENTITY_LIGHTNING_BOLT_THUNDER
volume: 1.0
pitch: 1.5

```

#### stop_sound

Forcibly terminates audio tracks based on sound reference keys or channel categories.

```yml
type: stop_sound
sound: MUSIC_DISC_PIGSTEP
category: RECORDS

```

### Flow Control

#### abort

Immediately halts action processing loops, terminating any subsequent actions down the line.

```yml
type: abort

```

### Core Mechanics

#### cancel_event

Cancels the intercepted vanilla game engine interaction event, preventing blocks from breaking, items from moving, or weapons from firing.

```yml
type: cancel_event

```

#### consume_item

Deducts an explicit number of units from the active hand item stack.

```yml
type: consume_item
amount: 1

```

#### damage_item

Inflicts durability degradation damage directly onto tools matching damageable definitions.

```yml
type: damage_item
amount: 5

```

### Player State

#### heal

Injects extra health points into the player up to their maximum health cap parameter.

```yml
type: heal
amount: 6.0

```

#### damage

Deals raw untyped damage directly onto the acting player entity profile.

```yml
type: damage
amount: 4.0

```

#### set_fire

Ignites the player entity for a specified integer duration of tracking ticks.

```yml
type: set_fire
ticks: 60

```

#### extinguish

Instantly quenches fire ticks, removing any active burning status on the player.

```yml
type: extinguish

```

#### add_potion_effect

Gives an active potion status condition effect to the player context.

```yml
type: add_potion_effect
effect: REGENERATION
duration: 200
amplifier: 2

```

#### remove_potion_effect

Removes a single targeting status potion effect type from the player.

```yml
type: remove_potion_effect
effect: BLINDNESS

```

#### clear_potion_effects

Clears out all running active potion effects simultaneously from the target.

```yml
type: clear_potion_effects

```

#### feed

Restores raw food hunger points and saturation levels up to the maximum cap of 20.

```yml
type: feed
amount: 5

```

### World and Execution

#### command_player

Forces the player to dispatch a localized server command string directly.

```yml
type: command_player
command: "home"

```

#### command_console

Dispatches an elevated administrative console command replacing the user placeholder value.

```yml
type: command_console
command: "give %player% diamond 1"

```

#### play_particle

Spawns a localized cloud of visual particle objects around the user coordinates.

```yml
type: play_particle
particle: SOUL
count: 30

```

#### strike_lightning

Tracks the player eye vector using ray trace algorithms up to a specific limit, spawning high-voltage lightning strikes at the intersection coordinate.

```yml
type: strike_lightning
range: 24.0

```

#### explosion

Triggers a high-impact explosion tracking target blocks, target entities, or fallback user coordinates.

```yml
type: explosion
power: 4f

```

#### push

Applies immediate velocity vector acceleration directly to the player.

```yml
type: push
x: 0.0
y: 1.2
z: 0.0

```

### Block Interactions

#### set_block

Overwrites the physical block state being targeted with an alternative material type.

```yml
type: set_block
material: AIR

```

#### break_block

Instructs the targeted block instance to break naturally while yielding its matching dropped materials.

```yml
type: break_block

```

### Target Interactions

#### damage_target

Inflicts combat damage tracking flags directly on hit entities or targets.

```yml
type: damage_target
amount: 7.0

```

#### heal_target

Applies restorative medical health adjustments onto target entities.

```yml
type: heal_target
amount: 4.0

```

#### ignite_target

Sets the focused target block or ray traced target entity on fire.

```yml
type: ignite_target
ticks: 100
range: 10.0

```

#### add_potion_effect_target

Injects a status effect map directly into the hit entity target.

```yml
type: add_potion_effect_target
effect: GLOWING
duration: 300
amplifier: 0

```

### Cooldowns

#### check_cooldown

Inspects usage map records for restrictions, aborting the action flow if an item is still cooling down.

```yml
type: check_cooldown

```

#### set_cooldown

Enforces a global item-scoped cooldown tick timer while applying a standard visual cooldown sweep over matching hand materials.

```yml
type: set_cooldown
ticks: 80

```

---

## Commands

### Main Command

```text
/packitems

```

### Subcommands

```text
/packitems reload
/packitems rebuild
/packitems list
/packitems give <player> <item_id> [amount]

```

### Command Reference

| Subcommand | Description |
| --- | --- |
| `reload` | Wipes registry map definitions and completely reloads item configurations |
| `rebuild` | Triggers resource pack compilation, zipping, and cryptographic calculation |
| `list` | Lists the identifying tracking codes of all registered items |
| `give` | Generates a custom item stack instance and gives it to a specific player |

---

## Performance Notes

* Constant-time O(1) tracking lookups utilizing concurrent hash map frameworks
* Single-pass structural event routing featuring custom debounce protection logic
* Memory-mapped tracking keys ensuring identity mapping using native NamespacedKey indicators
* Multi-threaded asset delivery threads managing resource zip downloads asynchronously
* Completely cached configuration parameters mitigating heavy runtime disc lookup operations
* Auto-cleanup operations flushing isolated session datasets upon player disconnect events

---

## Permissions

```text
packitems.command

```

Default:

```text
op

```

### Detailed Permission Node Map

| Node | Description | Default |
| --- | --- | --- |
| `packitems.reload` | Permits execution of the configuration reload module | op |
| `packitems.rebuild` | Permits manual compilation of asset packages | op |
| `packitems.list` | Permits review of registered item identifier lists | op |
| `packitems.give` | Permits item distribution mechanics to active players | op |
| `packitems.use.<item_id>` | Restricts or grants active item usage for a specific ID | all |
| `packitems.use.*` | Global wildcard node allowing use of all custom items | all |

---

## Compatibility

* Spigot 1.21+
* Paper 1.21+
* Java 21+
* Kotlin 2.4+
* [TextAPI](https://github.com/thenolle/textapi) Integration

---

## License
<a href="http://www.wtfpl.net/">
<img src="http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png"
width="80"
height="15"
alt="WTFPL" />
</a>
