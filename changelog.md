# Controlify 2.0.0-beta.14

This version has the following targets:
- Fabric 1.20.1
- Fabric 1.20.4
- NeoForge 1.20.4
- Fabric 1.20.6
- NeoForge 1.20.6
- Fabric 1.21
- NeoForge 1.21

## New Features

- New radial menu for debug actions.
  - Open F3 menus with graphs
  - Show/hide chunk boundaries
  - Reload chunks
  - Reload packs
  - Show/hide hitboxes
  - Start/stop profiler
  - Clear chat
- Add special `controlify.placeholder.controller_active` template localisation key.
  - Servers can use this with fallbacks to display a message specifically when a controller is being used, like a button glyph.
- Add a third abstract gui action keybind for developers to use. (Left Stick Press by default).

## Changes

- Add controller shortcuts to the keyboard widget
- Add button shortcuts to Sodium's settings screen.
- Add support for Reese's Sodium Options.
- Finally re-enable Iris compatibility.
- Properly crash the game when Controlify init fails, instead of causing a resource reload rollback
- Better catch errors that happen in `onControlifyInit` entrypoints to allow the game to continue loading.

## Bug Fixes

- Fix extreme FPS drops when using the on-screen keyboard.
  - These optimisations greatly depend on the ImmediatelyFast mod, it is now recommended.
- Fix server disconnecting clients without Controlify, with the message IndexOutOfBoundsException.
- Fix crash when changing YACL tabs with the controller.
- Fix key mapping emulations being processed whilst in GUIs, then all being applied at once when the GUI is closed.
