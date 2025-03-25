# Controlify {version}

This version has the following targets:
{targets}

[![](https://short.isxander.dev/bisect-img)](https://short.isxander.dev/bisect)

**By donating on my [Ko-Fi](https://ko-fi.com/isxander), you will gain access to builds of Controlify for snapshot
builds of Minecraft.**

## Changes

- **Added 1.21.5 target** for both Fabric and NeoForge!
- Drastically improved the performance of the on-screen keyboard
  - There is zero performance impact anymore. It used to decrease FPS by 8x (lol)
- Controlify no-longer auto-selects newly connected controllers in order to aid with splitscreen (thanks Mauro for PR)
- Removed explicit immediately-fast support, it works just fine on it's own.
  - ImmediatelyFast is still supported and recommended for performance reason, there's just
    no code in Controlify to support it anymore.

## Bug fixes

- Fix head disappearing and NaN log spam (thanks Mauro for PR)
- Fix unplugging and re-plugging same controller multiple times causing deletion of its config (thanks Mauro for PR)
- Only display the Bluetooth warning when it would affect the user (thanks Mauro for PR)
- Fix resource reload on NeoForge 1.21.4
