---
title: Feedback API
---

# Feedback API

Feedback is everything the player feels through the controller. The API splits it in two:

- A `FeedbackEvent` says **when** something should be felt. Your code fires it.
- A `FeedbackEffect` says **what** it feels like. This is usually a
  [data file](../resource-packs/feedback-effects.md), so resource packs can change it.

The API is in `dev.isxander.controlify.api.feedback` and is marked experimental.

::: tip
If the situation you want feedback for is already described by contextual state, you may not need any code. A
[rule with no event](../resource-packs/feedback-effects.md#rules-with-no-event) plays an effect whenever a predicate is
true.
:::

## Declaring an event

Declare an event once and keep it in a static field. The params are a type of your own; each `param` call also exposes
one of its numbers by name for data-driven effects.

```java
public record Recoil(float force) {}

public static final FeedbackEvent<Recoil> RECOIL = FeedbackEvent.builder(Identifier.fromNamespaceAndPath("mymod", "recoil"))
        .param("force", Recoil::force)
        .build();

// an event with no params
public static final FeedbackEvent<Void> RELOAD = FeedbackEvent.builder(Identifier.fromNamespaceAndPath("mymod", "reload"))
        .build();
```

Register events in your [Controlify entrypoint](controlify-entrypoint.md) so they appear in diagnostics:

```java
FeedbackEvents.REGISTER.subscribeForever(registry -> {
    registry.registerEvent(RECOIL);
    registry.registerEvent(RELOAD);
});
```

## Firing and holding

Emit sites hold no state. Events play on the current controller.

```java
// one-shot
RECOIL.fire(new Recoil(0.8f));

// one-shot at a position: it gets weaker with distance on its own
RECOIL.fireAt(pos, new Recoil(0.8f));

// continuous: call every tick while the condition is true
if (charging) {
    CHARGE.hold(new Charge(progress));
}

// optional: end a hold at once, instead of when the calls stop
CHARGE.release();
```

A held event ends by itself about three ticks after you stop calling `hold`, so `release` is only for when it must
stop immediately. Each `hold` call refreshes the params.

With nothing else set up, an event plays the effect with its own id, so `mymod:recoil` plays
`assets/mymod/controller/feedback/recoil.json`. If no such effect exists, firing it does nothing.

## Effects in code

Write the effect as a [data file](../resource-packs/feedback-effects.md) where you can. Use code for an effect that
needs logic a data file cannot express.

A code effect receives your params with their real type:

```java
FeedbackEvents.REGISTER.subscribeForever(registry -> {
    registry.registerEffect(RECOIL, (playback, out) -> {
        float force = playback.params().force();
        float remaining = 1f - playback.seconds() / 0.2f;
        if (remaining <= 0f) {
            return false; // finished
        }
        out.rumble(force * remaining, force * 0.5f);
        return true;
    });
});
```

- An effect is stateless. Everything about the playback is in `Playback`: `seconds()`, `params()`, `held()`,
  `released()` and `sinceRelease()`.
- Effects are sampled once per rendered frame, so work in seconds, not ticks.
- Return `false` when the effect is over. An effect for a held event should return `false` once `released()` is true,
  or after a tail of its own.
- A data effect with the same id always wins over a code effect, so resource packs can still replace yours.

`registerEffect(event, effect)` registers the event's default effect. To register a variant that
[selection rules](../resource-packs/feedback-effects.md#rules-for-an-event) can choose, give it its own id:
`registerEffect(id, event, effect)`. Mark the event with `.selection()` when you build it so its rules are consulted.

### The output frame

`FeedbackFrame` describes what should be felt without naming any hardware.

| Method | Meaning |
|--------|---------|
| `band(site, band, amp)` | A rumble band (`LOW` or `HIGH`). Every controller can play this. |
| `rumble(low, high)` | Both bands on the whole controller |
| `voice(site, hz, amp)` | A tone at a frequency, for HD haptics |
| `click(site, intensity, sharpness)` | A single transient |
| `hd()` | Whether the controller reproduces voices and clicks faithfully |

Call `click` once, on the frame it should happen. `playback.crossed(seconds)` is true for exactly one frame:

```java
if (playback.crossed(0.1f)) {
    out.click(Site.BODY, 0.8f, 0.7f);
}
```

How each controller plays a frame:

| Controller | Bands | Voices | Clicks |
|------------|-------|--------|--------|
| Ordinary rumble | The two motors | Folded into the nearest band | A short burst |
| Trigger motors | Trigger sites only | Amplitude only | A short burst |
| DualSense (HD haptics on) | Synthesised tones | Synthesised at their frequency | A synthesised burst |
| Steam Controller (2026) | The rumble motors | A tone command | A tick or click command |
