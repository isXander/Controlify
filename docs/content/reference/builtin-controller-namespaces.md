---
title: Built-in Controller Namespaces
---

# Built-in Controller Namespaces

A controller namespace is the ID of a controller type. Controlify's built-in types are defined in
`assets/controlify/controller/controller_identification.json5`. To add your own, or add controllers to these, see
[Custom Controller Identification](../resource-packs/custom-controller-identification).

A type without its own glyph set uses the `controlify:default` glyphs. A type without its own default bindings
uses only `default.json` (see [Default bindings](../developers/actions#default-bindings)).

| ID | Controllers | Own glyph set | Own default bindings |
|---|---|---|---|
| `controlify:default` | Any controller that no entry matches. The Thrustmaster T16000M also uses this type, with its own name. | Yes | Yes |
| `controlify:xbox` | Xbox controllers: original Xbox, Xbox 360, Xbox One, Xbox Series and Elite. Also the Xbox Wireless Adapter for Windows and the PDP Rematch wired controller. | Yes | No |
| `controlify:dualsense` | DualSense and DualSense Edge (PS5) | Yes | Yes |
| `controlify:dualshock4` | DualShock 4 (PS4) | Yes | No |
| `controlify:dualshock3` | DualShock 3 (PS3) | Yes | No |
| `controlify:psp` | PlayStation Portable | No | No |
| `controlify:switch` | Switch Pro controller, and controllers that report its ID. A pair of Switch 1 Joy-Cons. The 8BitDo SN30 Pro in DInput mode. | Yes | Yes |
| `controlify:steam_deck` | Steam Deck | Yes | No |
| `controlify:steam_controller` | Steam Controller, wired, over Bluetooth LE or through its dongles. | Yes | No |
| `controlify:stadia` | Stadia Controller | No | No |

A controller that isn't listed gets the `default` type, even if it's made for one of these consoles, unless it
reports the same VID and PID as a listed controller. Many Switch-compatible controllers report the Switch Pro
controller's ID.
