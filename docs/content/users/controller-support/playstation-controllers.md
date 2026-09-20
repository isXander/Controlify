---
title: Playstation Controllers
---

# Playstation Controllers

<ControllerCompatibility guide="/users/controller-support/playstation-controllers" />

## Fixing touchpad on Linux

On modern Linux systems, there exists a driver, `hid-playstation`, that detects the touchpad as a system touchpad.
If you want to make use of the touchpad in Controlify (currently only using the touchpad as a button), then you will 
need to create a `udev` rule to prevent Linux from recognizing the touchpad.

Create the following udev rule:

```bash
sudo nano /etc/udev/rules.d/99-controlify-playstation-touchpad.rules
```

It may ask for your password, then add:

```
ACTION!="remove", KERNEL=="event[0-9]*", \
    ENV{ID_VENDOR_ID}=="054c", \
    ENV{ID_INPUT_TOUCHPAD}=="1", \
    ENV{LIBINPUT_IGNORE_DEVICE}="1"
```

Save the file (Ctrl+O, then Enter), then reload the rules:

```bash
sudo udevadm control --reload-rules
sudo udevadm trigger
```

Disconnect and reconnect your controller after running the commands.

This prevents any USB/Bluetooth device identifying itself as made by Sony (vendor ID `0x54c`) from being
recognized as a touchpad.

If you want to reverse these changes, delete the file and re-run the commands.
