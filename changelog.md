# Controlify {version}

This version has the following targets:
{targets}

[![](https://short.isxander.dev/bisect-img)](https://short.isxander.dev/bisect)

## Steam Deck Support

The time has finally arrived: explicit Steam Deck support!

With a little bit of initial setup that Controlify will walk you through, Controlify will now be able to:

- Read the back buttons, gyro and touchpads of your Steam Deck
- Intelligently pause the game when you open the Steam or Quick Access Menu.

Controlify does this be hooking into the internal JavaScript console that runs SteamOS. This allows Controlify a
deep integration into SteamOS. Unfortunately, this deep access also comes at the cost that it all needs to be
reverse-engineered.

Here are some features you should expect to see coming to Controlify very soon:

- Native screenshot handling - Taking a screenshot in the game will add it to the media library of the game, as well as bringing out the screenshot preview popup
- Native keyboard handling - Instead of using the (admittedly crappy) built-in on-screen keyboard by Controlify, you will be able to use the Steam Deck one!

## Other Changes

- Target 1.21.1 rather than 1.21.0
- Add FancyMenu compatibility - modpack creators can now assign a custom action that opens the controller list screen

## Bug Fixes

- Fix the global settings not saving when pressing 'Save'
- Fix 'Out of Focus' look input breaking after resuming the game from pause menu
- Fix crash on modded pause screen that deleted certain widgets
- Fix multiple joystick with same HID data not being differentiated (currently hidden behind debug flag)
- Fix Dualshock3 sprite scaling
- Fix startup crash on 1.20.1 build
- Catch "item group has no page" error and log the offending item group, fixes crash
- Fix broken loading of custom controller mappings when a mapping includes a 'I don't have this input' option
