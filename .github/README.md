<div align="center">

![Controlify - Controller support for Minecraft Java](https://raw.githubusercontent.com/isXander/Controlify/multiversion/dev/assets/controlify-banner.png)

<img alt="supports fabric loader" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg">
<img alt="supports neoforge loader" height="56" src="https://raw.githubusercontent.com/intergrav/devins-badges/8494ec1ac495cfb481dc7e458356325510933eb0/assets/cozy/supported/neoforge_vector.svg">

[![wakatime](https://wakatime.com/badge/user/75903a5e-3254-43c5-b168-b082ed4dfc1b/project/62700873-0895-4dae-8159-86692dcceb33.svg)](https://wakatime.com/badge/user/75903a5e-3254-43c5-b168-b082ed4dfc1b/project/62700873-0895-4dae-8159-86692dcceb33)
[![Modrinth download count](https://img.shields.io/modrinth/dt/DOUdJVEm?logo=modrinth&style=flat-square)](https://modrinth.com/mod/controlify)
[![CurseForge download count](https://cf.way2muchnoise.eu/full_835847_downloads.svg)](https://curseforge.com/minecraft/mc-mods/controlify)

[![https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/patreon-singular_vector.svg](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/patreon-singular_vector.svg)](https://patreon.com/isxander)

A mod that adds the best **controller support** for Minecraft: Java Edition.

</div>

# SPLITSCREEN!

(Advertisement)

Controlify Splitscreen is a **separate mod in development** that adds splitscreen support to the game! JARs are currently only available on Patreon!

[![https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/patreon-singular_vector.svg](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/donate/patreon-singular_vector.svg)](https://patreon.com/isxander)

![splitscreen demo](https://cdn.modrinth.com/data/cached_images/23ac6e3fc051e7aaa473ef3af2c510399d9448cc.png)

## What is Controlify?

Controlify is a mod that adds the best controller support to Minecraft: Java Edition. It aims to meet (or exceed) a console-like experience with support for as many controllers as possible, including their advanced features, such as vibration (rumble), gyro, HD haptics and vendor-specific buttons (like mute).

## Why Controlify?

### User friendly

Despite advanced settings available for power users, Controlify is beginner-friendly. It comes by default with
button guides that appear across the whole game, helping you learn the controller layout. It also automatically detects
your controller's make and model, displaying relevant Minecraft-style button textures for a more intuitive experience.
Controlify "just works" out of the box, no need to spend tens of minutes getting your controller feeling like any other
game, thanks to the sane defaults I have created.

### Feature-rich

Controlify strives to support all the added features that may come with your controller.

- It supports gyroscopes natively, allowing for precise movements.
- It comes with vibration/rumble support, making your gameplay more immersive (this is even missing on Bedrock Edition!)
- Soon, HD haptics support for DualSense controllers, something never seen outside a Playstation game.
- Support for non-standard joysticks such as flight sticks, given a small amount of setup.

### Compatibility focus

I, the developer have actively collaborated with fellow mod creators of performance mods like Sodium and Iris to
ensure seamless controller functionality throughout these custom GUIs. This will mean a more friction-free environment
that lets you focus on playing the game, instead of battling through the menus.

## Q&A

### Does it support Steam Deck?

**Yes!** Controlify has been tested and is fully working on the Steam Deck, and works great.
However, due to limitations with SteamOS, only Steam games can currently interface with the gyroscope and the
back buttons directly, though [this is set to change in the coming months](https://github.com/libsdl-org/SDL/issues/9148)!
For now, you can use Steam Input to convert the gyro into a mouse input, and map the back buttons to other, more common
buttons.

### Does it support my controller?

Controlify supports most controllers out of the box, any generic gamepad-like controller is bound to work fine, at
least in a basic state, giving inputs. Though, more strange and uncommon controllers such as handhelds like the ROG
Ally may be missing features such as gyroscope.

If you are having trouble getting your controller working correctly, you can
[join my discord server](https://short.isxander.dev/discord) or consult
[the wiki](https://docs.isxander.dev/controlify/users/controller-compatibility-guide) for assistance.

## Features

### Controller vibration

Controlify supports controller vibration, which has not been seen before for PC versions of Minecraft,
including Bedrock Windows 10 Edition. Configure the intensity of each vibration source, with
complex vibration patterns for lots of aspects of the game (e.g. when you take damage).

![picture of vibration config](https://cdn.modrinth.com/data/DOUdJVEm/images/8a7809d07d9e1d9e3002007d7e5e13b73ce8fb5b.png)

### Radial menu

![radial menu showcase](https://cdn.modrinth.com/data/DOUdJVEm/images/e56d9be363b2b31440e16018cc01f197848b7ac6.webp)

To save up some buttons on your controller, dedicate some less-used actions to the radial menu.
It is fully customizable through the settings and intuitive to use. Any modded keybind is
compatible with this menu.

### Built-in gyro support

Controlify has built-in support for controller gyroscopes, allowing you to make fine movements in-game
with your controller. This can be combined with [flick stick](https://www.reddit.com/r/gamedev/comments/bw5xct/flick_stick_is_a_new_way_to_control_3d_games_with/) to be able to use a controller without
the compromise.

### Container cursor

Just like in bedrock edition, you can move a cursor across your inventory, and interact
with it in a controller-friendly way. With dedicated buttons for quick move, dropping etc. Controlify also comes with cursor snapping, so you don't need to be so precise with cursor movements.

![container cursor screenshot](https://cdn.modrinth.com/data/DOUdJVEm/images/249a2cbaea9b374b33fe67717380e732693dd37a.png)

### Controller identification

Controlify has the ability to identify the make and model of your controller automatically,
and displays relevant button textures for your controller. This is also extendable by resource packs, changing up the button textures in a different style and adding some of your own, more niche controller identifiers.

![demonstration of data-driven identification](https://cdn.modrinth.com/data/DOUdJVEm/images/ec1408d51787b87525b9fb0f2e56b54c5910d384.png)

### Joystick support

You can connect any joystick to the mod and map it with your own names and textures, with an unlimited amount of inputs per controller.

### Button guide

There is a simple overlay in-game that displays the buttons you can press
based on your player's state and look direction. This is useful for new players, who
have not yet memorised the controls.

![image of in-game button guide](https://cdn.modrinth.com/data/DOUdJVEm/images/57c41cee14680c74faf947c5cff355c0af4c35b3.png)

Also, some GUIs display controller buttons on elements that have a controller shortcut, to easily navigate with a controller like you should be able to.

![image of in-screen button guide](https://cdn.modrinth.com/data/DOUdJVEm/images/511e4182137bb27bbdf95539c8265b9af2038761.webp)

### Containerised Controllers

This mod is built around the fact that each controller is completely separate, with it's own configuration, bindings etc.
This means it will be trivial to add support for split-screen play in the future.

### Built for mod compatibility

GUI operation has been abstracted into a simple API, which allows other mods to easily add support for their own GUIs,
without convoluted support throughout the whole mod's codebase, making it difficult for third parties to integrate.

![demonstration of dabr compat](https://cdn.modrinth.com/data/DOUdJVEm/images/8ee5ec167bc5f8be96da725b10707094559138cb.gif)

*Video recorded using do-a-barrel-roll with a Thrustmaster HOTAS flightstick*

### Automatic controller deadzone calibration

The deadzone values of your controller are automatically calibrated,
meaning you don't have to worry about it.

![image of calibration screen](https://cdn.modrinth.com/data/DOUdJVEm/images/f5f8e2a0a05e61adb95dd919760b424165ca5d14.png)

## What is to come?

A few features in various points in the horizon are:

- Explicit Steam Deck support, with the ability to interface with its gyroscope and be able to handle its extra buttons on the back of the device.
- A better way to change controller bindings, possibly a custom graphical GUI to pick what buttons do what actions, not the other way around.
- Split-screen support of some degree.

## Backports?

This mod is only and will only be available for **1.19.4** and above, this is because in 1.19.4, Mojang
introduced arrow key navigation which was easily ported to controller, below 1.19.4, this is not possible.
