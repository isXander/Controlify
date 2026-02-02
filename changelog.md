# Controlify {version}

This version has the following targets:
{targets}

[![](https://short.isxander.dev/bisect-img)](https://short.isxander.dev/bisect)

**By donating on my [Patreon](https://patreon.com/isxander), you will gain access to builds of Controlify for splitscreen support and snapshot versions**

## New Config System

Each controller no longer has its own configuration. 
This means if you switch between multiple controllers, they will share the same configuration.

Instead of "controller settings", these are now referred to as "profiles".

This sets up the foundations of the current in-development splitscreen feature.

The format of the configuration file has completely changed. Controlify will automatically migrate your old configuration to the new format on first run.
Beware that if you downgrade Controlify after running this version, your configuration will be lost.

### Codec-based configuration

This new config system migrates from Gson object serialization to a codec-based system.
This makes everything a lot more explicit and easier to maintain, and has allowed for
much easier versioning and migration of configuration files using data-fixer-upper.

### Resource-pack-driven defaults

You can now bundle default profiles with resource packs.
Each controller type can provide its own default, but because configuration is per-profile and not per-controller.
Once a profile has been created for one controller type, it will not be overridden by another controller type's defaults,
even if you switch controllers.

Documentation coming soon.

## Settings screen overhaul

The carousel settings screen is no more! A new, simpler settings screen has taken its place,
since you can no longer choose between multiple controllers or configure them separately.

This layout has been designed for future expansion, namely adding splitscreen support.

## No more manual controller switching!

You no longer need to go into the settings to switch controllers.
Simply use your second controller, and Controlify will automatically switch to it!

## No more calibration!

Previous versions of Controlify had a pop-up when you connected a new controller, asking you to calibrate it.
This calibration step tried to adjust the deadzones and gyroscope offset for your controller.

- Deadzones are now defaulted to 12% for all controllers.
- Gyroscope offset is now automatically and continually calculated in the background while you play.
