---
title: Feedback
---

# Feedback

_Let the player feel what happens: rumble, HD haptics, trigger motors and the controller's light._

Events trigger feedback, rules select an effect, and effects play:

- An **event** triggers feedback. Controlify fires its own, like `controlify:ambient`, which is held the whole time
  the player is in a world, and your mod can fire more.
- **Rules** select which effect an event plays, depending on what's happening in the game.
- An **effect** is what the player feels. Effects are usually data files.

Effects and rules are resource files, so resource packs, including a server's resource pack, can change how
anything feels, or add feedback for new situations, without any code.

This page adds feedback to the example addon's boat. It starts with effects, then selects them for the ambient
event with rules, and only then adds events of its own for what rules can't describe.

## Effects

An effect lives at `assets/<namespace>/controller/feedback/effect/<path>.json`, and its ID is `<namespace>:<path>`.
The example's boost, `examplemod:boost`, is a light buzz and an orange light:

<<< @/../../src/example/resources/assets/examplemod/controller/feedback/effect/boost.json

An effect is a list of tracks. Each track drives one output, and its strength at any moment is:

```text
strength = envelope × level × lfo × scale
```

Every part is optional, and counts as 1 when left out. Strengths go from 0 to 1.

### Outputs

A track has exactly one output:

| Field | Output |
|---|---|
| `"band": "low"` | A heavy, low rumble. Every controller can play it. |
| `"band": "high"` | A light, high buzz. Every controller can play it. |
| `"hz": 180` | A tone at that frequency, for HD haptics. Other controllers play it in the nearest band. |
| `"click": { "sharpness": 0.7 }` | A single transient. `sharpness` goes from 0, a dull thud, to 1, a crisp tick. Default 0.5. |
| `"color": "#ff8000"` | The controller's light. See [light](#light). |

`"site"` says where a vibration is felt:

| Site | Where |
|---|---|
| `body` (default) | The whole controller |
| `left`, `right` | One grip. Ordinary rumble motors can't tell them apart, and play both. |
| `left_trigger`, `right_trigger` | The motors in the triggers, on controllers that have them |

How each kind of controller plays them:

| Controller | Bands | Tones | Clicks |
|---|---|---|---|
| Ordinary rumble | The two motors | In the nearest band | A short burst |
| Trigger motors | Trigger sites only | Strength only | A short burst |
| DualSense, with HD haptics on | Synthesised | Synthesised at their frequency | Synthesised |
| Steam Controller (2026) | The rumble motors | A tone | A tick or click |

### Strength

| Field | Effect on the track |
|---|---|
| `envelope` | A list of `[seconds, strength]` points, joined by straight lines. After the last point, its strength is kept. |
| `level` | A constant strength. Default 1. |
| `lfo` | A repeating wave, for anything that pulses. |
| `scale` | Multiplies by a number that whatever plays the effect provides, called a [param](#params). |
| `delay` | Seconds before the track starts. Its envelope and LFO count from then. For a click, it's when the click happens. |
| `requires` | `"hd"` plays the track only on controllers with HD haptics, and `"no_hd"` only on those without, so an effect can have a detailed version and a simple fallback. |

In an **envelope**, two points at the same time make a sudden step. A third value eases the line arriving at that
point: `linear`, `in_quad`, `out_quad`, `in_out_quad`, `in_sine` or `out_expo`, as in `[0.6, 0, "out_quad"]`.

An **lfo** is `{ "period": 0.35, "on": 0.1 }`:

| Field | Meaning |
|---|---|
| `period` | Seconds for one cycle |
| `shape` | `square` (default), `saw`, `triangle` or `sine` |
| `on` | For `square`, seconds of each cycle spent at `max`. Default half the period. |
| `min`, `max` | The range the wave swings between. Default 0 and 1. |

Controlify's anvil is three thuds, as a square wave on for 0.1 seconds of every 0.35:

<<< @/../../src/main/resources/assets/controlify/controller/feedback/effect/anvil.json

<a id="params"></a>**scale** is a param's name, like `"scale": "health"`, when the param already goes from 0 to 1.
Otherwise, map it:

```json
"scale": { "param": "destroy_time", "from": [0, 20], "to": [0.02, 0.27], "ease": "in_quad" }
```

The param is clamped to `from`, and mapped onto `to`, with `ease` bending the line. `from` and `to` default to
`[0, 1]`. A param that isn't provided counts as 0, which silences the track.

### Light

A `color` track is a layer on the controller's light, on controllers that have one, like the DualShock 4 and
DualSense. It ignores `site`.

The track's strength picks a colour from a gradient: a list of `[colour, position]` stops, with positions from 0 to
1. Colours are `#RRGGBB`, or `#RRGGBBAA` for a partly transparent colour.

```json
"color": [["#00ff00", 0], ["#ffff00", 0.5], ["#ff0000", 1]]
```

- A single colour fades in from fully transparent: `"#ff8000"` is `[["#ff800000", 0], ["#ff8000", 1]]`. The
  strength then works as opacity.
- A third value eases towards that stop, as in an envelope.
- Two stops at the same position make a sudden step.

Between stops, colours are interpolated in OKLab, a colour space where equal steps look equal. So the halfway point
between two colours looks halfway between them, rather than muddy or dim, and fading to a transparent stop doesn't
tint the colour beside it.

Light layers don't add up like vibration. Each layer covers what's beneath it by its opacity, and
[ordering](#ordering) decides which is on top. Beneath every layer is the light's default colour, so a layer that
fades out fades back to it. Because colours are blended by opacity rather than mixed, a layer never passes through
other hues on its way between colours.

So use light freely, but think about what else might be playing. A pack could tint the light by biome, or blue
underwater, and a red damage flash would still cover it and then fade back. To change colour without the layer
beneath showing through, start the gradient fully opaque at position 0. Turning a strength slider down makes a
layer more transparent, not darker.

### How long an effect lasts

An effect is either played once, or held for as long as something keeps it going:

| Played | The effect ends |
|---|---|
| Once | When its longest envelope or last click is over, or after `duration` seconds if it has one. |
| Held | When it stops being held, fading out over `release` seconds, default 0. Or after `duration` seconds, if it has one. |

An effect with only constant tracks has no length, so it can only be held, unless it has a `duration`. The boost
is held for as long as the player boosts, and fades out over 0.3 seconds after.

## Selecting effects with rules

Rules select the effects an event plays, depending on what's happening in the game. They test the state of a
[contextual domain](contextual-api): what the player is holding, looking at, riding, standing in, and much more.
Feedback rules use the in-game domain, `controlify:in_game`, so they have the same predicates as in-game button
guides and adaptive trigger effects. See [Predicates](contextual-api#predicates) for
how to write them.

A domain's feedback rules live in `assets/<domain namespace>/controller/contextual/feedback/<domain path>.json`, so
the in-game rules are `assets/controlify/controller/contextual/feedback/in_game.json`. The file merges across mods
and resource packs. The example's rules play the boost while the player boosts, and tint the light blue while
they're in a boat:

<<< @/../../src/example/resources/assets/controlify/controller/contextual/feedback/in_game.json

A rule says: for this event, if this is true, play this effect. Both rules are for `controlify:ambient`. It has no
effect of its own, so it only plays what rules select, for as long as they're true:

- An effect with a constant track, like the boost, is felt the whole time the rule is true.
- An effect that ends by itself is felt once, each time the rule becomes true.

Controlify uses ambient rules too. The rumble of walking on soul sand or honey is an ambient rule that selects
`controlify:slow_block` while `controlify:on_slow_block` and `controlify:input_moving` are true.

Rules work the same for every event, not just ambient:

- **Every** rule for the event whose `if` is true plays its effect, so one event can play several effects at once.
  Controlify's `controlify:use_item` uses rules to feel different for bows, shields and food.
- If no rule is true, the event plays the effect with its own ID, unless a rule selects that effect for the event.
  Then it only plays while such a rule is true, so a pack can put a condition on an event's own effect.
- `"then": "controlify:none"` stops there, so no rule below it plays. Put it first to silence an event in some
  situation.
- A held event checks its rules each time it's held. An effect starts when its rule becomes true and ends when it
  becomes false, while the event's other effects carry on.

### Ambient params

The ambient event provides these params, for effects to [scale](#params) by:

| Param | Meaning |
|---|---|
| `speed` | Horizontal speed in blocks per second, including a vehicle's. Walking is about 4.3. |
| `fall_speed` | Downward speed in blocks per second. 0 when not moving down. |
| `fall_distance` | Blocks fallen so far |
| `health` | 0 to 1, of maximum health. Absorption isn't included. |
| `hunger` | 0 to 1, how full the food bar is |
| `air` | 0 to 1, how much breath is left |

Controlify's heartbeat thumps and pulses the light red, more strongly as health drops below 30%:

<<< @/../../src/main/resources/assets/controlify/controller/feedback/effect/heartbeat.json

### Using your mod's facts

Rules can only test what the domain knows, so the facts your mod adds to it are what make ambient rules work for
your mod. The example's rules test two facts it added on the [Contextual API](contextual-api#adding-facts) page:
the derived fact `examplemod:in_boat`, and the source fact `examplemod:boosting`.

Add facts wherever they describe the situation, rather than adding an event. The same facts also work for button
guides and trigger effects.

### Controlify's events

Controlify fires its own events for things that happen in the game. Rules can be written for them, and a pack can
replace any of their effects:

| Event | Played | Params |
|---|---|---|
| `controlify:incoming_damage` | Once | |
| `controlify:outgoing_damage` | Once | |
| `controlify:explosion` | Once, at the blast | `power`: the explosion's power |
| `controlify:lightning` | Once, at the bolt | |
| `controlify:item_break` | Once | |
| `controlify:anvil` | Once | |
| `controlify:dragon_death` | Once | |
| `controlify:wither_spawn` | Once | |
| `controlify:water_land` | Once | `impact`: 0 to 1 |
| `controlify:fishing_bite` | Held | |
| `controlify:block_break` | Held | `destroy_time`: the block's destroy time |
| `controlify:use_item` | Held | `charge_progress`: 0 to 1 while a crossbow charges |
| `controlify:ui_navigate` | Once | |
| `controlify:ambient` | Held while in a world | See the table above |

## Your own events

Your mod needs its own event when rules can't describe the situation:

- **Something happens at an instant**, rather than being a state. The example's horn sounds when the player presses
  the horn button.
- **The feeling follows a number** your mod knows. Contextual state holds facts and slots, not numbers, so a number
  has to be an event's param. The example's engine follows the throttle.

### Declaring events

Declare each event once, in a static field. An event's params are a type of your own, with each `param` call
exposing one of its numbers by name, for effects to scale by:

<<< @/../../src/example/java/dev/isxander/controlify/example/feedback/BoatFeedback.java#events

Register your events in `FeedbackEvents.REGISTER`, subscribed from your [entrypoint](controlify-entrypoint):

<<< @/../../src/example/java/dev/isxander/controlify/example/feedback/BoatFeedback.java#register

An event plays the effect with its own ID, unless rules choose others. The horn event, `examplemod:horn`, plays
`assets/examplemod/controller/feedback/effect/horn.json`:

<<< @/../../src/example/resources/assets/examplemod/controller/feedback/effect/horn.json

### Firing and holding

Fire or hold an event where the thing happens. It plays on the player's current controller:

<<< @/../../src/example/java/dev/isxander/controlify/example/actions/BoatActions.java#feedback

- **`fire`** plays the effect once. Use it for something that happens at an instant and has no duration of its
  own, like an explosion or taking damage.
- **`hold`** keeps the effect going. Use it for something that lasts, like breaking a block, and call it every tick
  while it's happening. Each call updates the params. The effect ends shortly after the calls stop, or at once if you
  call `release()`.

`fireAt` and `holdAt` take the position where the thing happens. The effect then gets weaker with the player's
distance from it, reaching nothing at the event's falloff range: 32 blocks unless you set `falloffRange` on the
builder.

### Code effects

Write an effect in code only when the data format can't represent it. The example's engine is a tone whose pitch
follows the throttle, and a track's `hz` can't change:

<<< @/../../src/example/java/dev/isxander/controlify/example/feedback/BoatFeedback.java#code-effect

- An effect is stateless. Everything about the playback is in `Playback`: `seconds()`, `params()`, `param(name)`,
  `held()`, `released()`, `sinceRelease()`, and `crossed(seconds)`, which is true for exactly one frame.
- Effects are sampled every frame, so work in seconds, not ticks.
- Return `false` when the effect is over. An effect for a held event returns `false` once `released()` is true, or
  after a tail of its own.
- A code effect only plays for the event it's registered with. `registerEffect(id, event, effect)` registers one
  that rules can choose by its own ID.
- A data effect with the same ID always wins, so resource packs can still replace yours.

`FeedbackFrame` is what the effect writes to:

| Method | Output |
|---|---|
| `band(site, band, amp)` | A rumble band |
| `rumble(low, high)` | Both bands on the whole controller |
| `voice(site, hz, amp)` | A tone |
| `click(site, intensity, sharpness)` | A transient. Call it on one frame only, such as when `crossed` is true. |
| `light(rgb, alpha)` | A light layer, in sRGB |
| `hd()` | Whether the controller plays tones and clicks faithfully |

## Groups and ordering

`assets/controlify/controller/feedback/groups.json` puts effects into groups. Every group has a strength slider in
the controller settings, and groups can be ordered to decide which effects draw on top. The example puts its boat
effects in their own group, and its boat tint below the player's effects:

<<< @/../../src/example/resources/assets/controlify/controller/feedback/groups.json

Every mod's and pack's file is merged, from the lowest-priority pack to the highest. In both `groups` and
`ordering`, the key says what to do:

| Key | Effect |
|---|---|
| `"mypack:aiming"` | Adds to what lower packs said |
| `"=mypack:aiming"` | Replaces everything lower packs said about it |
| `"-mypack:aiming"` | Removes from it |

Within one file, replacements apply first, then removals, then additions. Mistakes are logged and skipped, and never
stop the pack from loading.

### Groups

- A new group gets its own slider, named by the language key `controlify.vibration_strength.<namespace>.<path>`,
  with a `.tooltip`.
- An effect can be in several groups. Its strength is the lowest of its groups' sliders, times the master strength.
- Groups list effects, not events. Add every effect you ship to the groups it belongs in, including ones only rules
  play.
- An effect in no group is in `controlify:ungrouped`, which has no slider, so only the master strength controls it.
  It can still be ordered.

### Ordering

Ordering decides which effect is on top when several play at once. It matters for light layers, and for HD haptics
when there are more tones than the controller can play, where the lowest are dropped. Vibration is unaffected.

`"a": { "above": ["b"] }` and `"b": { "below": ["a"] }` mean the same thing. When two effects play at once, the one
on top is:

1. the one whose groups rank higher, where a group's rank is how many groups are stacked below it;
2. then a fired event over a held one, and a held one over `controlify:ambient`;
3. then the newer one.

If two packs' orderings contradict each other, the higher-priority pack's is kept.
