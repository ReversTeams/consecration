# Consecration: NeoForge 1.21.1

Target: Minecraft 1.21.1, Java 21, NeoForge 21.1.x (tested with 21.1.265).
Branch: port/1.21.1-neoforge.

## Build

With a Java 21 JDK installed:

```powershell
.\gradlew.bat build
```

Linux/macOS: `./gradlew build`.
The installable mod is in `build/libs/`; do not install the `-sources.jar`.

## Validation

The three ports were loaded together in an isolated NeoForge dedicated server. Runtime smoke checks cover world loading, Consecration undead resistance and vulnerability, attachment save/load, Undead Protection, potion brewing, fire weapons, crafting recipes, Demolition Order goal installation/block breaking, and Weighted Armors tags/speed modifier removal.

The combined dedicated-server and integrated-client runs each passed 22 behavior checks. The client also entered a world, received the server vulnerability payload, and loaded the fire-arrow renderer. Reproducible checks are in the Consecration branch under `validation/`. Large modpack compatibility and every configuration combination have not been tested.

Implementation notes:
- Native NeoForge persistent entity attachments replace Forge capabilities.
- Native server configuration replaces SpectreLib; SpectreLib is not required.
- Vulnerability uses registered NeoForge payloads.
- Undead Protection is a data-driven 1.21.1 enchantment. Its custom condition still recognizes configured undead, not just vanilla undead.
- Fire arrows use their own entity type and renderer, including dispenser support.
- Silent Gear and Werewolves/Vampirism integrations target the 1.21.1 APIs. They are optional and have not yet been tested in a full pack with those mods installed.
- Tetra integration is skipped because no upstream 1.21.1 branch/port was found. Its old implementation remains in the historical Forge folder.
- The common/ and neoforge/ folders are built on this branch. forge/, fabric/, and quilt/ retain historical 1.20.1 source and are not part of this build.
- Importing 1.20.1 Forge worlds/ForgeCaps into this port is not supported or tested. Use a new test world.
