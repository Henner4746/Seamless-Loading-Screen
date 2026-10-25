# Seamless Loading Screen Continued

An unofficial continuation of [Seamless Loading Screen](https://github.com/Minenash/Seamless-Loading-Screen) by Minenash, ported to Minecraft 26.2 and 26.3 (Fabric).

When you leave a world or server, the mod takes a screenshot of what you were looking at. The next time you join, that screenshot replaces the loading screen and fades into the live world once the chunks have loaded.

A video of the original mod:
[![Seamless Loading Screen demonstration](https://i.imgur.com/NbG9zBP.png)](https://www.youtube.com/watch?v=PB9NinPPTwA "Seamless Loading Screen - Demonstration")

## Requirements

- Minecraft 26.2 or 26.3 with Fabric Loader 0.19 or newer
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [YetAnotherConfigLib](https://modrinth.com/mod/yacl)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, adds a settings button to the mod list)

## Usage

- **Singleplayer:** always on.
- **Servers:** off by default. Turn it on for a server under *Edit Server*. You can also change the default for all servers in the settings.
  - *Enabled* takes a new screenshot every time you leave.
  - *Custom / Freeze* keeps the screenshot that is already saved, so you can put your own image there.
  - *Disabled* shows the normal loading screen.
- **Settings:** press `G` in game, or use Mod Menu.

Screenshots are saved in `.minecraft/screenshots/worlds/`, with one folder each for singleplayer, servers and Realms.

## Changes in this fork

- Ported to Minecraft 26.2 and 26.3. Only Fabric is built; the NeoForge version and older Minecraft versions are dropped.
- The screenshot is also taken when you close the game window while in a world, not just when you quit through the pause menu.
- Screenshots are taken at the window's resolution, so the resolution setting is gone.
- Worlds opened by other mods now get their screenshot too, not only those opened from the world list or Quick Play.
- The "Server's Default Display Mode" setting now applies to servers that have no mode stored yet. Before, it had no effect and every server started out enabled.
- Chunks that have not started loading are see-through on the loading screen's chunk map again, and the server option button no longer leaves a gap above *Done*.
- Fixed the screenshot folder being nested twice (`screenshots/worlds/screenshots/worlds`). Existing screenshots are moved over automatically.

## Building

Java 25 is needed.

```
./gradlew build
```

The jars end up in `versions/<minecraft version>/build/libs`. `./gradlew :26.3:runClientGameTest` runs the in-game tests.

## Credits and license

The original mod was made by [Minenash](https://github.com/Minenash), with contributions from Blodhgarm and others. This fork is maintained by [Henner4746](https://github.com/Henner4746). Please report problems with this version [here](https://github.com/Henner4746/Seamless-Loading-Screen/issues), not to the original author.

MIT License, see [LICENSE](LICENSE).
