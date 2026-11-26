---
title: Migrating to v4
---

# Migrating to v4

_What changed in Controlify's API and resource formats, and what to use instead._

Controlify v4 replaces most of v3's API. This page maps every removed or renamed v3 type, method, file and field
to its v4 replacement. For how each v4 API works, follow the links to its page.

The biggest changes:

- The entrypoint has one method, where you subscribe to events and register everything.
- Bindings are now [actions](actions). How an action is triggered is data, and an action without an activator
  never fires.
- Screen and component processors are now [screen and element behaviours](screen-operation-api), registered by
  class.
- Rumble is now [feedback](feedback-api): events your code fires, and effects that are data files.
- Almost every resource file moved under `assets/<namespace>/controller/`.

## Depending on Controlify

Controlify is no longer published to Maven Central. v4 is on `https://maven.isxander.dev/releases`, and its
versions no longer have a loader suffix: one module holds every loader's jar, and you choose yours by capability.
See [Adding the dependency](getting-started#adding-the-dependency).

Update the version range you declare in your mod's manifest, so players can't run your mod with v3. See
[Keeping it optional](getting-started#keeping-it-optional).

## Entrypoint and events

The entrypoint now has one method, `onControlifyInitialize()`. In it you subscribe to events, and you register
everything from inside those event listeners. The old pre-init, init and discovery callbacks are gone. Events are now
`EventSource`s, split across `*Events` classes in each API package. `ControlifyApi` and its events return API types
(`Controller`, `dev.isxander.controlify.api.InputMode`) instead of internal ones.

### Registering the entrypoint

The Fabric entrypoint key is still `controlify`. The interface moved packages, so rename your ServiceLoader file:

| v3 | v4 |
|---|---|
| `META-INF/services/dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint` | `META-INF/services/dev.isxander.controlify.api.ControlifyEntrypoint` |

See [Registering the entrypoint](controlify-entrypoint#registering-the-entrypoint).

### Entrypoint

| v3 | v4 |
|---|---|
| `api.entrypoint.ControlifyEntrypoint` | `api.ControlifyEntrypoint` |
| `ControlifyEntrypoint#onControlifyPreInit(PreInitContext)` | `ControlifyEntrypoint#onControlifyInitialize()`. Only subscribe to events here. |
| `PreInitContext#bindings()` | Removed. Register in `ActionEvents.REGISTER_ACTIONS`. See [Actions](actions). |
| `PreInitContext#contextualDomains()` | Removed. Register in `ContextualEvents.REGISTER_DOMAINS`. See [Custom domains](contextual-api#custom-domains). |
| `ControlifyEntrypoint#onControlifyInit(InitContext)` | `ControlifyEvents.INITIALIZED`. It fires before controllers are discovered, so no controllers are connected yet. If you need them, use `ControllerEvents.DISCOVERY_COMPLETED`. |
| `InitContext#controlify()` | The `ControlifyApi` passed to `ControlifyEvents.INITIALIZED`, or `ControlifyApi.get()` |
| `ControlifyEntrypoint#onControllersDiscovered(ControlifyApi)` | `ControllerEvents.DISCOVERY_COMPLETED`, which passes the `ControlifyApi` |

The other registration events, for layers, key mapping proxies, feedback, look and screen behaviours, are listed in
[Registration events](controlify-entrypoint#registration-events).

Before:

```java
public class MyEntrypoint implements ControlifyEntrypoint {
    @Override
    public void onControlifyPreInit(PreInitContext ctx) {
        MyBindings.register(ctx.bindings());
        MyDomains.register(ctx.contextualDomains());
    }

    @Override
    public void onControlifyInit(InitContext ctx) {
        ControlifyEvents.CONTROLLER_CONNECTED.register(event -> onConnected(event.controller()));
    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {
        onDiscovered(controlify);
    }
}
```

After:

```java
import dev.isxander.controlify.api.ControlifyEntrypoint;
import dev.isxander.controlify.api.action.ActionEvents;
import dev.isxander.controlify.api.contextual.ContextualEvents;
import dev.isxander.controlify.api.controllermanager.ControllerEvents;

public class MyEntrypoint implements ControlifyEntrypoint {
    @Override
    public void onControlifyInitialize() {
        ActionEvents.REGISTER_ACTIONS.subscribeForever(registry -> {
            // register actions
        });
        ContextualEvents.REGISTER_DOMAINS.subscribeForever(registry -> {
            // register domains
        });
        ControllerEvents.CONTROLLER_CONNECTED.subscribeForever(event -> onConnected(event.controller()));
        ControllerEvents.DISCOVERY_COMPLETED.subscribeForever(controlify -> onDiscovered(controlify));
    }
}
```

Events are never replayed to late subscribers, so subscribe in the entrypoint, not in a later callback.

### Events

| v3 | v4 |
|---|---|
| `api.event.ControlifyEvents` | Split into `api.ControlifyEvents` and `api.controllermanager.ControllerEvents` |
| `dev.isxander.controlify.platform.EventHandler` | `api.utils.event.EventSource` |
| `EventHandler#register(Callback)` | `EventSource#subscribeForever(EventSink)`, or `subscribe(EventSink)`, which returns a `Subscription` to close. See [Unsubscribing](controlify-entrypoint#unsubscribing). |
| `EventHandler#invoke(T)` | Removed. You can't fire Controlify's events. |
| `EventHandler.Callback` | `api.utils.event.EventSink` |
| `ControlifyEvents.CONTROLLER_CONNECTED` | `ControllerEvents.CONTROLLER_CONNECTED` |
| `ControlifyEvents.ControllerConnected` | `ControllerEvents.ControllerConnected(controlify, controller, hotplugged)` |
| `ControllerConnected#newController()` | Removed, with no replacement |
| `ControlifyEvents.CONTROLLER_DISCONNECTED` | `ControllerEvents.CONTROLLER_DISCONNECTED`. Don't keep the controller after the listener returns. |
| `ControlifyEvents.ControllerDisconnected` | `ControllerEvents.ControllerDisconnected(controlify, controller)` |
| `ControlifyEvents.INPUT_MODE_CHANGED` | `api.ControlifyEvents.INPUT_MODE_CHANGED` |
| `ControlifyEvents.InputModeChanged` | `api.ControlifyEvents.InputModeChanged(controlify, mode)` |
| `ControlifyEvents.ACTIVE_CONTROLLER_TICKED` | Removed. Use your mod loader's client tick event and `ControlifyApi.get().getCurrentController()`. To react to the player switching controller, use `ControllerEvents.CURRENT_CONTROLLER_CHANGED`. |
| `ControlifyEvents.CONTROLLER_STATE_UPDATED` | Removed, as `ACTIVE_CONTROLLER_TICKED` |
| `ControlifyEvents.CONTROLLER_STATE_UPDATE` | Removed. Use your mod loader's client tick event and `ControlifyApi.get().getConnectedControllers()`. |
| `ControlifyEvents.ControllerStateUpdate` | Removed |
| `ControlifyEvents.VIRTUAL_MOUSE_TOGGLED` | Removed, with no event. Query `ScreenContext#isVirtualMouseEnabled()` instead. See [Virtual mouse](screen-operation-api#virtual-mouse). |
| `ControlifyEvents.VirtualMouseToggled` | Removed |
| `ControlifyEvents.LOOK_INPUT_MODIFIER` | `LookEvents.REGISTER`. Register a scaler or source. See [Look API](look-api). |

The full list of v4 events is in [Events](controlify-entrypoint#events).

Before:

```java
ControlifyEvents.ACTIVE_CONTROLLER_TICKED.register(event -> doSomething(event.controller()));
```

After, on Fabric:

```java
ClientTickEvents.END_CLIENT_TICK.register(minecraft ->
        ControlifyApi.get().getCurrentController().ifPresent(controller -> doSomething(controller)));
```

To read the player's input in a tick, read your actions' channels. See
[Reading its channels](actions#reading-its-channels).

### ControlifyApi

| v3 | v4 |
|---|---|
| `ControlifyApi#getCurrentController()`, returning `Optional<ControllerEntity>` | `ControlifyApi#getCurrentController()`, returning `Optional<? extends Controller>` |
| `ControlifyApi#currentInputMode()`, returning `dev.isxander.controlify.InputMode` | `ControlifyApi#currentInputMode()`, returning `dev.isxander.controlify.api.InputMode` |
| `ControlifyApi#setInputMode(dev.isxander.controlify.InputMode)` | `ControlifyApi#setInputMode(dev.isxander.controlify.api.InputMode)` |
| `dev.isxander.controlify.InputMode` | `dev.isxander.controlify.api.InputMode`. The constants and methods are the same. |
| `ControlifyApi#playRumbleEffect(RumbleSource, RumbleEffect)` | Removed. Declare a feedback event and `fire` it. See [Your own events](feedback-api#your-own-events). |
| `Controlify.instance()` | `ControlifyApi.get()`. `Controlify` is now internal, in `impl`. |
| `Controlify.instance().getControllerManager()` then `getConnectedControllers()` | `ControlifyApi#getConnectedControllers()` |
| `ControllerManager#isControllerConnected(uid)` | `ControlifyApi#getControllerByUid(uid)`, then `isPresent()` |

`ControlifyApi`'s queries work from `ControlifyEvents.INITIALIZED` onward. See
[Querying Controlify](controlify-entrypoint#querying-controlify).

### Controllers

`ControllerEntity` and its components were internal classes, with every component behind an `Optional`. v4 replaces
them with the `Controller` interface, which only exposes a few components. See [Controllers](controllers).

| v3 | v4 |
|---|---|
| `controller.ControllerEntity` | `api.controller.Controller` |
| `ControllerEntity#uid()` | `Controller#uid()` |
| `ControllerEntity#name()` | `Controller#name()`: the controller type's name, or the driver's if the type has none |
| `ControllerEntity#driverName()` | `Controller#driverReportedName()` |
| `ControllerEntity#info()` then `type().namespace()` | `Controller#typeId()` |
| `ControllerEntity#guid()` | Removed. Key data by `uid()`. |
| `ControllerEntity#info()`, `drivers()`, `update(boolean)`, `close()`, `getLogger()` | Removed |
| `ControllerEntity#settings()`, `defaultSettings()`, `setSettings(ProfileSettings)` | Removed. `Controller#guideVerbosity()` exposes the guide verbosity setting. |
| `ControllerEntity#getComponent(Identifier)` and the other `ECSEntity` methods | Removed |
| `ControllerEntity#input()`, returning `Optional<InputComponent>` | `Controller#input()`, returning `api.controller.input.InputComponent`. It's always present. |
| `InputComponent#getBinding(Identifier)`, returning `InputBinding` | `InputComponent#getAction(Identifier)`, returning `Action`. See [Input](controllers#input). |
| `InputComponent#getAllBindings()` | Removed |
| `InputComponent#isDefinitelyGamepad()` | `InputComponent#isGamepad()` |
| `InputComponent#stateNow()`, `stateThen()`, `rawStateNow()`, `rawStateThen()` | Removed. Read actions instead. See [Reading its channels](actions#reading-its-channels). |
| `InputComponent#buttonCount()`, `axisCount()`, `hatCount()`, `getDeadzoneGroups()`, `settings()` | Removed |
| `ControllerEntity#gyro()`, returning `Optional<GyroComponent>` | `Controller#gyro()`, returning `Optional<? extends api.controller.gyro.GyroComponent>` |
| `GyroComponent#getState()`, returning `GyroStateC` | `GyroComponent#getState()`, returning `Vector3fc`. It's now the rotation since the last frame, in radians, not a rate in radians per second. `pitch()`, `yaw()` and `roll()` are `x()`, `y()` and `z()`. For the controller's tilt, use the new `gravity()`. See [Gyro](controllers#gyro). |
| `GyroComponent#getCalibrator()`, `settings()` | Removed |
| `ControllerEntity#dualSense()` | `Controller#dualsense()`, returning `Optional<? extends api.controller.dualsense.DualsenseComponent>` |
| `DualsenseComponent#setMuteLight(boolean)` | `DualsenseComponent#setMicLightMode(MicLightMode)`. `MicLightMode.fromBoolean(on)` converts a boolean. See [DualSense](controllers#dualsense). |
| `DualsenseComponent#getMuteLight()` | Removed |
| `DualsenseComponent#setLeftTriggerEffect`, `setRightTriggerEffect` and their getters | Removed. Use adaptive trigger effects. See [Adaptive trigger effects](contextual-api#adaptive-trigger-effects). |
| `ControllerEntity#rumble()`, `triggerRumble()`, `hdHaptics()` | Removed. Use feedback events. See [Feedback](feedback-api). |
| `ControllerEntity#led()` | Removed. Use a feedback effect's light output. See [Light](feedback-api#light). |
| `ControllerEntity#touchpad()`, `batteryLevel()`, `bluetooth()` | Removed, with no replacement |

## Bindings to actions

Input bindings are now **actions**. You register an `ActionSpec` in `ActionEvents.REGISTER_ACTIONS`, keep the
`ActionAccessor` it returns, and read the action through three channels: pulse, latch and continuous. A v3
`justPressed()` check becomes `pulse().consumeAllPulses()` (or `consumePulse()` in a `while` loop). A v3
held check (`digitalNow()`) becomes `latch().isLatchActive()`, and `analogueNow()` becomes
`continuous().getContinuous()`.

How an action is triggered is now data. Its [activator](actions#activators), in
`assets/controlify/controller/activators.json`, says which gesture fires it and which channels it drives. The
v3 builder had no equivalent. An action without an activator can still be bound, but it never fires. The one
exception is an action that proxies a key mapping, which gets an editable `hold` if you don't give it an
activator.

See [Actions](actions) for the full v4 API.

### Code

Everything below was in `dev.isxander.controlify.api.bind` unless a package is given. Classes from
`dev.isxander.controlify.bindings` weren't API, but the v3 API exposed them, so they're listed too.

| v3 | v4 |
|---|---|
| `ControlifyBindApi`, `ControlifyBindApi.get()`, `PreInitContext.bindings()` | `ActionEvents`. Subscribe from `ControlifyEntrypoint.onControlifyInitialize()`. See [Registering it](actions#registering-it). |
| `ControlifyBindApi.registerBinding(callback)` | `ActionEvents.REGISTER_ACTIONS.subscribeForever(registry -> registry.register(ActionSpec.builder()…build()))`. Registration closes after this event, so you can't register later. |
| `ControlifyBindApi.registerBinding(callback, filter)` | `ActionSpecBuilder.shouldExpose(Predicate<Controller>)`. The action is still created on every controller. It's only hidden from the controls settings. |
| `ControlifyBindApi.RegistryCallback` | Removed. Build an `ActionSpec` with `ActionSpec.builder()`. |
| `ControlifyBindApi.createSupplier(Identifier)` | `controller.input().getAction(Identifier)`, which returns the `Action` or null. There's no public way to make an `ActionAccessor` from an ID. |
| `ControlifyBindApi.getKeyCorrelation(KeyMapping)` | Removed. No replacement. |
| `ControlifyBindApi.registerBindContext(BindContext)` | `ActionEvents.REGISTER_LAYERS`, then `layers.layer(id).activeWhen(…).above(…).register()`. See [Layers](actions#layers). |
| `ControlifyBindApi.getAllBindIds()` | Removed. No replacement. |
| `InputBindingSupplier` | `ActionAccessor` |
| `InputBindingSupplier.on(ControllerEntity)` | `ActionAccessor.on(Controller)` or `on(InputComponent)` |
| `InputBindingSupplier.onOrNull(ControllerEntity)` | `ActionAccessor.onOrNull(Controller)`, or `onOptional(Controller)` |
| `InputBindingSupplier.bindId()` | `ActionAccessor.actionId()` |
| `InputBindingSupplier.inputGlyph()` | `ActionGlyphs.action(accessor)` |
| `InputBindingSupplier.CODEC` | Removed. Store the action's `Identifier` with `Identifier.CODEC`, then look it up with `controller.input().getAction(id)`. |
| `InputBindingBuilder` | `ActionSpecBuilder`, from `ActionSpec.builder()`. Call `build()` and pass the result to `ActionSpecRegistry.register`. |
| `InputBindingBuilder.id(Identifier)` | `ActionSpecBuilder.id(Identifier)` |
| `InputBindingBuilder.id(String, String)` | Removed. Use `id(Identifier.fromNamespaceAndPath(namespace, path))`. |
| `InputBindingBuilder.name`, `description`, `category` | Same methods on `ActionSpecBuilder`. The default language keys changed. See [Language keys](#language-keys). |
| `InputBindingBuilder.allowedContexts(BindContext...)` | `ActionSpecBuilder.layer(Identifier...)`. You need at least one layer. |
| `InputBindingBuilder.addKeyCorrelation(KeyMapping)` | `ActionProxyManager.dontAutoProxyKeyMapping(KeyMapping)`, in `ActionEvents.REGISTER_PROXIES`. See [Key mappings](actions#key-mappings). |
| `InputBindingBuilder.keyEmulation(KeyMapping)` | `ActionProxyManager.proxyKeyMapping(accessor, keyMapping)`, in `ActionEvents.REGISTER_PROXIES`. The proxy consumes the action's pulses, so don't read them yourself. |
| `InputBindingBuilder.keyEmulation(KeyMapping, Function<ControllerEntity, Boolean>)` | `ActionProxyManager.proxyKeyMapping(accessor, keyMapping, Predicate<Controller>)` |
| `InputBindingBuilder.defaultInput(Input)` | Removed. Put defaults in `controller/default_bind/default.json`. See [Default bindings](actions#default-bindings). |
| `InputBindingBuilder.radialCandidate(...)` | Removed. Give the action an icon in `controller/radial_icons.json`. See [Radial menu icon](actions#radial-menu-icon). |
| `InputBinding` | `Action` |
| `InputBinding.id()`, `name()`, `description()`, `category()` | `action.spec().id()`, `.name()`, `.description()`, `.category()` |
| `InputBinding.contexts()` | `action.spec().layers()`, a `Set<Identifier>` |
| `InputBinding.inputGlyph()`, `inputIcon()` | `ActionGlyphs.action(accessor)`. The glyph is resolved when it's displayed, using that client's selected controller. There's no glyph for one fixed controller. See [Showing its input](actions#showing-its-input). |
| `InputBinding.isRadialCandidate()` | Removed. No public replacement. |
| `InputBinding.boundInput()` | `Action.bindings()`, or `Action.primaryInput()` for the first binding's input |
| `InputBinding.setBoundInput(Input)` | `Action.setBindings(List<ActionBinding>)` |
| `InputBinding.defaultInput()` | `Action.defaultBindings()` |
| `InputBinding.isUnbound()` | `action.bindings().isEmpty()` |
| `InputBinding.analogueNow()` | `action.continuous().getContinuous()`. Needs a `continuous` activator. |
| `InputBinding.digitalNow()` | `action.latch().isLatchActive()`. Needs an activator that drives the latch, like `span`. |
| `InputBinding.justPressed()` | `action.pulse().consumeAllPulses()`. Needs a `pulse` activator with the `press` gesture, or a `span`, which pulses once when it starts. |
| `InputBinding.justReleased()` | `action.pulse().consumeAllPulses()`. Needs a `pulse` activator with the `release` gesture. |
| `InputBinding.justTapped()` | `action.pulse().consumeAllPulses()`. Needs a `pulse` activator with the `tap` gesture. |
| `InputBinding.guiPressed()`, `bindings.output.GuiPressOutput` | `action.pulse().consumeAllPulses()`. Needs a `pulse` activator with the `gui_press` gesture. |
| `InputBinding.analoguePrev()`, `digitalPrev()` | Removed. Keep the previous value yourself if you need it. |
| `InputBinding.createStateAccess(...)`, `returnStateAccess(...)`, `bindings.StateAccess` | Removed. Read the channels instead. |
| `InputBinding.getDigitalOutput`, `addDigitalOutput`, `getAnalogueOutput`, `addAnalogueOutput`, the `bindings.output` classes, and the constants `ANALOGUE_NOW` to `KEY_EMULATION` | Removed. Describe the behaviour with an activator. To combine channels, use a [list of activators](actions#combining-activators). |
| `InputBinding.fakePress()` | Removed. No public replacement. |
| `bindings.input.Input` | `ActionBinding`, which holds a `LogicalInput` and an optional `Gesture` |
| `bindings.input.ButtonInput`, `AxisInput` | `LogicalInput.Single(Identifier)` |
| `bindings.input.HatInput` | `LogicalInput.Single`, with a direction in the ID, like `controlify:hat/0/up` |
| `bindings.input.EmptyInput` | An empty bindings list |
| `RadialIcon` | Removed from the API. Icons are data only, in `controller/radial_icons.json`. |
| `bindings.BindContext` | A layer `Identifier`. The built-in ones are in `BuiltinActionLayers`. |
| `BindContext.IN_GAME`, `ANY_SCREEN`, `REGULAR_SCREEN`, `CONTAINER`, `V_MOUSE_CURSOR`, `RADIAL_MENU` | The constants with the same names in `BuiltinActionLayers`. The IDs haven't changed. See [Built-in layers](actions#built-in-layers). |
| `BindContext.UNKNOWN`, `V_MOUSE_COMPAT`, `BindContext.CONTEXTS` | Removed. Built-in virtual mouse actions now use `V_MOUSE_CURSOR`. |
| `font.BindingFontHelper.binding(...)` | `ActionGlyphs.action(...)` |
| `font.BindingFontHelper.bindingWithFallback(...)` | `ActionGlyphs.actionWithFallback(...)` |
| `font.BindingFontHelper.bindingWithActiveFallback(...)` | `ActionGlyphs.actionWithActiveFallback(...)` |
| `bindings.ControlifyBindings` | `api.action.BuiltinActions` for the GUI actions, which are API-safe. The rest are in `impl.action.ControlifyActions`, which isn't API. The fields are `ActionAccessor`s with the same names. |
| `ControlifyBindings.CYCLE_OPT_FORWARD`, `CYCLE_OPT_BACKWARD` | `GUI_SECONDARY_NAVI_RIGHT`, `GUI_SECONDARY_NAVI_LEFT` |

Other APIs that took an `InputBindingSupplier`, such as `ButtonGuideApi.addGuideToButton`, now take an
`ActionAccessor`.

#### Registering and reading an action

Before (v3):

```java
private InputBindingSupplier openBackpack;

@Override
public void onControlifyPreInit(PreInitContext context) {
    openBackpack = context.bindings().registerBinding(builder -> builder
            .id("mymod", "open_backpack")
            .category(Component.translatable("mymod.category"))
            .allowedContexts(BindContext.IN_GAME)
            .addKeyCorrelation(MyMod.OPEN_BACKPACK_KEY));
}

// every tick
ControlifyApi.get().getCurrentController().ifPresent(controller -> {
    if (openBackpack.on(controller).justPressed()) {
        MyMod.openBackpack();
    }
});
```

After (v4):

```java
public static ActionAccessor openBackpack;

@Override
public void onControlifyInitialize() {
    ActionEvents.REGISTER_ACTIONS.subscribeForever(registry -> {
        openBackpack = registry.register(ActionSpec.builder()
                .id(Identifier.fromNamespaceAndPath("mymod", "open_backpack"))
                .category(Component.translatable("mymod.category"))
                .layer(BuiltinActionLayers.IN_GAME)
                .build());
    });
    ActionEvents.REGISTER_PROXIES.subscribeForever(proxies ->
            proxies.dontAutoProxyKeyMapping(MyMod.OPEN_BACKPACK_KEY));
}

// every tick
ControlifyApi.get().getCurrentController().ifPresent(controller -> {
    if (openBackpack.on(controller).pulse().consumeAllPulses()) {
        MyMod.openBackpack();
    }
});
```

Then give the action an activator in `assets/controlify/controller/activators.json`. Without one, it never
fires. `press` matches v3's `justPressed()`. `tap` is usually better, because players can share the button with
a long press:

```json
{
  "mymod:open_backpack": { "type": "pulse", "gesture": "press", "editable": true }
}
```

If you read both `justPressed()` and `digitalNow()` on one v3 binding, use `"span"`. It holds the latch and
pulses once when it starts.

#### Language keys

The default name and description keys changed:

| v3 | v4 |
|---|---|
| `controlify.binding.<namespace>.<path>` | `controlify.action.<namespace>.<path>` |
| `controlify.binding.<namespace>.<path>.desc` | `controlify.action.<namespace>.<path>.desc` |
| `controlify.binding.<controller type>.<namespace>.<path>`, a name for one controller type | Removed. Actions have one name for every controller. |

### Data files

Controlify only reads files at the new paths. It migrates players' saved bindings, but it doesn't convert your
mod's or resource pack's files.

| v3 | v4 |
|---|---|
| `assets/<namespace>/controllers/default_bind/<path>.json` | `assets/<namespace>/controller/default_bind/<path>.json` |
| `assets/controlify/radial_icons.json` | `assets/controlify/controller/radial_icons.json` |
| None | `assets/controlify/controller/activators.json`. This is new, and your actions need it. See [Activators](actions#activators). |

#### Default bindings

The `"defaults"` object and `"clear_below"` are unchanged. Each action now takes a **list** of bindings instead
of a single input object:

| v3 value | v4 value |
|---|---|
| `{ "button": "controlify:button/south" }` | `["controlify:button/south"]` |
| `{ "axis": "controlify:axis/left_stick_up" }` | `["controlify:axis/left_stick_up"]` |
| `{ "hat": "controlify:hat/0", "target_state": "up" }` | `["controlify:hat/0/up"]` |
| `{ "hat": "controlify:hat/0", "target_state": "right_up" }` | `[{ "modifiers": ["controlify:hat/0/up"], "trigger": "controlify:hat/0/right" }]` |
| `{ "type": "empty" }` | `[]` |

A binding can also be a chord or carry a gesture. See [Default bindings](actions#default-bindings).

#### Radial icons

The icon format is unchanged, so you only need to move the file. v4 adds an optional `overlay` text component,
and `model` can also be `{ "id": "...", "enchant_glint": true }`. See [Radial menu icon](actions#radial-menu-icon).

### Built-in IDs

No built-in binding was renamed or removed. All 96 v3 IDs in `ControlifyBindings` exist with the same IDs as
actions in v4, so resource packs and code that use them by ID keep working. See
[Built-in Actions](../reference/builtin-actions).

The only removed names are the deprecated Java aliases `CYCLE_OPT_FORWARD` and `CYCLE_OPT_BACKWARD` (see the
table above). Their IDs were never separate.

`controlify:open_chat` no longer drives `options.keyChat`, as it did in v3. In v4, only `controlify:sprint`,
`controlify:attack` and `controlify:use` proxy key mappings, so their pulses aren't available to read.

## Screens

Screen processors and component processors are now [screen behaviours](screen-operation-api#screen-behaviours) and
[element behaviours](screen-operation-api#element-behaviours). You attach them by registering a factory against a
class, in `ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS` and `ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS`. You don't
need a mixin, so this works for vanilla classes and other mods' classes too.

A few things work differently:

- **Behaviours compose.** In v3, one processor replaced the default one. In v4, every matching registration runs in
  a [chain](screen-operation-api#chains), together with Controlify's built-in behaviours. Call `next.run()` where you
  used to call `super`. Leave it out where you used to return `true` from an `override...` method.
- **Registrations match subclasses.** A v3 component registration only matched the exact class. A v4 registration
  matches the class, its subclasses, and interfaces it implements.
- **Behaviours get a `ScreenContext`.** It replaces the `screen` field and the static helpers. It gives you the
  screen, its focus, sounds, button guides, the on-screen keyboard and the screen's virtual mouse.
- **Input arrives as action pulses.** Use `BuiltinActions.GUI_*` with `pulse().consumePulse()` or
  `consumeAllPulses()`, instead of `ControlifyBindings.GUI_*` with `justPressed()` or `guiPressed().get()`.
  Holding a direction repeats its pulses, so you don't need a hold-repeat helper.
- **Screen input runs whether or not the virtual mouse is on.** v3 only called `handleButtons` with the virtual mouse
  off. Check `context.isVirtualMouseEnabled()`, or wrap your behaviour in `ScreenBehaviours.conditional`.

Register in your [entrypoint](controlify-entrypoint#registration-events), not in pre-init.

### Screen processors

| v3 | v4 |
|---|---|
| `dev.isxander.controlify.screenop.ScreenProcessor<T>` (extend it) | `dev.isxander.controlify.api.screenop.ScreenBehaviour` (implement it). Take a `ScreenContext<? extends T>` in the constructor. |
| `ScreenProcessor.screen` | `context.screen()` |
| `ScreenProcessorProvider.registerProvider(cls, factory)`, `ScreenProcessorFactory.registerProvider(cls, factory)` | `ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribeForever(registry -> registry.<T>register(cls, factory))`. Pass a priority as the second argument if you need one. |
| `ScreenProcessorFactory.Factory<T>` | `ScreenBehaviourFactory<T>`. It receives the `ScreenContext`, not the screen. |
| Implementing `ScreenProcessorProvider` with a mixin (`screenProcessor()`) | Removed. Register by class instead. |
| `ScreenProcessorProvider.provide(screen)`, `ScreenProcessorFactory.createForScreen(screen)` | Removed. There is no public lookup of a screen's behaviours. |
| Extending a built-in processor, like `AbstractContainerScreenProcessor` | Register your own behaviour for your class. Controlify's built-in behaviour for the superclass still runs, because registrations match subclasses and compose. |
| `onControllerUpdate(controller)` | `handleNavigation(controller, next)`, then `handleInteraction(controller, next)` |
| `handleButtons(controller)` | `handleInteraction(controller, next)`. Call `next.run()` to keep pressing, back and clicking. |
| `handleScreenVMouse(controller, vmouse)` (documented as `handleVMouse`) | `handleInteraction` or `handleNavigation`, checking `context.isVirtualMouseEnabled()`. Or wrap the behaviour in `ScreenBehaviours.conditional(context::isVirtualMouseEnabled, ...)`. |
| `handleComponentNavigation(controller)`, `createScreenNavigationFunc(direction)` | `handleNavigation(controller, next)`. Consume the `BuiltinActions.GUI_NAVI_*` pulses yourself to replace the built-in focus navigation. |
| `handleComponentNavOverride`, `handleComponentButtonOverride` | Removed. Controlify passes input to the focused elements' behaviours itself. |
| `handleTabNavigation(controller)` | `handleNavigation(controller, next)`. Built-in tab switching runs after your behaviour; consume `BuiltinActions.GUI_NEXT_TAB` and `GUI_PREV_TAB` pulses to replace it. |
| `onTabChanged(controller)` | Removed. Handle the tab actions yourself if you need to know. |
| `onWidgetRebuild()` | `onWidgetRebuild(next)` |
| `render(controller, graphics, tickDelta, vmouse)`, `render(controller, graphics, tickDelta)` | `extractRenderState(graphics, tickDelta, controller, next)`. Get the virtual mouse from `context.virtualMouse()`. |
| `virtualMouseBehaviour()` | Call `context.virtualMouse().setMode(...)` in your behaviour's factory or constructor. See [virtual mouse](screen-operation-api#virtual-mouse). |
| `onVirtualMouseToggled(enabled)` | Removed. Controlify clears and restores focus itself. Check `context.isVirtualMouseEnabled()` when you need to. |
| `onInputModeChanged(mode)` | Removed. Read `context.inputMode()`. Controlify manages focus when the mode changes. |
| `tryOpenKeyboard(controller, element)` | `context.tryOpenKeyboard(controller, element)` |
| `getFocusTree()` | `context.focusPath()`, deepest element first |
| `CustomFocus.getCustomFocus()` | Removed. The focus path follows `ContainerEventHandler.getFocused()`. |
| `getWidget(Component)`, `getWidget(String)` | Removed. Keep a reference to the widget, or search `context.screen().children()`. |
| `holdRepeatHelper`, `createHoldRepeatHelper()` | Removed. Navigation actions repeat their pulses while held. |
| `playClackSound()` | `context.playClickSound()` |
| `playFocusChangeSound()` | `context.playFocusChangeSound()`. For a navigation step, `context.notifyNavigation(controller)` plays the sound (if the player turned on extra UI sounds) and the haptic. |
| `ScreenControllerEventListener.onControllerInput(controller)`, `addEventListener(listener)` | Removed. Register a screen behaviour and use `handleNavigation` or `handleInteraction`. |
| (new) `update(delta, next)`, `collectSnapPoints(output, next)`, `onScreenActiveChanged(active)` | See [screen behaviours](screen-operation-api#screen-behaviours). |

### Component processors

| v3 | v4 |
|---|---|
| `dev.isxander.controlify.screenop.ComponentProcessor` | `dev.isxander.controlify.api.screenop.ElementBehaviour` |
| `ComponentProcessorProvider.REGISTRY.register(cls, ctor)` | `ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribeForever(event -> event.registry().register(cls, factory))`. The event fires for each screen, with `event.screenContext()`. |
| `Registry<T, U>` | `ScreenBehaviourRegistry`, `ElementBehaviourRegistry` |
| Implementing `ComponentProcessorProvider` with a mixin (`componentProcessor()`) | Removed. Register by class instead. |
| `ComponentProcessorProvider.provide(component)` | `context.elementBehaviour(element)` |
| `ComponentProcessor.EMPTY` | `ElementBehaviours.chain()`, a behaviour that passes everything on |
| `overrideControllerNavigation(screen, controller)` | `handleNavigation(screen, controller, next)`. Returning `false` becomes `next.run()`; returning `true` becomes leaving it out. |
| `overrideControllerButtons(screen, controller)` | `handleInteraction(screen, controller, next)`, the same way |
| `onFocusGained(screen, controller)` | `onFocusChanged(screen, focused)`. It's also called when focus is lost, and has no controller. |
| `shouldKeepFocusOnKeyboardMode(screen)` | Removed. Controlify keeps focus on an `EditBox` when the player switches to keyboard and mouse, and clears it otherwise. |
| `getKeyboardBehaviour(screen, controller)` | `keyboardBehaviour(screen)`, returning an `Optional<ElementKeyboardBehaviour>` |
| `AbstractButtonComponentProcessor` | `ElementBehaviours.button(press)` |
| `AbstractSliderComponentProcessor.incrementSlider(reverse)` | `ElementBehaviours.slider(adjust)`, which calls `adjust` with `-1` or `+1`. See [using a built-in behaviour](screen-operation-api#using-a-built-in-behaviour). |

### Virtual mouse and snapping

| v3 | v4 |
|---|---|
| `dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour` | `dev.isxander.controlify.api.screenop.VirtualMouseMode`, with the same constants |
| `VirtualMouseBehaviour.hasCursor()` | Removed. Compare with `VirtualMouseMode.DISABLED`. |
| `VirtualMouseBehaviour.isDefaultOr(mode)` | Removed. Use `emulatesClicks()` or `scrolls()`. |
| `VirtualMouseHandler` (`Controlify.instance().virtualMouseHandler()`) | `context.virtualMouse()`, a `VirtualMouse` for that screen |
| `isVirtualMouseEnabled()`, `requiresVirtualMouse()` | `context.isVirtualMouseEnabled()`, or `virtualMouse().isEnabled()` |
| `getCurrentX`, `getCurrentY`, `snapToClosestPoint`, `snapInDirection`, `preventScrollingThisTick` | The same methods on `VirtualMouse` |
| `enableVirtualMouse()`, `disableVirtualMouse()`, `toggleVirtualMouse()` | Removed. Set a mode with `virtualMouse().setMode(...)`. The toggle belongs to the player, on `DEFAULT` screens. |
| Clicking with the cursor in a `CURSOR_ONLY` screen | `virtualMouse().emulateClicks(controller)` |
| `ControlifyEvents.VIRTUAL_MOUSE_TOGGLED`, `VirtualMouseToggled` | Removed. Check `context.isVirtualMouseEnabled()`. |
| `dev.isxander.controlify.api.vmousesnapping.SnapPoint` | `dev.isxander.controlify.api.screenop.SnapPoint`, with the same constructors |
| `ISnapBehaviour.controlify$collectSnapPoints(consumer)`, `getSnapPoints()` (implemented on a screen or widget) | `collectSnapPoints(output, next)` on a screen behaviour, or `collectSnapPoints(screen, output, next)` on an element behaviour. Every widget already snaps at its centre. |

### Button guides

| v3 | v4 |
|---|---|
| `dev.isxander.controlify.api.buttonguide.ButtonGuideApi` | `dev.isxander.controlify.api.gui.ButtonGuideApi` |
| `ButtonGuideApi.addGuideToButton(button, binding, predicate)` (static) | `context.buttonGuide().addGuideToButton(button, action, predicate)`, from a screen behaviour's `onWidgetRebuild`. See [button guides](screen-operation-api#button-guides). |
| `InputBindingSupplier` and `Supplier<InputBindingSupplier>` arguments, like `ControlifyBindings.GUI_ABSTRACT_ACTION_1` | `ActionAccessor` and `Supplier<ActionAccessor>`, like `BuiltinActions.GUI_ABSTRACT_ACTION_1` |
| `dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate` | `dev.isxander.controlify.api.gui.ButtonGuidePredicate` |
| `ButtonGuidePredicate.ALWAYS`, `ButtonGuidePredicate.FOCUS_ONLY` | `ButtonGuidePredicate.always()`, `ButtonGuidePredicate.focusOnly()` |
| `ButtonRenderPredicate` (the name the v3 docs used) | `ButtonGuidePredicate` |

### On-screen keyboard

| v3 | v4 |
|---|---|
| `dev.isxander.controlify.screenop.keyboard.ComponentKeyboardBehaviour` | `dev.isxander.controlify.api.screenop.ElementKeyboardBehaviour`. See [text input](screen-operation-api#text-input). |
| `ComponentKeyboardBehaviour.UNDEFINED`, `Undefined` | `Optional.empty()` |
| `ComponentKeyboardBehaviour.DoNothing` | `ElementKeyboardBehaviour.DoNothing` |
| `ComponentKeyboardBehaviour.Handled(KeyboardLayoutWithId, InputTarget, KeyboardPositioner)` | `ElementKeyboardBehaviour.Handled(Identifier, InputTarget, KeyboardPositioner)`. Pass the layout's identifier. |
| `dev.isxander.controlify.screenop.keyboard.InputTarget` | `dev.isxander.controlify.api.gui.keyboard.InputTarget`, with the same methods |
| `InputTarget.Delegated` | Removed. Write your own class that forwards to another `InputTarget`. |
| `MixinInputTarget` | Removed. Implement `InputTarget` on a separate class instead of on a mixin. |
| `dev.isxander.controlify.screenop.keyboard.KeyboardLayouts.FULL`, `SIMPLE`, `SERVER_IP` | `dev.isxander.controlify.api.gui.keyboard.KeyboardLayouts.FULL`, `SIMPLE`, `SERVER_IP` |
| `KeyboardLayouts.full()`, `simple()`, `serverIp()`, `fallback()` | Removed. Pass the identifier; Controlify loads the layout and falls back if it's missing. |
| `KeyboardLayoutWithId`, `KeyboardLayout`, `Controlify.instance().keyboardLayoutManager()` | Removed from the API. Use layout identifiers. |
| `KeyboardOverlayScreen.KeyboardPositioner` | `dev.isxander.controlify.api.gui.keyboard.KeyboardPositioner` |
| `KeyboardOverlayScreen.aboveOrBelowWidgetPositioner(width, height, padding, rect)` | `KeyboardPositioner.aboveOrBelowWidgetPositioner(width, height, padding, rect)` |

### Migrating a screen processor

Before, in v3:

```java
public class MyScreenProcessor extends ScreenProcessor<MyScreen> {
    public MyScreenProcessor(MyScreen screen) {
        super(screen);
    }

    @Override
    protected void handleButtons(ControllerEntity controller) {
        super.handleButtons(controller);
        if (ControlifyBindings.GUI_ABSTRACT_ACTION_1.on(controller).guiPressed().get()) {
            this.screen.reset();
            playClackSound();
        }
    }

    @Override
    public void onWidgetRebuild() {
        super.onWidgetRebuild();
        ButtonGuideApi.addGuideToButton(
                this.screen.resetButton,
                ControlifyBindings.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.always());
    }

    @Override
    public VirtualMouseBehaviour virtualMouseBehaviour() {
        return VirtualMouseBehaviour.DISABLED;
    }
}

// in onControlifyPreInit
ScreenProcessorProvider.registerProvider(MyScreen.class, MyScreenProcessor::new);
```

After, in v4:

```java
import dev.isxander.controlify.api.action.BuiltinActions;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.ScreenBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;
import dev.isxander.controlify.api.screenop.VirtualMouseMode;

public class MyScreenBehaviour implements ScreenBehaviour {
    private final ScreenContext<? extends MyScreen> context;

    public MyScreenBehaviour(ScreenContext<? extends MyScreen> context) {
        this.context = context;
        context.virtualMouse().setMode(VirtualMouseMode.DISABLED);
    }

    @Override
    public void handleInteraction(Controller controller, Runnable next) {
        if (BuiltinActions.GUI_ABSTRACT_ACTION_1.on(controller).pulse().consumeAllPulses()) {
            this.context.screen().reset();
            this.context.playClickSound();
        }
        next.run();
    }

    @Override
    public void onWidgetRebuild(Runnable next) {
        this.context.buttonGuide().addGuideToButton(
                this.context.screen().resetButton,
                BuiltinActions.GUI_ABSTRACT_ACTION_1,
                ButtonGuidePredicate.always());
        next.run();
    }
}

// in your entrypoint
ScreenopEvents.REGISTER_SCREEN_BEHAVIOURS.subscribeForever(registry ->
        registry.<MyScreen>register(MyScreen.class, MyScreenBehaviour::new));
```

The explicit `<MyScreen>` is needed because Java can't infer the type from a constructor reference.

### Migrating a component processor

Before, in v3:

```java
public record MySliderProcessor(MySlider slider) implements ComponentProcessor {
    @Override
    public boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerEntity controller) {
        if (ControlifyBindings.GUI_NAVI_LEFT.on(controller).justPressed()) {
            this.slider.nudge(-1);
            return true;
        }
        if (ControlifyBindings.GUI_NAVI_RIGHT.on(controller).justPressed()) {
            this.slider.nudge(1);
            return true;
        }
        return false;
    }

    @Override
    public void onFocusGained(ScreenProcessor<?> screen, ControllerEntity controller) {
        this.slider.stopEditing();
    }
}

// in onControlifyPreInit
ComponentProcessorProvider.REGISTRY.register(MySlider.class, MySliderProcessor::new);
```

After, in v4:

```java
import dev.isxander.controlify.api.action.BuiltinActions;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.screenop.ElementBehaviour;
import dev.isxander.controlify.api.screenop.ScreenContext;

public record MySliderBehaviour(MySlider slider) implements ElementBehaviour {
    @Override
    public void handleNavigation(ScreenContext<?> screen, Controller controller, Runnable next) {
        if (BuiltinActions.GUI_NAVI_LEFT.on(controller).pulse().consumePulse()) {
            this.slider.nudge(-1);
            screen.playNavigationHaptic(controller);
            return; // stops the chain, like returning true
        }
        if (BuiltinActions.GUI_NAVI_RIGHT.on(controller).pulse().consumePulse()) {
            this.slider.nudge(1);
            screen.playNavigationHaptic(controller);
            return;
        }
        next.run(); // like returning false
    }

    @Override
    public void onFocusChanged(ScreenContext<?> screen, boolean focused) {
        if (focused) {
            this.slider.stopEditing();
        }
    }
}

// in your entrypoint
ScreenopEvents.REGISTER_ELEMENT_BEHAVIOURS.subscribeForever(event ->
        event.registry().register(MySlider.class, MySliderBehaviour::new));
```

For a slider, you can usually drop the class and use the built-in behaviour instead. It handles editing, haptics and
the secondary navigation actions the same way as every other slider:

```java
event.registry().register(MySlider.class,
        ElementBehaviours.enabledWidget(ElementBehaviours.slider(MySlider::nudge)));
```

`enabledWidget` needs `MySlider` to extend `AbstractWidget`. See
[using a built-in behaviour](screen-operation-api#using-a-built-in-behaviour) for the other factories.

## Contextual state

The contextual API keeps its shape: domains, contributors and the sink work as before. What changed is where you register, the controller type in every context, and the removal of the trigger effect and guide location types. See [Contextual API](contextual-api) for the full v4 API.

| v3 | v4 |
|---|---|
| `PreInitContext#contextualDomains()` | Subscribe to `ContextualEvents.REGISTER_DOMAINS`. It passes you the `ContextualDomainRegistry`. |
| `ContextualDomainRegistry#inGame()` | `BuiltinContextualDomains.IN_GAME` |
| `ContextualDomainRegistry#container()` | `BuiltinContextualDomains.CONTAINER` |
| `ContextualDomainRegistry#register(id)`, `register(id, contributor)` | Unchanged. Only call them inside `REGISTER_DOMAINS`. Afterwards they throw. |
| `Context#controller()` returns `ControllerEntity` | Returns `dev.isxander.controlify.api.controller.Controller`. See [Controllers](controllers). |
| `InGameContext#controller()`, `ContainerContext#controller()` return `ControllerEntity` | Return `Controller` |
| `ContainerContext(player, hoveredSlot, holdingItem, cursorOutsideContainer, controller, verbosity)` | `ContainerContext(player, hoveredSlot, holdingItem, cursorOutsideContainer, controller)`. `verbosity()` now comes from `controller.guideVerbosity()`. |
| `new InGameContext(...)` | The constructor is internal. Use `InGameContext.create(minecraft, controller)`, which is now public. |
| Your own `Context#verbosity()` read from controller settings | Return `controller().guideVerbosity()`. See [Custom domains](contextual-api#custom-domains). |
| `ContextualDomain#createTriggerEffectInstance()` | Removed. Trigger effects only come from `trigger_effect` rules for the in-game domain. See [Adaptive trigger effects](contextual-api#adaptive-trigger-effects). |
| `TriggerEffectInstance` | Removed, with no code replacement. Write `trigger_effect` rules instead. |
| `GuideLocation` | Removed from the API. Guide rules still take `"where": "left"` or `"right"`. |
| `GuideVerbosity.CODEC` | Now internal. The enum itself is unchanged. |
| `ContextualStateSink` methods | Unchanged. `ContextualStateSink`, `ContextualDomain`, `ContextualDomainRegistry` and `GuideInstance` are now `@ApiStatus.NonExtendable`, so don't implement them. |
| Built-in facts and slots | None renamed or removed. `controlify:on_slow_block` and `controlify:use_animation/<animation>` are new. See [Built-in state](contextual-api#built-in-state). |

Before:

```java
@Override
public void onControlifyPreInit(PreInitContext context) {
    context.contextualDomains().inGame().registerContributor((inGame, sink) ->
            sink.contributeFact(Identifier.fromNamespaceAndPath("examplemod", "boosting"), isBoosting(inGame.player())));
}
```

After:

```java
@Override
public void onControlifyInitialize() {
    ContextualEvents.REGISTER_DOMAINS.subscribeForever(registry ->
            BuiltinContextualDomains.IN_GAME.registerContributor((inGame, sink) ->
                    sink.contributeFact(Identifier.fromNamespaceAndPath("examplemod", "boosting"), isBoosting(inGame.player()))));
}
```

## Rumble and feedback

v3 played rumble from code: you built a `RumbleEffect` and passed it, with a `RumbleSource`, to `ControlifyApi#playRumbleEffect`. v4 replaces all of it with feedback. You declare a `FeedbackEvent` and fire it, and the effect it plays is usually a data file that resource packs can replace. See [Feedback](feedback-api).

| v3 | v4 |
|---|---|
| `ControlifyApi#playRumbleEffect(source, effect)` | Removed. Declare a `FeedbackEvent`, register it in `FeedbackEvents.REGISTER`, and call `fire()` or `hold()`. See [Your own events](feedback-api#your-own-events). |
| `ControllerEntity#rumble()`, `RumbleComponent`, `RumbleManager#play(...)` | Removed. Events always play on the current controller. You can't target a specific controller. |
| `RumbleEffect`, `BasicRumbleEffect` | An effect file at `assets/<namespace>/controller/feedback/effect/<path>.json`. See [Effects](feedback-api#effects). For behaviour data can't describe, implement `FeedbackEffect` and register it with `FeedbackRegistry#registerEffect`. See [Code effects](feedback-api#code-effects). |
| `BasicRumbleEffect.constant(strong, weak, ticks)` | Two tracks, `"band": "low"` at `strong` and `"band": "high"` at `weak`, with `"duration"` in seconds. |
| `BasicRumbleEffect.byTick(...)`, `byTime(...)`, `seq(...)`, `repeat(...)` | A track's `envelope`, or an `lfo` for repeats. In code, read `Playback#seconds()`. Effects are sampled every frame, so work in seconds, not ticks. |
| `ContinuousRumbleEffect` | Call `FeedbackEvent#hold()` every tick while it should play. It ends shortly after the calls stop, or at once with `release()`. See [Firing and holding](feedback-api#firing-and-holding). |
| `ContinuousRumbleEffect#heartbeat()`, `timeout(...)`, `stop()`, `stopCondition(...)`, `BasicRumbleEffect#earlyFinish(...)` | `hold()` each tick, and stop calling it or call `release()` to end it. |
| `ContinuousRumbleEffect.Builder#inWorld(pos, min, max, range, falloff)` | `fireAt(pos, params)` or `holdAt(pos, params)`. Set the range with `FeedbackEvent.Builder#falloffRange(blocks)`. The falloff curve and the min and max strength are fixed. |
| `RumbleEffect#priority()`, `BasicRumbleEffect#prioritised(...)` | `ordering` in `groups.json`. It only decides which light layer or HD tone is on top. See [Ordering](feedback-api#ordering). |
| `RumbleState(strong, weak)` | `"band": "low"` and `"band": "high"` tracks, or `FeedbackFrame#rumble(low, high)` in a code effect |
| `TriggerRumbleState(left, right)` | A track with `"site": "left_trigger"` or `"right_trigger"`, or `FeedbackFrame#band(Site.LEFT_TRIGGER, band, amp)` |
| `RumbleSource.PLAYER`, `WORLD`, `INTERACTION`, `GUI` | The groups `controlify:player`, `controlify:world`, `controlify:interaction` and `controlify:gui`. Add your effect IDs to a group in `assets/controlify/controller/feedback/groups.json`. See [Groups](feedback-api#groups). |
| `RumbleSource.MASTER` | The master strength. It applies to every effect and isn't a group. |
| `RumbleSource.register(id)` | Define a new group in `groups.json`. Its slider is named by `controlify.vibration_strength.<namespace>.<path>`, as in v3. |
| `HDHapticComponent#playHaptic(...)` with an `.ogg` file | An effect with `hz` or `click` tracks. Mark detail with `"requires": "hd"`. See [Outputs](feedback-api#outputs). |
| Built-in rumble for damage, explosions, block breaking and the rest | Built-in events with data effects that packs can replace or select with rules. See [Selecting effects with rules](feedback-api#selecting-effects-with-rules). |

Before:

```java
ControlifyApi.get().playRumbleEffect(
        RumbleSource.PLAYER,
        BasicRumbleEffect.constant(1f, 0.5f, 10));
```

After:

```java
public static final FeedbackEvent<Void> BUMP =
        FeedbackEvent.builder(Identifier.fromNamespaceAndPath("examplemod", "bump")).build();

// in your entrypoint
FeedbackEvents.REGISTER.subscribeForever(registry -> registry.registerEvent(BUMP));

// where it happens
BUMP.fire();
```

`assets/examplemod/controller/feedback/effect/bump.json`:

```json
{
  "duration": 0.5,
  "tracks": [
    { "band": "low", "level": 1.0 },
    { "band": "high", "level": 0.5 }
  ]
}
```

`assets/controlify/controller/feedback/groups.json`, to keep it under the Player slider:

```json
{
  "groups": {
    "controlify:player": ["examplemod:bump"]
  }
}
```

## Look input

v3 had one event that let you change the combined stick and gyro look vector. v4 splits that into look scalers, which multiply the turn, and look sources, which add to it. Both are registered per controller. See [Look API](look-api).

| v3 | v4 |
|---|---|
| `ControlifyEvents.LOOK_INPUT_MODIFIER` | `LookEvents.REGISTER`, then `LookRegistry#registerScaler(id, factory)` or `registerSource(id, factory)` |
| `LookInputModifier` that multiplies `lookInput()` | A `LookScaler`. Call both `scales.rate(yaw, pitch)` and `scales.direct(yaw, pitch)` to scale stick and gyro alike. See [Scalers](look-api#scalers). |
| `LookInputModifier` that adds to `lookInput()` | A `LookSource` that calls `out.rate(...)`, `out.delta(...)` or `out.scripted(...)`. See [Sources](look-api#sources). |
| `LookInputModifier.functional(x, y)` | Removed. Scalers can only multiply, and sources can only add. |
| Zeroing `lookInput()` | A scaler that sets `rate(0, 0)` and `direct(0, 0)`. Scripted contributions, like flick stick, can't be scaled. |
| `LookInputModifier#controller()` (`ControllerEntity`) | `LookContext#controller()`, or the `Controller` your factory receives |
| `lookInput().x`, `lookInput().y` | Yaw and pitch. `LookContext` also gives `player`, `dt`, and the camera's `yaw` and `pitch`. |

Before:

```java
ControlifyEvents.LOOK_INPUT_MODIFIER.register(event -> {
    if (isZooming()) {
        event.lookInput().mul(0.25f);
    }
});
```

After:

```java
LookEvents.REGISTER.subscribeForever(registry -> registry.registerScaler(
        Identifier.fromNamespaceAndPath("examplemod", "zoom"),
        controller -> (ctx, scales) -> {
            if (isZooming()) {
                scales.rate(0.25, 0.25);
                scales.direct(0.25, 0.25);
            }
        }));
```

## Resource packs

Almost every Controlify resource now lives under `assets/<namespace>/controller/`. v4 doesn't read the old paths, and doesn't warn about files left there, so move them. The `controlify:xbox_one` controller type is now `controlify:xbox`. Players' saved settings are migrated, but packs aren't.

| v3 | v4 |
|---|---|
| `contextual/facts/<domain>.json` | `controller/contextual/facts/<domain>.json`. Same format. |
| `contextual/guide/<domain>.json` | `controller/contextual/guide/<domain>.json`. Same format. See [Button guides](contextual-api#button-guides). |
| `contextual/trigger_effect/in_game.json` | `controller/contextual/trigger_effect/in_game.json`. Same format. |
| None | `controller/contextual/feedback/in_game.json`: feedback rules. See [Selecting effects with rules](feedback-api#selecting-effects-with-rules). |
| Guide `then` using `controlify.binding.controlify.<action>` | `controlify.action.controlify.<action>`. Controlify's built-in language keys were renamed. |
| Guide and trigger effect `for`: a binding ID | An action ID. Same format. See [Built-in Actions](../reference/builtin-actions). |
| Slow-block rumble, hard-coded | An ambient rule in `controller/contextual/feedback/in_game.json` selects the effect `controlify:slow_block` while `controlify:on_slow_block` and `controlify:input_moving` are true. Change the rule or replace `controller/feedback/effect/slow_block.json`. |
| `haptics/navigate.ogg` | Removed. Replace the effect `controlify:ui_navigate` at `controller/feedback/effect/ui_navigate.json`. |
| `controllers/controller_identification.json5` | `controller/controller_identification.json5`. See [Custom Controller Identification](../resource-packs/custom-controller-identification). |
| Identification field `namespace` | `id` |
| Identification field `name` | `friendly_name` |
| Identification field `mapping` | `mapping_id`, an identifier of a joystick mapping at `controller/mapping/<path>.json`. In v3 the field had no effect. |
| Identification field `dont_load` | `prevent_loading` |
| Controller type `controlify:xbox_one` | `controlify:xbox`. An entry with the old ID is a new, unknown type, so it falls back to the `controlify:default` glyphs. |
| `controllers/font_mappings/<type>.json` | `controller/font_mapping/<type>.json`. See [Input Glyphs](../resource-packs/input-glyphs). |
| Font mapping `unknown` key | Removed. Unmapped inputs use the default set's glyph, or a text label like `B3`. |
| Font mapping value: exactly one character | One or more characters, drawn in sequence |
| None | Font mapping keys `controlify:gesture/<gesture>` draw gesture badges. See [Gesture badges](../resource-packs/input-glyphs#gesture-badges). |
| Built-in glyphs at `\u0001` to `\u001F` | Built-in glyphs at `` to ``. If you replace only a built-in font file or only its mapping, update it to the new characters. Replacing only the texture still works. |
| `font/controller/xbox_one.json`, `textures/font/controller/xbox_one.png` | `font/controller/xbox.json`, `textures/font/controller/xbox.png` |
| `controllers/font_mappings/xbox_one.json` | `controller/font_mapping/xbox.json` |
| `textures/gui/sprites/controllers/xbox_one.png` | `textures/gui/sprites/controllers/xbox.png`. Controller icons stay under `controllers/`. |
| `keyboard_layout/<layout>/<language>.json` | `controller/keyboard_layout/<layout>/<language>.json`. Same format. Key `shortcut` now names an action ID. See [Keyboard Layouts](../resource-packs/keyboard-layouts#where-layouts-go). |
| `controllers/default_config.json` | `controller/default_config.json`. The `gyro` section changed, flick stick moved to its own `flick_stick` section, and `light` is new. Start from the file Controlify ships. |
| `controllers/gamecontrollerdb-sdl3.txt` | `controller/gamecontrollerdb-sdl3.txt` |
