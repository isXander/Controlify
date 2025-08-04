# Controlify {version}

This version has the following targets:
{targets}

[![](https://short.isxander.dev/bisect-img)](https://short.isxander.dev/bisect)

**By donating on my [Patreon](https://patreon.com/isxander), you will gain access to builds of Controlify for splitscreen support and snapshot versions**

## Data-driven button guides

Controlify's button guide system (found in-game and in container screens) has been completely rewritten to be data-driven.
This means that resource packs can now add or overwrite button guides.

If you would like to make a resource pack to add or change button guides, consider reading the newly
written documentation on the [Controlify wiki](https://moddedmc.wiki/en/project/controlify/docs/resource-packs/guides).

Although this feature has not been highly requested, people have expressed discontent with the current set of
button guides. Although the guides present in 2.3.0 are the same as before, this new system allows for
players to make their own guides, or mod developers to add guides for their mods.

## Other changes

- New `LEDComponent` for controllers, to allow mods to control the LED on controllers that support it.
- Rewrote the dualsense trigger effect API, to allow mods to use the DualSense's adaptive triggers more easily.

## Bug fixes

- Fixed the crash on startup for some users, with the log `Argument value 0x200000001 exceeds native capacity`
- Fix Switch 1 Joy-Cons having incorrect button glyphs ([by rinOfTheStars]())
