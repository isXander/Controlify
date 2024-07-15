# Controlify {version}

This version has the following targets:
{targets}

## Bug Fixes

- Fix extreme FPS drops when using the on-screen keyboard.
  - These optimisations greatly depend on the ImmediatelyFast mod, it is now recommended.
- Fix server disconnecting clients without Controlify, with the message IndexOutOfBoundsException.
- Fix crash when changing YACL tabs with the controller.
- Fix key mapping emulations being processed whilst in GUIs, then all being applied at once when the GUI is closed.
