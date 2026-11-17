# EnchantXP

Server-side Fabric mod for Minecraft **26.2**. The enchanting table charges a fixed amount of experience
**points** instead of levels. Vanilla clients need nothing installed.

## The problem

Vanilla `Player.onEnchantmentPerformed` does exactly this (verified against the 26.2 bytecode):

```java
this.experienceLevel -= levels;          // levels = 1 / 2 / 3, the button index + 1
if (this.experienceLevel < 0) { this.experienceLevel = 0; this.experienceProgress = 0.0F; this.totalExperience = 0; }
this.enchantmentSeed = this.random.nextInt();
```

So the third button always costs **3 levels**, and the higher your level the more points that is:

| player level | 3 levels in XP points |
|---|---|
| 30 | 306 |
| 50 | 822 |
| 100 | 2172 |

## What the mod does

Two injections into `Player.onEnchantmentPerformed`:

1. `HEAD` — remembers `totalExperience`.
2. `TAIL` — vanilla has already removed the levels, so the state is rebuilt from `before - price`:
   `totalExperience = 0; experienceLevel = 0; experienceProgress = 0; giveExperiencePoints(target)`.
   `giveExperiencePoints` expands level and progress from total XP exactly (the `while (progress >= 1)` loop
   converges to the state matching `totalExperience`).

The enchantment seed advance stays vanilla — the injection does not cancel anything.

The rebuild is silent: `giveExperiencePoints` walks the level up one at a time and the vanilla code plays the
"level up" sound on every level divisible by five, so the mixin moves `lastLevelUpTime` forward before the
rebuild. Without that line every enchantment played the level-up chime.

The level requirement on the buttons (8 / 16 / 30) is untouched: it is a separate value (`costs[i]`) and it is
what the GUI shows.

## Config

`config/enchantxp.json`, read once at startup:

```json
{ "price_points": [100, 200, 300] }
```

Index 0 is the first button (1 level in vanilla), 1 the second (2 levels), 2 the third (3 levels).
Defaults are what 1/2/3 levels cost at level 30, so nothing changes at level 30 and everything above it gets
cheaper.

## Install

Fabric Loader ≥ 0.20.1 on the server, jar into `mods/`.

## Build

```
./gradlew build
```

## License

MIT.
