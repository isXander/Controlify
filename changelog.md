# Controlify {version}

This version has the following targets:
{targets}

[![](https://short.isxander.dev/bisect-img)](https://short.isxander.dev/bisect)

**By donating on my [Patreon](https://patreon.com/isxander), you will gain access to builds of Controlify for splitscreen support**

## SDL Natives changes

All SDL natives are now bundled inside the Controlify jar. 
Controlify no longer requests to download SDL natives from the internet,
this has only increased jar size by 44%.

Massive efforts have been made to reduce the size of these natives. 
For example, the Windows x64 native used to be 7 MB, it's now 470 KB.

As a result `quiet_mode` setting has been removed, as it is no longer needed as
Controlify no longer asks on first launch to download SDL natives.

## Changes

- {version} now has an additional target of 1.21.6.
- The controller carousel screen has a new 'Keyboard & Mouse' entry.
  This replaces the previous functionality where you could press 'Stop using' on
  the selected controller.
  - This is to prepare for the splitscreen feature.
- SDL has been updated to 3.2.16, a now-stable version.

## Bug fixes

- Fix a bug where servers forced you to enable features, not just prevent you from enabling them
- Fix not being able to scroll with a mouse in the villager trade screen
- Fix sprint starting with toggle sprint when you move slowly, not just when you move a lot (1.21.5+)

