---
title: Feedback Effects
---

# Feedback Effects

Feedback is everything the player feels through the controller: rumble, trigger rumble and HD haptics, and the light
on controllers that have one. A resource pack can change what any feedback feels like, add feedback for new situations,
and decide which strength sliders control it.

There are three kinds of file:

| File | What it decides |
|------|-----------------|
| `assets/<namespace>/controller/feedback/effect/<path>.json` | What an effect feels like |
| `assets/<namespace>/controller/contextual/feedback/in_game.json` | Which effect plays, and when |
| `assets/controlify/controller/feedback/groups.json` | Which strength sliders control each effect, and which effects draw on top |

## Events and effects

An **event** is something that happens: the player takes damage, an explosion goes off, a bow is being drawn.
An **effect** is what that feels like.

By default an event plays the effect with the same id. The event `controlify:explosion` plays the effect at
`assets/controlify/controller/feedback/effect/explosion.json`. To change how an explosion feels, a pack only needs to provide
that file.

Events either **fire** once (damage, an explosion) or are **held** for as long as something is true (drawing a bow,
walking through soul sand).

## Effect files

An effect is a list of tracks. Each track drives one output over time.

```json
{
  "tracks": [
    { "band": "low",  "envelope": [[0, 1], [0.1, 1], [0.1, 0.8], [0.5, 0, "out_quad"]], "scale": "power" },
    { "band": "high", "level": 0.2 }
  ]
}
```

The strength of a track at any moment is:

```text
strength = envelope × level × lfo × scale
```

Every part is optional and counts as 1 when left out. Strengths are from 0 to 1.

### What a track drives

A track has exactly one of these:

| Field | Meaning |
|-------|---------|
| `"band": "low"` | Heavy, low rumble. Works on every controller. |
| `"band": "high"` | Light, high buzz. Works on every controller. |
| `"hz": 120` | A tone at that frequency. Controllers without HD haptics fold it into the nearest band. |
| `"click": { "sharpness": 0.7 }` | A single transient. `sharpness` 0 is a dull thud, 1 is a crisp tick. |
| `"color": "#ff0000"` | The controller's light. See [`color`](#color). |

and may say where it is felt with `"site"`:

| Site | Where |
|------|-------|
| `body` (default) | The whole controller |
| `left`, `right` | One grip. Ordinary rumble motors cannot tell these apart and play both. |
| `left_trigger`, `right_trigger` | The motors inside the triggers, on controllers that have them |

### `color`

A color track paints the controller's light, on controllers that have an RGB light (the DualShock 4 and DualSense
lightbars). `site` is ignored.

The track's strength picks a color from a gradient: a list of `[color, position]` stops, with positions from 0 to 1.
Colors are `#RRGGBB`, or `#RRGGBBAA` for a partly transparent color.

```json
"color": [["#00ff00", 0], ["#ffff00", 0.5], ["#ff0000", 1]]
```

- A single color is shorthand for that color fading in from fully transparent: `"#ff0000"` is
  `[["#ff000000", 0], ["#ff0000", 1]]`. The strength then works as opacity.
- A third value eases the change arriving at a stop, as in an [envelope](#envelope): `["#ff0000", 1, "in_quad"]`.
- Two stops at the same position make a sudden step.
- Below the first stop the first color is used, and above the last stop the last.

Light does not add up like vibration. Each color track is a layer that covers what is beneath it by its opacity: a
damage flash that is fully red hides a blue underwater tint entirely, and fades back to blue as it becomes transparent.
Which layer is on top is decided by [groups and ordering](#groups-and-ordering). Beneath every layer is the light's
default color, a dim blue, so an effect that fades out fades back to it.

Transparent and dark are different. To pulse a layer without letting what is beneath show through, keep every stop
opaque and fade towards a darker color instead: `[["#000040", 0], ["#0000ff", 1]]`.

Turning a strength slider down makes a layer more transparent, not darker.

### `envelope`

A list of `[seconds, strength]` points. The strength moves in a straight line from each point to the next.

```json
"envelope": [[0, 0], [0.5, 1], [2, 0]]
```

This rises to full strength over half a second, then fades out over the next second and a half.

- Two points at the same time make a sudden step.
- A third value eases the line arriving at that point: `linear`, `in_quad`, `out_quad`, `in_out_quad`, `in_sine`
  or `out_expo`: `[2, 0, "out_quad"]`
- After the last point, the last strength is kept.

### `level`

A constant strength. `"level": 0.3` on its own is a steady rumble at 30%.

### `lfo`

A repeating wave that the strength is multiplied by. Use it for anything that pulses.

```json
"lfo": { "period": 0.35, "on": 0.1 }
```

| Field | Meaning |
|-------|---------|
| `period` | Seconds for one full cycle |
| `shape` | `square` (default), `saw`, `triangle` or `sine` |
| `on` | For `square`: seconds of each cycle spent at `max`. Defaults to half the period. |
| `min`, `max` | The range the wave swings between. Default 0 and 1. |

| Shape | What it does over one period |
|-------|------------------------------|
| `square` | At `max` for `on` seconds, then at `min` |
| `saw` | Rises steadily from `min` to `max`, then drops back |
| `triangle` | Rises steadily from `min` to `max`, then falls steadily back |
| `sine` | Swings smoothly up and back |

The anvil is three thuds, written as a square wave that is on for 0.1 s of every 0.35 s:

```json
{
  "duration": 1.05,
  "tracks": [ { "band": "low", "lfo": { "period": 0.35, "on": 0.1 } } ]
}
```

### `scale`

Events pass numbers along with them, called params. `scale` multiplies a track by one.

```json
"scale": "movement"
```

When the number is not already between 0 and 1, remap it:

```json
"scale": { "param": "destroy_time", "from": [0, 20], "to": [0.02, 0.27], "ease": "in_quad" }
```

The param is clamped to the `from` range and stretched onto the `to` range. Here a block that takes no time to break
gives 0.02, one with a destroy time of 20 or more gives 0.27, and `ease` bends the line between them. `from` and `to`
both default to `[0, 1]`.

A param the event does not have counts as zero, which silences the track.

### `delay`

Seconds before the track starts. It is silent until then, and its envelope and LFO count from that moment. For a
`click`, `delay` is when the click happens.

### `requires`

`"requires": "hd"` plays the track only on controllers with HD haptics, and `"requires": "no_hd"` only on controllers
without. This lets one effect have a detailed version and a simple fallback.

### How long an effect lasts

| How the event plays it | When it ends |
|------------------------|--------------|
| Fired | When its longest envelope (or last click) is over, or after `duration` seconds if given |
| Held | When the event stops being held. It then fades out over `release` seconds (default 0). |

An effect with only constant tracks has no length, so a fired event cannot play it unless it has a `duration`.

## Built-in events

| Event | Fired or held | Params |
|-------|---------------|--------|
| `controlify:incoming_damage` | Fired | |
| `controlify:outgoing_damage` | Fired | |
| `controlify:explosion` | Fired at the blast | `power`: the explosion's power |
| `controlify:lightning` | Fired at the bolt | |
| `controlify:item_break` | Fired | |
| `controlify:anvil` | Fired | |
| `controlify:dragon_death` | Fired | |
| `controlify:wither_spawn` | Fired | |
| `controlify:water_land` | Fired | `impact`: 0 to 1 |
| `controlify:fishing_bite` | Held | |
| `controlify:slow_block` | Held | `movement`: 0 to 1 |
| `controlify:block_break` | Held | `destroy_time`: the block's destroy time |
| `controlify:use_item` | Held | `charge_progress`: 0 to 1 while a crossbow charges |
| `controlify:ui_navigate` | Fired | |
| `controlify:ambient` | Held the whole time the player is in a world | See [Ambient feedback](#ambient-feedback) |

Events fired at a position get weaker with distance on their own. Effects do not need to handle it.

## Choosing which effects play

Rules live in `assets/<namespace>/controller/contextual/feedback/in_game.json`. They use the same predicates as
[button guides](guides.md) and [adaptive trigger effects](adaptive-trigger-effects.md).

```json
{
  "rules": [
    { "for": "controlify:use_item", "if": "controlify:use_animation/bow", "then": "controlify:use_item/bow" }
  ]
}
```

A rule says: for this event, if this is true, then play this effect. `controlify:use_item` uses rules to feel
different for a bow, a shield and food. Any event can have rules.

- **Every** rule for the event whose `if` is true plays its effect, so one event can play several effects at once.
- If no rule is true, the event plays the effect with its own id.
- `"then": "controlify:none"` stops there: no rule below it plays. Put it first to silence an event in some situation.

While an event is held, its rules are checked every game tick. An effect starts when its rule becomes true and ends
when it becomes false, and the event's other effects carry on.

### Ambient feedback

`controlify:ambient` is held for as long as the player is in a world. It has no effect of its own, so it only plays
what rules give it. This is how a pack adds feedback for a situation Controlify has no event for.

What that feels like depends on the effect:

- An effect with a constant track is felt the whole time the predicate is true.
- An effect whose envelopes end at 0 or has an explicit duration is felt once, each time the predicate becomes true.

The ambient event's params describe the player, so effects can react to them with [`scale`](#scale):

| Param | Meaning |
|-------|---------|
| `speed` | Horizontal speed in blocks per second, including a vehicle's. Walking is about 4.3. |
| `fall_speed` | Downward speed in blocks per second. 0 when not moving down. |
| `fall_distance` | Blocks fallen so far |
| `health` | 0 to 1, of maximum health |
| `hunger` | 0 to 1, how full the food bar is |
| `air` | 0 to 1, how much breath is left |

A heartbeat that grows as health drops below 30%, played by the rule
`{ "for": "controlify:ambient" "if": "controlify:is_survival", "then": "mypack:heartbeat" }`:

```json
{
  "tracks": [
    {
      "band": "low",
      "lfo": { "period": 0.9, "on": 0.12 },
      "scale": { "param": "health", "from": [0, 0.3], "to": [0.6, 0] }
    }
  ]
}
```

## Groups and ordering

`assets/controlify/controller/feedback/groups.json` puts effects into groups. Every group has a strength slider in
Controlify's settings, and groups can be ordered to decide which effects draw on top of others.

```json
{
  "groups": {
    "controlify:interaction": ["controlify:use_item/bow", "controlify:use_item/consume"],
    "mypack:aiming": ["controlify:use_item/bow", "controlify:use_item/crossbow"]
  },
  "ordering": {
    "controlify:player": { "above": ["controlify:world"] },
    "mypack:ambience": { "below": ["controlify:world"] }
  }
}
```

Every pack's file is merged, from the lowest-priority pack to the highest. In both `groups` and `ordering`, the key
says what to do:

| Key | What it does |
|-----|--------------|
| `"mypack:aiming"` | Adds to what lower packs said |
| `"=mypack:aiming"` | Replaces everything lower packs said about this group |
| `"-mypack:aiming"` | Removes |

Within one file, replacements apply first, then removals, then additions.

### Groups

- An effect can be in several groups. Its strength is the **lowest** of its groups, times the master strength, so a
  player who turns "aiming" off silences the bow even though it is also in "interaction". The master strength is not a
  group: it always applies on top.
- A group that is new gets its own slider. `"=mypack:aiming": []` empties a group but keeps its slider.
- `"-controlify:interaction": ["controlify:use_item/bow"]` takes the bow out of the group.
- Groups list effects, not events. An effect is in the same groups whichever event plays it, so a pack that adds an
  effect, including a new variant like `mypack:use_item/boomerang`, should list it in the groups it belongs in. An
  event's id only groups the effect with that same id.
- An effect in no group is in `controlify:ungrouped`. That group cannot be changed and has no slider, so these
  effects are controlled by the master strength only, but it can be used in `ordering` like any other group.

### Ordering

Ordering decides which effect is on top when two play at once. It matters for outputs that cannot simply be added
together: [light layers](#color), and HD haptics when there are more tones than the controller can play, where the
lowest ones are dropped. Vibration is unaffected.

`"controlify:player": { "above": ["controlify:world"] }` draws player effects over world effects. It is the same
ordering as `"controlify:world": { "below": ["controlify:player"] }`, so removing either form removes it.
`"=controlify:player": { ... }` replaces every ordering involving `controlify:player`, including ones other groups
declared about it.

When two effects play at once, the one on top is:

1. The one whose groups are ranked higher. A group's rank is how many groups are stacked below it, and an effect ranks
   as its highest group. Groups that are not ordered against each other can tie.
2. A fired event over a held one, and a held one over `controlify:ambient`.
3. The newer one.

Mistakes never stop the pack from loading. They are logged and skipped:

- Orderings that contradict each other, like `a` above `b` and `b` above `a`: the higher-priority pack's ordering is
  kept, and the log names both packs.
- An ordering that names a group no pack defines, or orders a group against itself.
- Anything malformed, like an invalid id: just that entry is skipped.
