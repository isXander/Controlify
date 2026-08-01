# Controlify {version}

This version has the following targets:
{targets}

**By donating on my [Patreon](https://patreon.com/isxander), you will gain access to builds of Controlify for splitscreen support and snapshot versions**

## Adaptive Trigger Effects

Controlify now has built-in support for DualSense Adaptive Trigger effects.

This is a never-seen-before feature for Minecraft, on any edition.

The current built-in effects are:

- Bow pull back
- Weapon attack (any item with weapon component)
- Crossbow pull back (any item with charged_projectiles component)
- Crossbow shoot (any item with charged_projectiles component)
- Eating (any item with consumable component)
- Shields (any item with blocks_attacks component)
- Equipping items (any item with equippable component)
- Spears (any item with kinetic_weapon component)
- Goat horns (any item with instrument component)

All of these effects are completely data-driven and can be modified just with a resource pack.
This extends to server-provided resource packs.
Target data components, item tags, vanilla item predicates, and item IDs.

[Check out the wiki](https://moddedmc.wiki/project/controlify/latest/docs/resource-packs/adaptive-trigger-effects)
for instructions on creating resource packs.

## GUI Scale for Button Guides

The GUI scale option for the Ingame and Screen button guides have returned!
Rather than a % scale slider, you can now choose a vanilla GUI scale number, defaulting to one minus the default.

## Bug Fixes

- Fix Mixed Input leaving the virtual mouse unresponsive for 2 seconds (thank you [Ifbusta](https://github.com/isXander/Controlify/pull/939))
- Fix Xbox Controllers not being detected when wired on Windows
