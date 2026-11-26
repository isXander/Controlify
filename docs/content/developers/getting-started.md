---
title: Getting Started
---

# Getting Started

_Add Controlify to your mod, and keep it optional._

## Adding the dependency

Controlify is published to `https://maven.isxander.dev/releases`, as `dev.isxander:controlify`. Versions are the
Controlify version and the Minecraft version, like `4.0.0+mc26.3`.

One module holds every loader's jar, so choose yours by requiring its capability:

::: code-group
```kotlin [Fabric]
repositories {
    maven("https://maven.isxander.dev/releases")
}

dependencies {
    implementation("dev.isxander:controlify:4.0.0+mc26.3") {
        capabilities { requireCapability("dev.isxander:controlify-fabric") }
    }
}
```

```kotlin [NeoForge]
repositories {
    maven("https://maven.isxander.dev/releases")
}

dependencies {
    implementation("dev.isxander:controlify:4.0.0+mc26.3") {
        capabilities { requireCapability("dev.isxander:controlify-neoforge") }
    }
}
```

```kotlin [Multiloader common]
repositories {
    maven("https://maven.isxander.dev/releases")
}

dependencies {
    compileOnly("dev.isxander:controlify:4.0.0+mc26.3") {
        capabilities { requireCapability("dev.isxander:controlify-common") }
    }
}
```
:::

The capability brings the right jar, its sources, and its dependencies, including YACL. Don't use a classifier
like `:fabric` instead: your IDE then can't find the sources. Builds that set the `io.github.mcgradleconventions.loader`
attribute to `fabric`, `neoforge` or `common` select the right jar without a capability.

The jar that players download is a single universal jar, which runs on both loaders.

## Keeping it optional

Most mods that support Controlify still work without it, so don't make it a required dependency. Instead, stop
players from running your mod alongside a Controlify older than the API you use:

::: code-group
```json [fabric.mod.json]
"breaks": {
    "controlify": "<4.0.0-"
}
```

```toml [neoforge.mods.toml]
[[dependencies.mymod]]
modId = "controlify"
type = "optional"
versionRange = "[4.0.0,)"
side = "CLIENT"
```
:::

On Fabric, the `-` in `<4.0.0-` makes the range stop before Controlify 4.0.0's pre-releases, so they still count as
new enough.

In code, only reach Controlify's API from your [Controlify entrypoint](controlify-entrypoint#keeping-controlify-optional),
so your mod still loads when Controlify isn't installed.

## The example addon

These pages build one example mod: a boating mod, with a horn, a boost and a throttle, tilt steering, a settings
screen, and a zoom key. Each page adds the Controlify support for one part of it. Every Java snippet, and most JSON snippets, are
taken from the example's source, which compiles against Controlify on every Minecraft version it supports. Read it
in full on [GitHub](https://github.com/isXander/Controlify/tree/main/src/example).

The example is split the way these pages recommend:

| Path | Contains |
|---|---|
| `ExampleMod`, `Boats`, `ExampleKeyMappings`, `Zoom`, `settings/`, `mixin/` | The mod itself, which works without Controlify |
| `ExampleControlifyEntrypoint` | Where Controlify starts the integration |
| `actions/` | [Actions](actions) |
| `screen/` | [Screen behaviours](screen-operation-api) |
| `contextual/` | [Contextual facts and domains](contextual-api) |
| `feedback/` | [Feedback](feedback-api) |
| `look/` | [Look sources and scalers](look-api) |
| `events/` | [Events](controlify-entrypoint#events) |
| `resources/assets/controlify/controller/` | Data for Controlify: activators, default bindings, rules and more |
| `resources/assets/examplemod/` | The mod's own effects, guide rules and translations |
