---
title: Controlify Entrypoint
---

# Controlify Entrypoint

_Learn how to hook into Controlify._

Controlify provides a Fabric entrypoint to hook into important lifecycle stages of Controlify. You do this just like `ClientModInitializer`.

## Registering the entrypoint

- On Fabric, you register an entrypoint like any other, by adding an entry to your `fabric.mod.json` file under the `entrypoints` section.
- On NeoForge (or Fabric), you register an entrypoint using a Java service provider interface (SPI) in your `META-INF/services` directory.

::: code-group
```json [fabric.mod.json]
{
    "entrypoints": {
        "controlify": [
            "com.example.mymod.ControlifyEntrypoint"
        ]
    }
}
```

```txt [META-INF/services/dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint]
com.example.mymod.MyControlifyEntrypoint
```
:::

Then simply create the class and implement the interface:

```java
public class ControlifyEntrypoint implements ControlifyEntrypoint {
    @Override
    public void onControlifyPreInit(PreInitContext ctx) {
        // ...
    }

    @Override
    public void onControlifyInit(InitContext ctx) {

    }

    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {
        // ...
    }
}
```

## Using the entrypoint

Use intellisense to see the methods available in each of the init contexts.

Such things include registering custom bindings, button guides, and more.
