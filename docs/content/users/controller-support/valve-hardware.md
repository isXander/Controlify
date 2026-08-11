---
title: Valve Hardware
---

# Valve Hardware

## Steam Deck

<ControllerCompatibility guide="/users/controller-support/valve-hardware#steam-deck" />

Controlify supports standard controller input and rumble on both LCD and OLED Steam Deck models.

::: danger Enhanced driver temporarily unavailable
A SteamOS update broke the integration used by Controlify's enhanced Steam Deck driver, so the driver is disabled
in the latest Controlify release. Installing Decky Loader or changing Controlify's enhanced-driver setting will
not restore it while it is disabled. Standard controller input and rumble continue to work.
:::

The enhanced driver provides the four back buttons, gyro, touchpad clicks, automatic game pausing, and Steam
screenshot integration. These features are unavailable on SteamOS until the driver can be enabled again.

### Set up the enhanced driver

The following setup applies when enhanced driver support is available:

1. Switch the Steam Deck to **Desktop Mode**.
2. Install Decky Loader by following the [official Decky Loader installation page](https://decky.xyz/). Use Decky's
   current instructions rather than older commands copied from another guide.
3. Return to **Gaming Mode**. Controlify's enhanced driver does not work in Desktop Mode because Steam converts the
   built-in controls into keyboard and mouse input there.
4. Start Minecraft with Controlify installed. In Controlify's global settings, leave **Use Enhanced Steam Deck
   Driver** enabled. It is enabled by default.
5. If Minecraft was running while Decky Loader was installed or the setting was changed, restart Minecraft so the
   enhanced driver can connect during startup.

The enhanced driver communicates with SteamOS through its embedded browser debugger. Decky Loader provides this
connection securely, so no Controlify-specific Decky plugin is required.

Once connected, the back buttons and touchpad clicks appear as bindable controller buttons, and Controlify can use the
Deck's gyro for motion aiming. Touch position, movement, and gestures are not currently used by Controlify.

### Troubleshooting

These steps apply after enhanced driver support has been restored. They cannot work around the current SteamOS
incompatibility.

#### Decky Required

Controlify could not connect to SteamOS's browser debugger. Confirm that Decky Loader is installed and running, then
restart Minecraft from Gaming Mode. You do not need to install a plugin from the Decky store for Controlify.

#### Desktop Mode Detected

Return to Gaming Mode before launching Minecraft. Desktop Mode's keyboard-and-mouse conversion prevents Controlify
from using the Steam Deck as its native built-in controller.

#### Enhanced Steam Deck driver not running

Check that **Use Enhanced Steam Deck Driver** remains enabled in Controlify's global settings, that Decky Loader is
available in the Steam Deck Quick Access menu, and that Minecraft was launched after Decky Loader started. Restart
Minecraft after correcting any of these conditions.

## Steam Controller (2026)

<ControllerCompatibility guide="/users/controller-support/valve-hardware#steam-controller-2026" />
