---
title: Controlify Entrypoint
---

# Controlify Entrypoint

_Run your addon's setup at the right point in Controlify's startup._

Addons register things with Controlify by subscribing to its events. An event is never replayed to a listener that
subscribes after it fired. The Controlify entrypoint runs your code before Controlify fires any of its events, so
you can subscribe in time, whatever order the mod loader initialises mods in.

## Registering the entrypoint

Implement `ControlifyEntrypoint`, and subscribe to the events your addon needs:

<<< @/../../src/example/java/dev/isxander/controlify/example/ExampleControlifyEntrypoint.java#entrypoint

Then list the class. On Fabric, use a `controlify` entrypoint. On NeoForge, use a Java service file, which also
works on Fabric:

::: code-group
```json [fabric.mod.json]
{
  "entrypoints": {
    "controlify": [
      "com.example.mymod.MyControlifyEntrypoint"
    ]
  }
}
```

```txt [META-INF/services/dev.isxander.controlify.api.ControlifyEntrypoint]
com.example.mymod.MyControlifyEntrypoint
```
:::

A class listed both ways is only called once.

Controlify calls `onControlifyInitialize` once, on the client, during its own client initialisation. Minecraft
hasn't finished loading yet and Controlify's managers don't exist, so only subscribe to events here. If it
throws, Controlify logs the error and carries on starting up.

## Keeping Controlify optional

Controlify is the only thing that loads your entrypoint class, so it is never loaded when Controlify isn't
installed. If every class that uses Controlify's API is only reached from your entrypoint, your mod runs with or
without Controlify, and players don't need to install it.

The example addon is split this way: the mod itself never references Controlify, and everything that does is
reached from `ExampleControlifyEntrypoint`. See [Getting Started](getting-started#the-example-addon) for how it's laid
out, and for declaring Controlify as an optional dependency.

## Events

Every event is a static `EventSource` in one of the API's `*Events` classes. Subscribe with `subscribeForever`:

<<< @/../../src/example/java/dev/isxander/controlify/example/events/ControllerLogging.java#runtime-event

- Listeners run in the order they subscribed, on the thread that fires the event, before it returns.
- An event is never replayed to a listener that subscribes after it fired.
- If a listener throws, Controlify logs the error and the other listeners still run.
- `filter` and `via` return a new source that only passes on some events, or transforms them.

Don't assume an event fires only once, or at a particular time, unless its documentation says so. Controlify may
fire an event again, for example on a resource reload, and may move when it fires without changing the API.
Write listeners that are safe to run more than once.

### Registration events

Subscribe to these in your entrypoint. Each registry locks once its event has fired, so register in the listener
and nowhere else.

| Event | Register |
|---|---|
| `ActionEvents.REGISTER_LAYERS` | [Action layers](actions#layers) |
| `ActionEvents.REGISTER_ACTIONS` | [Actions](actions) |
| `ActionEvents.REGISTER_PROXIES` | [Key mapping proxies](actions#key-mappings) |
| `ContextualEvents.REGISTER_DOMAINS` | [Contextual](contextual-api) domains |
| `FeedbackEvents.REGISTER` | [Feedback](feedback-api) events and code effects |
| `LookEvents.REGISTER` | [Look](look-api) sources and scalers |
| `ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS` | [Screen behaviours](screen-operation-api) |
| `ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS` | [Element behaviours](screen-operation-api), for each screen as it opens |

The action events fire once, which is why you can keep the `ActionAccessor`s that `REGISTER_ACTIONS` returns in
fields.

### State events

These fire when Controlify's state changes:

| Event | Fires when |
|---|---|
| `ControlifyEvents.INITIALIZED` | Controlify's queries become usable. No controllers are connected yet. |
| `ControllerEvents.DISCOVERY_COMPLETED` | Controllers connected at startup have been found |
| `ControllerEvents.CONTROLLER_CONNECTED` | A controller connects. `hotplugged` is false for controllers found at startup. |
| `ControllerEvents.CONTROLLER_DISCONNECTED` | A controller disconnects |
| `ControllerEvents.CURRENT_CONTROLLER_CHANGED` | The player switches controller. The controller is null when there is none. |
| `ControlifyEvents.INPUT_MODE_CHANGED` | The input mode changes, after Controlify has applied it |

### Unsubscribing

`subscribe` returns a `Subscription`. Close it when the listener should stop, or the listener, and everything it
references, stays alive. A screen that shows connected controllers while it's open subscribes when it's added and
closes the subscription when it's removed:

<<< @/../../src/example/java/dev/isxander/controlify/example/events/ControllerStatusScreen.java#subscription

Listeners that last for the whole game, like everything subscribed in your entrypoint, use `subscribeForever`.

## Querying Controlify

`ControlifyApi.get()` returns Controlify's API for querying its current state. Call it whenever you need it; there's
no need to keep it. Its queries work from `ControlifyEvents.INITIALIZED` onward.

| Method | Returns |
|---|---|
| `getCurrentController()` | The controller the player is using, if any |
| `getConnectedControllers()` | Every connected controller |
| `getControllerByUid(uid)` | A connected controller by its unique ID |
| `currentInputMode()` | `KEYBOARD_MOUSE`, `CONTROLLER` or `MIXED` |
| `setInputMode(mode)` | Switches input mode. Controller modes need a current controller. |
