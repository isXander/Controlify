# Controlify {version}

This version has the following targets:
{targets}

[![](https://short.isxander.dev/bisect-img)](https://short.isxander.dev/bisect)

**By donating on my [Patreon](https://patreon.com/isxander), you will gain access to builds of Controlify for splitscreen support and snapshot versions**

## Changes

- Add 1.21.11 target
- Allow quickly navigating through YACL option groups with the right stick up/down (secondary gui navigation bindings)
  - This allows you to quickly find categories of controls without having to scroll through the entire list
- Fix behaviours of the "last input type" that Minecraft internally tracks (Ellet #740)
  - This should fix issues on modded GUI screens
- Allow use of Service Loader for entrypoints on Fabric, just like NeoForge
- Increase the width of the action select list when editing radial menu items
- Fix the access to binding registration being in the init context, instead of the pre init context
