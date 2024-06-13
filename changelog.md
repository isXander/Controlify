# Controlify 2.0.0-beta.11

This build brings incomplete support for NeoForge for 1.20.6 and 1.20.4.
These builds are missing all networking features.

This version has the following targets:
- Fabric 1.20.1
- Fabric 1.20.4
- NeoForge 1.20.4
- Fabric 1.20.6
- NeoForge 1.20.6
- Fabric 1.21

## Changes

- Fallback to default controller font set if the font mapping file does not exist for the controller's namespace
- Add a controller icon for PSP (thanks \_cheburkot\_)
- Add incomplete support for NeoForge 1.20.4 and 1.20.6
- Fix compatibility with Fabric API 0.100+ on 1.20.6
- Update to the SDL library, this will be redownloaded
- Fix registry sync issue that prevented non-Controlify clients joining Controlify servers.
- Fix the default controller sprite being displayed as purple/black.
