# Controlify testing report

## Executive summary

Controlify now has the beginnings of a valuable end-to-end test harness: a real Minecraft client can discover an SDL virtual controller, receive controller input, exercise gameplay, and capture effects sent back to the device. This is the right foundation for testing the behavior that is most likely to regress at integration boundaries.

The recommended strategy is not to put every test into the client game-test suite. Controlify has four useful test layers:

1. **JVM tests** for codecs, mappings, maths, rumble composition, migrations, and other deterministic logic.
2. **Client game tests** for controller discovery, input polling, bindings, gameplay, screens, and device output.
3. **Resource-pack integration tests** for reload listeners, pack priority, cache invalidation, and the visible behavior produced by loaded data.
4. **Environment and compatibility tests** for loader parity, optional mods, platform integrations, and physical hardware.

The highest-value next step is to make the virtual controller context expose the corresponding Controlify `ControllerEntity` and provide tick-synchronised input and output assertions. With that in place, controller discovery, mappings, bindings, movement, gyro, resource reloads, and most gameplay actions become straightforward to test.

The first implementation milestone should cover the following suites:

- Virtual controller lifecycle and capabilities
- SDL-to-Controlify input translation
- Binding transitions and default binds
- Deadzones and controller mappings
- Input-mode and active-controller switching
- Analogue movement and core gameplay actions
- Rumble manager behavior and the remaining rumble event sources
- Pure JVM coverage for mappings, codecs, config migrations, and DualSense effect encoding

These tests exercise central contracts used by almost every Controlify feature and provide better value than starting with screenshots or optional-mod compatibility.

## Current game-test framework

### What is already possible

The framework under `src/gametest` can currently:

- Attach SDL virtual gamepads identified as Xbox or DualSense controllers.
- Define available SDL buttons and axes or provide a custom SDL gamepad mapping.
- Advertise gyro sensors and touchpads.
- Press and release buttons, move axes, and submit gyro or generic sensor samples.
- Wait for Minecraft ticks and use Fabric's singleplayer world, screen, input, and screenshot helpers.
- Observe simple rumble, trigger rumble, player index, RGB LED output, sensor enablement, and raw device-effect packets.
- Decode raw effect packets through a pluggable `GamepadEffectDriver`.
- Exclude physical controllers from the test environment while allowing virtual controllers.

`RumbleTests` proves the complete path from a world event, through Controlify's rumble logic and SDL driver, to a virtual device callback. That pattern can be reused for inputs, gyro, adaptive triggers, LEDs, and server-originated vibration.

### Important limitations

| Limitation | Why it matters | Recommended framework change |
| --- | --- | --- |
| The test controller does not expose its `ControllerEntity`. | Tests cannot inspect identity, components, settings, raw state, mapped state, bindings, or the active-controller relationship. | Resolve the entity after attachment by SDL joystick ID or UID and expose it from `TestControllerContext`. |
| Input mutation and tick advancement are separate operations. | Tests can accidentally assert before SDL and Controlify have both polled the new state. | Add `pressAndTick`, `releaseAndTick`, `setAxisAndTick`, and lower-level `awaitInputState` helpers. Keep non-ticking methods for multi-input frames. |
| Hats, balls, and touchpad fingers cannot be injected. | Generic joystick hats, touchpad state, and all mapping conversions cannot be tested end to end. | Wrap `SDL_SetJoystickVirtualHat`, `SDL_SetJoystickVirtualBall`, and `SDL_SetJoystickVirtualTouchpad`. |
| SDL setter return values are ignored. | Invalid indices or failed injections silently become timeouts later. | Check every SDL call and throw an `SDLException` at the mutation site. |
| Output state only stores the latest value. | A stale output can satisfy a later test, and pulse duration, ordering, mixing, and reset behavior cannot be asserted. | Record immutable output events with a sequence number and client tick; provide `clearOutput`, `outputHistory`, and `waitForNextOutput`. |
| Raw effect buffers are retained. | The native callback's `ByteBuffer` lifetime and position are not guaranteed after the callback. | Copy remaining bytes immediately and expose an immutable byte array or read-only copied buffer. |
| Controller cleanup only detaches the SDL joystick. | Tests need to know that Controlify processed disconnection and selected a fallback controller/input mode. | Close the opened handle as appropriate, detach, and wait until the manager no longer contains the entity. |
| Configuration is global and persistent across scenarios. | One test can change bindings, sensitivity, profiles, or feature flags for every later test. | Add a fixture that snapshots relevant DTOs/settings, applies changes on the client thread, and restores them in `close()`. |
| No resource-pack fixture exists. | Reload listener behavior and pack priority cannot be tested through Minecraft's real resource manager. | Register packaged test packs during test-mod client startup and add enable/reload/restore helpers. |
| Screen state is mostly opaque. | GUI tests need reliable assertions for focus, hovered slot, edit-box content, cursor location, and open overlays. | Add test-only accessors and focused semantic helpers rather than relying primarily on pixels. |
| Device power cannot be controlled through the virtual descriptor. | Low-battery notification behavior cannot be triggered deterministically. | Add a test-only power-state seam at the SDL driver boundary or test the notifier with a fake entity/component. |
| HD haptics require an eligible audio device. | A virtual SDL controller alone cannot exercise audio-device discovery and playback. | Unit-test audio conversion/queue logic and use an injectable haptic sink; leave native playback as a platform smoke test. |

### Test isolation requirements

Every client game-test class should leave the client in the same state in which it found it. A common test fixture should restore:

- Attached virtual controllers and current-controller selection
- Input mode
- Open screen and virtual-mouse state
- Global, profile, and device settings
- Bound inputs and radial actions
- Server policies
- Enabled resource packs
- Spawned entities, inventory contents, game mode, camera, HUD, and player pose when changed
- Outstanding rumble or trigger effects

Prefer one behavior per test entrypoint where Fabric permits it. If several scenarios must share a world or controller, clear input and output state before each scenario and wrap failures with the scenario name, as `RumbleTests` currently does.

## Recommended test backlog

Priorities are based on regression impact, breadth of exercised code, determinism, and implementation cost:

- **P0:** foundational coverage to implement first.
- **P1:** important feature coverage after the foundation exists.
- **P2:** valuable, but more specialised or more expensive to maintain.
- **P3:** environment-dependent coverage that should not block ordinary CI.

### P0: controller lifecycle and input foundations

#### Virtual controller lifecycle tests

| Scenario | Assertions | Blockers |
| --- | --- | --- |
| Attach an Xbox virtual controller. | Exactly one new controller appears; it becomes current; driver name, VID/PID, UID, GUID, and Xbox controller namespace are as expected; an input component exists. | Expose the attached `ControllerEntity` and manager lookup. |
| Attach a DualSense virtual controller. | The controller is identified as DualSense and receives the expected DualSense, input, rumble, LED/effect, gyro, and touchpad components according to advertised capabilities. | Entity/component inspection and a decoded effect driver. |
| Detach the current controller. | The manager removes it, a disconnect event fires once, its resources close, and the current controller/input mode fall back correctly. | Lifecycle event capture and close-and-await helper. |
| Attach two virtual controllers. | UIDs are distinct and stable; input from the inactive controller selects it only under the documented profile/current-controller rules. | Multiple-controller fixtures and active-controller assertion. |
| Attach an unsupported or non-gamepad virtual joystick. | The SDL joystick driver is selected, counts are correct, and no gamepad-only components or mappings appear. | Builder support for joystick type, hats, and generic input inspection. |
| Advertise optional output callbacks selectively. | Rumble, trigger-rumble, and LED components are present only when SDL reports those capabilities. | Builder switches for selectively omitting callbacks. |

Also assert event API behavior: `CONTROLLER_CONNECTED`, `CONTROLLER_DISCONNECTED`, and state-update events should identify the correct entity, contain the correct hotplug flag, and not be duplicated during reinitialisation.

#### SDL input translation tests

These tests should observe both `InputComponent.rawStateNow()` and the deadzoned/mapped `stateNow()`.

| Scenario | Assertions | Blockers |
| --- | --- | --- |
| Every advertised SDL gamepad button | Pressing one SDL index activates only the corresponding `GamepadInputs` identifier; release advances `stateThen`/`stateNow` correctly. | Entity and input-state access. |
| Signed stick axes | Negative and positive SDL values map to the correct directional axes, with the opposite direction at zero. Check endpoints and representative partial values. | Tick-aware axis helpers and approximate assertions. |
| Trigger axes | Resting, half, and fully pressed values map to the expected `0..1` Controlify axis. | Input-state access. |
| Generic joystick buttons and axes | Each button maps by index; each signed axis splits into positive and negative `JoystickInputs`; asymmetric `short` endpoints still map to `-1` and `1`. | Generic joystick builder preset. |
| Generic joystick hats | All nine SDL hat states map to the corresponding `HatState`. | Hat injection helper. |
| Custom SDL gamepad mapping | A deliberately unusual physical layout is transformed into canonical SDL gamepad inputs before Controlify reads it. | A reusable mapping fixture and state inspection. |
| Unadvertised inputs | Inputs absent from the descriptor mask do not appear as supported or accidentally activate. | Ability to query supported identifiers/counts. |

#### Binding state and context tests

Test `InputBinding` as a public behavioral contract, not its ring-buffer implementation.

- Button binding: `digitalNow`, `digitalThen`, `justPressed`, `justReleased`, and `analogueNow` across press, hold, and release frames.
- Axis binding around `buttonActivationThreshold`, including equality and values just below/above it.
- `justTapped` for a short press and non-tap behavior for a held press.
- Directional axes and opposing inputs, including simultaneous opposites.
- Suppression and restoration without manufacturing false transitions.
- Allowed `BindContext` behavior in-game, in normal GUI navigation, virtual mouse, radial menu, and no applicable context.
- GUI press repeat timing and reset after navigation.
- Borrowed state-access outputs and fake presses if these remain supported API behavior.
- Key-mapping emulation: controller press and release update the correlated Minecraft key mapping exactly once.
- Automatically generated bindings for otherwise uncorrelated Minecraft key mappings.

This suite needs binding lookup by ID and deterministic binding overrides in the config fixture.

#### Deadzone and controller-mapping tests

Use JVM tests for exhaustive transformations and a smaller client test to prove the settings-to-driver integration.

JVM coverage should include:

- Radial deadzone behavior at zero, just below, exactly at, just above, and at full magnitude.
- Default deadzone versus group-specific overrides.
- Every `MappingEntry` combination: button-to-button/axis/hat, axis-to-button/axis/hat, hat-to-button/axis/hat, and nothing-to-button/axis/hat.
- Inversion, thresholds, min/max remapping, all hat directions, and missing source inputs.
- Multiple mappings targeting the same output, preserving the documented order/overwrite behavior.
- Mapping and deadzone codec round trips, typed and fuzzy forms, malformed entries, and unknown types.

Client coverage should:

- Install a mapping in the virtual controller's device settings.
- Inject a raw input and assert raw, mapped, and deadzoned state independently.
- Change the mapping and deadzone at runtime and prove the next poll uses the new values.
- Reload or replace mapping data and verify cached mappings do not remain stale.

The last scenario is a high-value regression test because `ControllerMappingStorage` currently owns a static cache and is not itself a reload listener.

#### Input-mode and active-controller tests

- Controller input switches `KEYBOARD_MOUSE` to `CONTROLLER` when mixed input is disabled.
- Controller input switches to `MIXED` when mixed input is enabled.
- The frame that causes a mode switch is not also treated as an action press.
- Keyboard or mouse use switches back according to current settings.
- Tiny resting noise inside the configured deadzone does not switch modes.
- Input is suppressed when the window is inactive and out-of-focus input is disabled.
- A non-current controller can become current only when the current controller is idle and the active profile does not pin a UID.
- Repeated faulty input eventually disables the offending controller through the intended safeguard.

These tests need helpers to inspect and restore current controller, input mode, focus state, and active profile.

### P0: gameplay input

#### Movement tests

Create a flat singleplayer world and compare player input vectors and server-observed movement.

- Full and partial forward/back/left/right movement preserve analogue magnitude.
- Diagonal input with length greater than one is limited, not normalised upward from smaller values.
- `alwaysKeyboardMovement` and server analogue-movement policy quantise values at `buttonActivationThreshold`.
- Server whitelist behavior selects analogue or keyboard-style movement for matching and non-matching addresses; the pure address matching can be unit-tested.
- Opening a screen clears controller movement and action key states.
- Jump does not fire merely because the button was held while closing a GUI.
- Toggle sneak versus hold sneak, including flying, swimming, riding, landing, and dismount reset cases.
- Sprint, auto-jump, and disable-fly-drifting behavior under their settings and server policies.
- `DualInput` combines or chooses keyboard/controller state as intended in mixed-input configurations.

Prefer assertions on `ClientInput` and player state over position-only assertions; position introduces physics and network timing noise.

#### Look and gyro tests

- Stick look applies horizontal/vertical sensitivity, inversion, configured input curve, and pitch clamping.
- Reduced aiming sensitivity applies only in the intended aiming/use contexts.
- Registered `LookInputModifier`s run in order and can adjust or cancel input according to the API contract.
- A gyro-capable virtual controller causes SDL sensor enablement and creates a `GyroComponent`.
- Submitted gyro samples update all three axes with the expected orientation/sign.
- Gyro button modes, relative mode, pitch/yaw inversion, yaw mode, and sensitivity alter camera rotation correctly.
- Gyro input is suppressed in keyboard mode, unfocused state, or disabled settings.
- Flick-stick produces the intended yaw animation and does not leave a residual turn.

Exact camera deltas should use tolerances and controlled settings. The test fixture should disable unrelated easing and restore the original camera orientation.

#### Core action tests

Use default binds first, then one remapped-bind case to prove actions consume bindings rather than hardcoded SDL indices.

- Open/close inventory and pause screens.
- Next/previous hotbar slot, including wraparound and hold-repeat cadence.
- Drop one item, repeated drop, and drop full stack.
- Swap main/offhand items.
- Change perspective and restore renderer state.
- Pick block and pick block with NBT under the correct permissions.
- Toggle HUD and player list.
- Open the general, game-mode, hotbar selection, and creative load/save radial menus only when allowed.

Debug actions and screenshots are lower priority; test the binding/action dispatch with one harmless representative rather than making CI mutate debug or screenshot state repeatedly.

### P0: deterministic JVM tests

These tests should run without starting Minecraft wherever the involved classes permit it.

#### Input and utility maths

- Linear, power, cubic-blend, and S-curve input transformations at `-1`, intermediate negatives, zero, intermediate positives, and `1`.
- Sign preservation, endpoint preservation, and monotonicity for supported curve settings.
- Short/float conversion endpoints and round-trip tolerance.
- Vector easing and deadzone functions, including zero-length vectors and diagonal values.
- Hold-repeat timing and reset behavior.
- Ring-buffer growth/history behavior used by bindings.
- Snap-point directional ranking with aligned, diagonal, tied, wrapped, and out-of-range points.

#### Rumble logic

- `RumbleState` integer packing/unpacking endpoints and representative values.
- Basic effect constant frames, `byTick`, `byTime`, join, sequence, repeat, early finish, and priority.
- Continuous effect timeout, minimum duration, stop condition, silent frames, and builder modifiers.
- `RumbleManager` combines concurrent effects by maximum motor magnitude, applies source and master strengths, removes finished effects, sends a final zero state, clears effects, and handles silent mode.
- Trigger-rumble state scaling and driver conversion to unsigned 16-bit motor values.

#### DualSense effects

- Every `DualsenseTriggerEffect` variant validates parameter boundaries.
- Zero strength/amplitude produces the off state where documented.
- Feedback, weapon, vibration, multi-position, and slope effects produce exact type and parameter bytes.
- `DualsenseEffectsState` writes exactly 47 bytes with correct offsets, enable flags, LEDs, mute light, and left/right trigger placement.
- `TriggerEffectCodecs` accepts every documented compact and expanded form, round-trips values, and rejects invalid or ambiguous objects.

These tests are especially valuable because byte-layout mistakes can be unsafe or impossible to diagnose without a physical controller.

#### Configuration and data fixing

- DTO/settings round trips for global, shared, device, profile, input, gyro, rumble, DualSense, Bluetooth, and HD-haptic configuration.
- Collection copying: mutating a settings object should not unexpectedly mutate the source DTO, and serialised DTO collections should not alias mutable settings collections.
- Default values and bounds such as non-negative preferred profile.
- One golden old-config fixture for each source schema and one focused fixture for each fix:
  - The large controller/profile/device migration
  - Horizontal look inversion
  - Radial action migration and deduplication
  - Analogue movement whitelist and seen servers
  - DualSense trigger-effects setting
  - Guide GUI scales
  - Generated binding IDs in bindings and radial actions
- Full migration from the oldest supported fixture to the current schema followed by a successful current codec parse.
- Unknown fields survive migrations where forward-compatible remainder behavior is intended.

Keep migration fixtures as readable JSON resources. Assert the complete canonical output for focused fixtures so accidental field loss is visible.

#### Codecs and network payloads

- Input, rule, guide, radial icon, controller type, HID ID, deadzone, and mapping codec round trips.
- Strict/fuzzy codec ambiguity and malformed-object errors.
- Vibration, origin vibration, entity vibration, and server-policy packet round trips.
- Empty, one-frame, and long vibration arrays, plus invalid lengths if packet decoding is expected to defend against them.
- Unknown rumble-source and server-policy identifiers follow their documented registration/ignore behavior.
- Handshake equal, older-client, and newer-client protocol outcomes.

### P1: expanded rumble integration

The existing suite covers lightning, explosions, outgoing damage, and incoming damage. Add scenarios for every other event family represented by the rumble mixins:

| Feature | Suggested scenario | Main assertion |
| --- | --- | --- |
| Block breaking | Break a controlled block at known progress/speed. | Rumble begins, scales with progress if intended, and stops on completion/cancel. |
| Fishing | Cast, hook an entity/item, and reel in. | The correct interaction pulse occurs once per event. |
| Item use | Eat/drink, draw a bow, charge a crossbow, and use a continuous item. | Event-specific frame pattern and cleanup after release/completion. |
| Item break | Consume the final durability point. | A distinct item-break pulse occurs once. |
| Slow block movement | Walk through a representative slowing block. | Continuous effect exists only while the condition holds. |
| Water landing | Fall into water at two controlled velocities. | Stronger landing produces no weaker output and the effect ends. |
| Level events | Trigger representative world level events wired to rumble. | Expected motor/channel activates without unrelated duplicates. |

Add manager-level client tests for:

- Master and per-source intensity settings.
- Rumble disabled globally for the profile.
- Concurrent event mixing and priority.
- Paused client, keyboard input mode, inactive window, and config-screen exceptions.
- Switching current controller while an effect is active.
- Output history returning to zero after every effect.

Use unsigned comparisons for captured `short` values, since SDL motor magnitudes use unsigned 16-bit values represented by Java `short`.

### P1: DualSense, trigger rumble, LEDs, and sensors

#### Adaptive triggers

- Equipping or using an item matching built-in use/swing rules sends the expected left/right trigger effects.
- Releasing use or changing held item sends an off effect.
- Profile setting disables trigger effects.
- Controller input suppression clears effects.
- Resource rules override Java component and item registrations in the documented priority order.
- Component registrations override item registrations; a `null` component result falls through.
- Cached matches update after an `ItemStack` component/count change.
- Reload and client-tag updates invalidate resolved rules and stack caches.
- Registry/tag placeholders resolve after entering a world and do not leak across worlds.

Implement a `DualsenseEffectDriver` in the test framework that copies and decodes the 47-byte state. Tests should assert semantic fields and reserve raw-byte assertions for the JVM layout suite.

#### Other device output

- Queue trigger rumble and verify exact left/right output plus final zero state.
- Set player index and verify callback propagation.
- Set RGB LED color, including settings-driven brightness/color if applicable.
- Change the DualSense mute light and verify effect packets.
- Assert that unsupported virtual controllers do not expose or attempt unsupported output.
- Verify output callback failures are logged/handled without corrupting later controller updates; this needs a builder option for callbacks that return `false`.

#### Touchpads

- Advertised touchpad and finger counts create the correct `TouchpadComponent` model.
- Finger down/move/up updates ID, position, pressure, and current/previous frames.
- Two touchpads and multiple simultaneous fingers remain independent.
- Touchpad click maps to the canonical touchpad button.

Virtual-mouse touch movement is currently commented out. Keep component/driver tests active now, and add cursor tests when that feature is re-enabled rather than encoding the disabled implementation as expected behavior.

### P1: resource-pack integration

#### Required resource-pack fixture

Add a test-mod client initializer that registers one or more built-in test packs before the initial resource reload. The fixture should:

1. Snapshot selected pack IDs.
2. Enable a named test pack at a known priority.
3. invoke `Minecraft.reloadResourcePacks()` on the client thread.
4. Wait for the returned reload future and one settling tick.
5. Run semantic assertions against the relevant manager and visible controller behavior.
6. Restore the original selection and reload in `close()` even after failure.

Use small packs with unique `controlify_test:*` identifiers. Include separate low- and high-priority layers where precedence is under test. Avoid writing packs into the user's resource-pack directory during a run.

#### Resource-driven feature matrix

| Data family | Essential tests | Observable result |
| --- | --- | --- |
| Controller identification | Parse JSON5; recognise test VID/PID; duplicate HID priority; `dont_load`; friendly name, namespace, mapping ID, and icon; live type change. | Attached controller type or deliberate absence; controller reinitialises once on reload. |
| Default binds | Default fallback; controller-specific layer; `clear_below`; malformed layer; reload with `keepDefaultBindings` true/false. | Binding's default/current input matches the documented layering rules. |
| Default profile config | Recursive object merge, scalar/list replacement, multiple layers, malformed layer, missing base resource. | New profile/controller settings contain the merged values or fail clearly when the required base is missing. |
| Input font mappings | Known input, unknown fallback character, namespace fallback, malformed character, and controller-type switch. | Produced component uses the expected font and glyph character. |
| Keyboard layouts | Requested locale, default-language fallback, missing layout fallback, malformed path/JSON, and all built-in layout shapes. | Resolved `KeyboardLayout` and an on-screen keyboard with expected keys/actions. |
| Guide rules | Conditions, forbidden facts, verbosity, stacking, duplicate locations, dynamic rules, pack priority, and `replace`. | `GuideInstance` resolves the expected action/location set and refreshes after reload epoch changes. |
| Radial icons | Model and texture definitions, per-binding merge, high-pack override, malformed entry/layer, explicit and generated binding candidates. | Candidate status and resolved icon match the final merged map. |
| Adaptive trigger rules | Use/swing separation, item/tag/component predicates, first-match order, pack priority, malformed layer, reload/cache invalidation. | Matching held items produce the expected semantic trigger effect. |
| Controller mappings | All mapping forms, deadzone definitions, missing file, malformed file, and reload. | Raw virtual input is transformed to the expected Controlify state without stale cache data. |
| SDL gamepad database | A unique test GUID mapping and override order. | Virtual joystick is treated as a gamepad with the expected canonical controls. |

Pack-priority tests deserve special attention. Several loaders intentionally use resource stacks in different directions, and guide `replace` behavior, default-bind prepending, default-config merging, radial-icon overwrite order, and adaptive-trigger reversed layers should each have an explicit two-pack test.

### P1: server integration and policies

Use Fabric's singleplayer/integrated server for command and packet flow, and pure codec tests for malformed payloads.

- Successful Controlify handshake sets analogue movement to allowed.
- Protocol mismatch disconnects with the correct client/server-old distinction.
- Disconnect resets all `ServerPolicies` to `UNSET`.
- Policy packets update reach-around, fly drifting, and analogue movement; unknown IDs are ignored.
- `/vibratecontroller ... static` reaches the virtual controller for the requested duration and strengths.
- Positioned vibration attenuates with distance and stops outside range.
- Entity vibration follows a moving entity, handles entity removal, and attenuates by distance.
- `allowServerRumble = false` blocks all three client-bound vibration forms.
- Commands require the intended permission level and reject invalid strength, range, and duration arguments.

Add packet/output history before these tests so duration and attenuation can be asserted without sampling races.

### P1: reach-around and gameplay patches

#### Reach-around

Cover the decision table directly and through one placement integration test:

- Existing block/entity hit is never replaced.
- Miss while looking down, on ground, supported by a non-air block produces a hit on the supporting block.
- Pitch below the threshold, airborne player, air support, vehicle, or disallowed policy preserves the miss.
- `OFF`, singleplayer-only/default behavior, `ALLOWED`, `DISALLOWED`, and `UNSET` combinations respect global setting and server policy.
- Interacting with the generated hit can place a block without breaking ordinary block interaction.

The pure hit-result test can call `ReachAroundHandler` with controlled entities/world state; the placement test proves the Minecraft mixin path.

#### Other gameplay patches

- Analogue boat input produces proportional steering/acceleration and keyboard input still works in mixed mode.
- Tutorial/key-message substitutions use controller glyphs only in controller modes.
- Accessibility behavior such as auto-jump and sprint handling follows profile settings.
- Offhand and vehicle transitions do not leave stale held/toggled actions.

### P1: public API and extension contracts

Controlify's extension APIs deserve small contract tests so internal refactors do not silently break other mods:

- Entrypoint ordering: pre-init registration runs before guide domains and bindings freeze; init runs after core systems exist; controller discovery callback runs once after discovery.
- A failing third-party entrypoint is logged and isolated rather than preventing other entrypoints or Controlify from initialising.
- Binding registration validates duplicate IDs, controller filters, context sets, radial-candidate metadata, key emulation, and `on`/`onOrNull` behavior.
- Binding registration is rejected after the registry is locked.
- Custom guide domains accept facts and dynamic rules before freezing, reject duplicate/late registration, participate in resource reload, and create usable instances.
- Rumble API calls route to the current capable controller and become a safe no-op when no capable current controller exists.
- Trigger-effect API registrations obey resource, component, and item priority and handle a component callback returning no effect.
- Look-input modifiers and active-controller/state events execute in documented order and receive the correct controller.
- Custom screen processors/component processors are selected by the registry and can define focus, navigation, guides, and virtual-mouse behavior.
- Public virtual-mouse snapping points contributed by a screen are collected, filtered, and navigated with built-in points.

Use a tiny test-only Controlify entrypoint in the game-test mod for lifecycle and registration tests. Keep deliberately failing registrations isolated to a launch profile or test phase so global frozen registries cannot contaminate later scenarios.

### P2: screen operations and virtual mouse

GUI tests should favour semantic state over screenshots. Screenshots are best reserved for a few stable visual contracts such as guide placement, controller glyphs, keyboard layout, and radial selection.

#### Generic navigation

- D-pad/stick navigation moves focus among vanilla buttons in all four directions.
- Confirm activates the focused component once; back closes to the correct parent.
- Hold repeat works for lists, sliders, and cycling controls without skipping unexpectedly.
- Slider left/right changes values by expected increments and respects endpoints.
- Tab navigation, list entries, and scroll position remain visible after focus changes.
- UI focus/clack sounds respect `extraUiSounds` and do not duplicate.
- Mouse use, controller use, and mixed input update focus and cursor visibility consistently.

#### Screen-specific processors

Create representative tests for:

- Title, pause, options, and options-subscreen navigation.
- World selection and creation.
- Multiplayer server list, direct join, add/edit server, and server-list entry actions.
- Language selection.
- Chat, command suggestions, and history.
- Sign editing and line switching.
- Inventory/container slot navigation, quick move, take/place one/all, outside click, and close.
- Recipe-book tabs, filter, recipes, and page navigation.
- Creative inventory tabs and search.
- Merchant, bundle, loom, stonecutter, and enchantment special actions/snapping.

Start with inventory, options, and chat because they exercise most shared processors. Add one regression test per specialised processor rather than duplicating the entire generic navigation matrix on every screen.

#### Virtual mouse

- Toggle only on eligible screens and restore ordinary cursor behavior when disabled.
- Stick movement applies sensitivity, easing, window scaling, and bounds.
- Releasing movement snaps to the nearest in-range point.
- Directional snapping chooses the closest valid point and handles wrap/fallback behavior.
- Left, right, and shift click deliver correct mouse button/modifier sequences.
- Scroll accumulates and decays without changing direction or continuing indefinitely.
- Back closes the screen.
- Container/recipe/enchantment/loom/stonecutter snap-point providers expose visible, valid targets.

Expose virtual-mouse target/current coordinates and last snapped point through test-only accessors. Avoid asserting GLFW cursor position alone because interpolation makes it timing-sensitive.

### P2: radial menus, guides, glyphs, and keyboard

#### Radial menus

- Open on press, select with stick or directional bindings, execute on release/confirm, and cancel cleanly.
- Deadzone and focus timeout prevent accidental selection.
- Page switching preserves or resets selection according to the UI contract.
- Missing/unbound actions render safely and do not execute another action.
- User-configured radial actions and generated modded bindings appear in the expected order.
- Game-mode and creative hotbar menus enforce permissions/mode restrictions.
- Model, texture, and fallback radial icons render without missing assets.

#### Button guides

- Unit-test every built-in fact provider with controlled contexts, especially verbosity thresholds, player mode/pose, hit result, held items, and container cursor/item state.
- Test rule selection, forbidden facts, stacking, action-location conflicts, and missing facts.
- Verify `GuideInstance` recomputes after a domain reload epoch and after relevant context changes.
- Use screenshots for one in-game guide at top/bottom and one container guide, at fixed resolution and GUI scale.
- Assert guide enablement, verbosity, and scale settings semantically before visual comparison.

#### Input glyphs

- Each built-in controller namespace maps canonical inputs to a non-fallback glyph where defined.
- Unknown inputs use the namespace's unknown glyph.
- Missing controller namespace falls back to the default map.
- A binding with multiple relevant inputs produces the expected component sequence/font.
- Controller type changes update glyph output after reload/reinitialisation.
- Translation/keybind mixins substitute controller glyphs only in the intended input mode.

#### On-screen keyboard

- Full, simple, and server-IP layouts resolve and render.
- Character, backspace, delete, cursor, shift/caps, space, enter, and cancel keys mutate the target correctly.
- Shift/caps state changes displayed and inserted characters.
- Locale fallback selects `en_us` when a locale-specific file is absent.
- Chat, sign, server-address, and ordinary edit-box integrations choose the right layout and hints.
- Disabling the on-screen keyboard setting prevents automatic opening without breaking edit-box focus.

### P2: configuration and settings UI

- Load missing configuration using defaults and save a current-schema file.
- Corrupt configuration produces the intended recovery/error behavior without partially applying state.
- Profiles and device settings are created, selected, renamed/updated, and associated with stable controller UIDs.
- Preferred profile and pinned controller survive restart/reload semantics.
- Applying settings updates live components; cancel/discard restores previous values.
- Reset-to-default uses the currently loaded resource-pack defaults.
- Rebinding detects intended buttons/axes, supports cancellation and unbinding, and does not capture resting noise.
- Deadzone calibration and gyro calibration update only the target controller/device settings.
- Rumble preview and LED/trigger settings produce output only on capable controllers.
- A newly attached generic joystick without a mapping opens the mapping setup stage; completing or cancelling it advances to the correct next screen.
- A Bluetooth DualSense shows its warning only when applicable, persists the dismissal setting, and preserves the screen that was open before the wizard.
- Multiple queued setup wizards run in attachment order and skip disabled stages without trapping the current screen.
- Hotplug connection toasts contain the resolved friendly controller name and do not appear for initial discovery where the contract excludes them.

Client restart persistence is expensive in a game test. Cover serialization and manager load/save in JVM or focused integration tests, then use one process-level smoke test for persistence if the CI runner can launch a second client instance with an isolated game directory.

### P3: constrained and environment-dependent coverage

| Feature | Practical automated coverage | Remaining blocker |
| --- | --- | --- |
| Low-battery notifier | Unit/integration test with an injected `BatteryLevelComponent`; verify threshold, deduplication, recovery, setting, and toast content. | SDL virtual joystick does not provide a controllable power callback. |
| HD haptics | Test sound decoding, channel conversion, queue length, timeout, and sink calls with a fake audio sink. | Native path requires a discoverable four-channel DualSense audio device. |
| Steam Deck | Unit-test mode/CEF payload parsing and driver decision logic; run an opt-in Deck smoke suite. | Requires SteamOS/Decky/CEF environment and possibly hardware. |
| SDL native loading | Process-level tests for native-source precedence, missing loader/native failure, subsystem initialisation, audio failure tolerance, configured hints, and native/binding version mismatch. | Native libraries cannot be unloaded and safely reloaded with different conditions in one JVM; use isolated launch profiles. |
| Native keyboard | Test decision/fallback logic and ordinary overlay keyboard. | Platform modal behavior needs OS/controller-specific automation. |
| Physical SDL backends | Run opt-in smoke tests for representative Xbox, PlayStation, Switch, and generic devices. | Hardware lab and stable USB/Bluetooth provisioning. |
| Optional mods | Launch dedicated compatibility profiles and test one representative screen/action per integration. | Additional mod dependencies, version churn, and licensing/distribution constraints. |
| NeoForge parity | Compile all common tests and build a loader-neutral harness where possible; run manual/smoke matrix initially. | Current client game-test framework is Fabric-specific. |

Optional-mod priorities should be YACL first because it backs Controlify's own settings, then Sodium/Reese's Sodium Options, Simple Voice Chat, Iris, and FancyMenu. Compatibility tests should verify that controller navigation works and no mixin/accessor failure occurs; they should not duplicate those mods' own feature tests.

## Framework implementation roadmap

### Milestone 1: controller observability and deterministic frames

1. Resolve and store the Controlify `ControllerEntity` created for each virtual joystick.
2. Expose identity, components, raw/mapped state, bindings, settings, and whether it is current.
3. Add checked input methods for buttons, axes, hats, touchpads, and sensors.
4. Add an explicit frame API:
   - Mutate several inputs without ticking.
   - Commit one frame and wait for Controlify to poll it.
   - Await a semantic input/binding condition with a bounded timeout.
   - Neutralise all inputs during cleanup.
5. Wait for both attachment and detachment lifecycle completion.

This milestone unlocks nearly every P0 client test.

### Milestone 2: output event recording

1. Replace latest-only fields with immutable event records while retaining convenient latest-value accessors.
2. Record sequence, client tick, output kind, and copied payload.
3. Add clear, snapshot, and `waitForNext` APIs that cannot be satisfied by stale output.
4. Treat captured motor values as unsigned 16-bit values in assertion helpers.
5. Implement a semantic DualSense effect decoder.
6. Allow callback success/failure and supported capabilities to be configured independently.

This milestone makes temporal rumble, server vibration, LEDs, trigger rumble, and adaptive-trigger tests reliable.

### Milestone 3: state fixtures

Add `AutoCloseable` fixtures owned by `ControlifyGameTestContext` for:

- Global/profile/device settings
- Binding overrides and deadzones/mappings
- Input mode and current controller
- Server policies
- Player/world state
- Screens, cursor, and virtual mouse

Fixtures must restore state in `finally`/`close`, even after an assertion fails. Prefer DTO copies or manager-supported snapshots over reflection-based field-by-field restoration.

### Milestone 4: resource packs

1. Add a client initializer to the test mod so test packs are registered before reload listeners freeze or initial resources load.
2. Package minimal base, lower-priority, and higher-priority test packs under game-test resources.
3. Implement pack selection, async reload waiting, and guaranteed restoration.
4. Add accessors for loaded manager state only where no public behavioral observation exists.
5. Run controller-type reload tests with care: they intentionally replace `ControllerEntity` instances, so test contexts must either follow reinitialisation or declare themselves invalid.

### Milestone 5: GUI semantics

1. Add accessors for focused widget, screen processor, virtual-mouse target/current position, snap points, hovered slot, edit-box contents, and guide/radial selection.
2. Build semantic helpers such as `pressBinding`, `assertFocused`, `assertScreen`, `assertSlot`, and `assertText`.
3. Fix window size, GUI scale, language, resource packs, and animation time for screenshot tests.
4. Keep screenshot templates few and intentional; use semantic assertions for most behavior.

### Milestone 6: external seams and matrices

- Introduce small injectable boundaries for battery readings and haptic output if production design permits them cleanly; otherwise use game-test mixins.
- Create separate Gradle/run profiles for optional mods rather than loading every compatibility dependency into core CI.
- Compile and run JVM tests on every supported Stonecutter version.
- Run the deterministic Fabric client suite on every supported version when runtime cost permits; otherwise run the full suite on one version and lifecycle/input/resource smoke tests on the others.
- Keep physical hardware and Steam Deck suites opt-in and non-blocking until a managed runner exists.

## Suggested suite layout

The exact class names can follow project conventions, but separating tests by responsibility will make failures easier to diagnose:

```text
src/test/java/.../controlify/test/
  binding/
  codec/
  config/
  controller/mapping/
  dualsense/
  rumble/
  server/packet/
  util/

src/test/resources/
  config_migrations/
  codec_fixtures/

src/gametest/java/.../controlify/gametest/tests/
  ControllerLifecycleTests
  ControllerInputTests
  BindingTests
  MovementTests
  GameplayActionTests
  GyroTests
  RumbleTests
  DualsenseOutputTests
  ResourcePackTests
  ServerIntegrationTests
  ScreenNavigationTests
  VirtualMouseTests
  RadialMenuTests
  GuideAndGlyphTests
  KeyboardTests

src/gametest/resources/
  resourcepacks/controlify_test_base/
  resourcepacks/controlify_test_low/
  resourcepacks/controlify_test_high/
  templates/
```

Do not create one game-test entrypoint for every tiny assertion if client startup dominates runtime. Group related scenarios when they can safely share expensive setup, but reset controller input, output history, player state, and feature settings between them.

## CI and acceptance criteria

### Recommended CI stages

1. **Fast JVM stage:** compile all variants and run deterministic JVM tests on every supported Minecraft version.
2. **Core client stage:** run lifecycle, input, bindings, movement, rumble, and resource-pack tests on the primary Fabric version.
3. **Version smoke stage:** run lifecycle, one input/action flow, one rumble flow, and one resource reload on each additional supported Fabric version.
4. **Compatibility stage:** scheduled or change-triggered profiles for YACL and optional mods.
5. **Hardware stage:** opt-in/manual jobs for Steam Deck, HD haptics, native keyboard, Bluetooth/power, and representative physical controllers.

### Reliability rules

- Every wait must have a bounded timeout and report the semantic state it was awaiting.
- Never use a previous output as proof of a new event; wait on an output sequence number.
- Use approximate comparisons for analogue/camera values and exact comparisons for digital states and encoded bytes.
- Fix world seed/settings, window resolution, GUI scale, language, and pack selection where relevant.
- Avoid real-time sleeps; advance game ticks through the test context.
- Avoid assertions based only on log messages.
- Keep screenshots out of tests whose behavior can be asserted through state.
- A test that changes persistent files must use an isolated game/config directory or restore from an exact snapshot.
- Test helpers should fail at the mutation/assertion site with controller ID, tick, input/output history, and current screen in the error message.

### Definition of useful coverage

A feature is meaningfully covered when tests verify its public or player-visible contract across the relevant boundary. Examples:

- A mapping codec test alone does not cover controller mappings; at least one client test must prove mapped virtual input reaches a binding.
- A guide rule codec test alone does not cover data-driven guides; a reload test must prove pack priority and a guide instance must resolve the expected actions.
- A rumble mixin invocation alone does not cover rumble; the virtual device must observe the expected output and eventual stop.
- A screen screenshot alone does not cover controller navigation; injected controller input must change focus or activate the intended action.

## Recommended implementation order

1. Controller entity lookup, checked/tick-aware input, hats/touchpads, and detach waiting.
2. Output histories and DualSense decoding.
3. Lifecycle, input translation, binding, deadzone/mapping, and input-mode suites.
4. JVM mapping, rumble, DualSense, codec, packet, and migration suites.
5. Movement, look, gyro, core gameplay actions, and expanded rumble suites.
6. Config and resource-pack fixtures, followed by every data-driven manager's priority/reload tests.
7. Server policies, commands, reach-around, and gameplay patches.
8. Screen navigation, virtual mouse, radial menus, guides, glyphs, and keyboard.
9. Persistence, optional-mod profiles, loader parity, and hardware-dependent suites.

This order builds reusable observability first, then covers the contracts with the largest blast radius, and leaves visually brittle or environment-dependent tests until the deterministic core is protected.
