# Controlify {version}

This version has the following targets:
{targets}

**By donating on my [Patreon](https://patreon.com/isxander), you will gain access to builds of Controlify for splitscreen support and snapshot versions**

## Rumble Improvements

- Added new outgoing damage rumble
  - Small rumble when you apply damage to other entities directly
  - This works on vanilla servers, but not servers with proprietary protocol translation like Hypixel 1.8
- Adjusted the nearby explosion rumble effect
  - Intensity falloff is much quicker; distant explosions cause less rumble
  - The power (radius) of the explosion is now taken into account for its rumble intensity

## Adaptive Trigger Effect Improvements

- Check client-sided tags on Fabric;
  - Mods that provide tags (like Fabric API tag-conventions-v2) work in predicates, regardless of whether you are connecting to a vanilla server
  - This allows you to target convention tags (`c:` tags), such as `c:tools/bow`, to gain better compatibility with modded bows for example.
- Added a plethora of new trigger effects
  - Subtle vibration with the Brush (any item with tag `#c:tools/brush`)
  - Feedback when using the Spyglass (item `minecraft:spyglass`)
  - Feedback when using Trident-like items (any item with tag `#c:tools/trident`)
  - Feedback when throwing eggs (any item with tag `#minecraft:eggs`), snowballs, splash potions, lingering potions, experience bottles
  - Feedback when deploying Fishing Rod (any item with tag `#c:tools/fishing_rod`)
  - Feedback when emptying a Bundle (any item with non-empty `minecraft:bundle_contents`)
  - Feedback when using Ender Pearls and Wind Charges (any item with `minecraft:use_cooldown`)
- Updated Bow effect to target `#c:tools/bow` instead of `minecraft:bow`
