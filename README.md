# EnchantXP

Server-side Fabric mod for Minecraft **26.2**. The enchanting table charges experience **points**
instead of levels. Vanilla clients need nothing installed.

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

The price scales with the working bookshelves, counted the same way vanilla does it
(`EnchantingTableBlock.isValidBookShelf`, vanilla caps the power at 15 shelves). A table with no
shelves costs **10 / 20 / 30** points, and the price grows linearly with every shelf up to
**100 / 200 / 300** at 15+ shelves (≈ +6 / +12 / +18 points per shelf; 15 is the cap, a table with
more shelves pays the full price). The shelves are counted where vanilla computes the offers
(`slotsChanged`), so the price always matches the enchantment the player is looking at.
`EnchantmentMenuMixin` hands that number to the player at the click, and the click is refused when
the player has fewer points than the price. Creative (`hasInfiniteMaterials`) skips that check, like
it skips the level and lapis checks in vanilla — though vanilla creative still drains the levels it
charges.

Two injections into `Player.onEnchantmentPerformed`:

1. `HEAD` — remembers `totalExperience` and the scoreboard `Score`.
2. `TAIL` — vanilla has already removed the levels, so the state is rebuilt from `before - price`:
   `totalExperience = 0; experienceLevel = 0; experienceProgress = 0; giveExperiencePoints(target)`.
   `giveExperiencePoints` expands level and progress from total XP exactly (the `while (progress >= 1)` loop
   converges to the state matching `totalExperience`). It also adds the given points to the player's
   score, so the score is put back to the remembered value — vanilla enchanting never touches it.

The enchantment seed advance stays vanilla — the injection does not cancel anything.

The rebuild is silent: `giveExperiencePoints` walks the level up one at a time and the vanilla code plays the
"level up" sound on every level divisible by five, so the mixin moves `lastLevelUpTime` forward before the
rebuild. Without that line every enchantment played the level-up chime.

The level requirement on the buttons (8 / 16 / 30) is untouched: it is a separate value (`costs[i]`) and it is
what the GUI shows.

## Config

`config/enchantxp.json`, read once at startup:

```json
{
  "price_points": [100, 200, 300],
  "price_points_weak": [10, 20, 30],
  "full_bookshelves": 15
}
```

Index 0 is the first button (1 level in vanilla), 1 the second (2 levels), 2 the third (3 levels).
`price_points_weak` is the price at a table with no shelves, `price_points` the price at
`full_bookshelves` shelves, and everything in between is linear. Old configs without the new keys
get the defaults for them.

## Install

Fabric Loader ≥ 0.20.1 on the server, jar into `mods/`.

## Build

```
./gradlew build
```

## License

MIT.
