# Controlify {version}

This version has the following targets:
{targets}

**By donating on my [Patreon](https://patreon.com/isxander), you will gain access to builds of Controlify for splitscreen support and snapshot versions**

## Changes

- Add Steam Controller sprite
- Detect Steam Controller explicitly
- Attempt to load SDL3 from the system as a fallback, laying the groundwork for future Android compatibility
- Fix detecting Android platform as Linux, leading to attempt to load incorrect native library of SDL
