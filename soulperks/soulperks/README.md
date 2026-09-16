# Soul Perks (Fabric 1.21.1)

Kill a player -> they drop their **Soul**. Throw the Soul into the void -> choose 1 of 3 random perks (out of 12). Perks stack.

## Build
Requires JDK 21.
    ./gradlew build        -> build/libs/soulperks-1.0.0.jar
    ./gradlew runClient    -> test client

Needs Fabric API on client and server.

## Texture
Replace `src/main/resources/assets/soulperks/textures/item/soul.png` (16x16 PNG).

## Commands
- `/soulperks list` - your perks
- `/soulperks reopen` - reopen a pending choice
- `/soulperks test` (op) - simulate a soul falling in the void
- `/soulperks reset <player>` (op)

## Tuning
- Perk values & max stacks: `perk/Perk.java` (update lang descriptions too)
- Keep perks on death / number of choices: `SoulPerks.java`
