# Minecraft Version Support Matrix

**Status of this document**: living reference. Edits should accompany every
addition or removal of a Stonecutter target version.

## Supported MC versions (first-class)

These are the versions exercised by `./gradlew chiseledBuild` and present in
`settings.gradle.kts`'s `versions(...)` declaration. Each builds and runs as
a tested artifact.

| MC version | Status      | Yarn build (current) | Notes                                                                                                                                                                                                                                                          |
|------------|-------------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1.21.1     | first-class | `1.21.1+build.3`     | First widely-adopted point release after 1.21 / Tricky Trials.                                                                                                                                                                                                 |
| 1.21.4     | first-class | `1.21.4+build.8`     | "Bundles of Bravery" — adds PALE_OAK_SAPLING, CLOSED_EYEBLOSSOM, OPEN_EYEBLOSSOM. Lacks 1.21.5+ symbols (BUSH, FIREFLY_BUSH, SHORT_DRY_GRASS, TALL_DRY_GRASS, CACTUS_FLOWER, WILDFLOWERS), so it requires its own Stonecutter target — see "Why a separate 1.21.4 build?" below. |
| 1.21.5     | first-class | `1.21.5+build.1`     | "Spring to Life" — adds BUSH, FIREFLY_BUSH, WILDFLOWERS, SHORT_DRY_GRASS, TALL_DRY_GRASS, CACTUS_FLOWER. Renames `BambooSaplingBlock` → `BambooShootBlock`. Default Stonecutter active version.                                                                |
| 1.21.8     | first-class | `1.21.8+build.1`     | Stable mid-series; no plant-API drift relative to 1.21.5.                                                                                                                                                                                                      |
| 1.21.11    | first-class | `1.21.11+build.5`    | Final 1.21 series version. "Mounts of Mayhem".                                                                                                                                                                                                                 |
| 26.1.2     | first-class | Mojmap (no Yarn)     | 2026 year-based versioning. Mojang official mappings; Java 25; no Yarn. Uses `//? if >=26` Stonecutter conditionals throughout — see "Why a separate 26.1.2 build?" below.                                                                                     |
| 26.2       | first-class | Mojmap (no Yarn)     | Adds sulfur / cinnabar / speleothem blocks and collapses the dyed-colour and weathering-copper block families into `ColorCollection` / `WeatheringCopperCollection`. **No plant-block changes vs. 26.1.2** — the shared `//? if >=26` source tree compiles unmodified. Requires Loom 1.17+.                                    |
| 26.3       | first-class | Mojmap (no Yarn)     | Adds the poplar wood set (`POPLAR_SAPLING`), `RED_SHRUB` (a `BushBlock`) and the wall-attached `SHELF_MUSHROOM` (intentionally excluded). `DirtPathBlock` → `PathBlock` (the `Blocks.DIRT_PATH` field is unchanged). New enum entries are gated on `//? if >=26.3`. |

## Per-version compatibility map

The 1.21 series spans 12 game versions (1.21, 1.21.1, …, 1.21.11). The mod
ships **8 jars**; the five 1.21.x jars together cover **all 12** by leveraging each game-drop's
internal patch line (Mojang's "drop X.Y" branches share the same plant block
API across patch releases within the drop) and adding a dedicated 1.21.4
build to handle the symbol gap between "Bundles of Bravery" and
"Spring to Life".

The mapping below is the authoritative source for the `game-versions:` field
in `.github/workflows/release.yml`'s mc-publish step and for the Modrinth /
CurseForge "supported versions" list per release entry.

| Built jar | Drop name           | Patch versions covered       | Modrinth / CF `game-versions` value | `depends.minecraft` (fabric.mod.json) |
|-----------|---------------------|------------------------------|-------------------------------------|---------------------------------------|
| 1.21.1    | Tricky Trials *     | 1.21, 1.21.1, 1.21.2, 1.21.3 | `1.21,1.21.1,1.21.2,1.21.3`         | `>=1.21- <1.21.4`                     |
| 1.21.4    | Bundles of Bravery  | 1.21.4                       | `1.21.4`                            | `>=1.21.4- <1.21.5`                   |
| 1.21.5    | Spring to Life      | 1.21.5                       | `1.21.5`                            | `>=1.21.5- <1.21.6`                   |
| 1.21.8    | Chase the Skies     | 1.21.6, 1.21.7, 1.21.8       | `1.21.6,1.21.7,1.21.8`              | `>=1.21.6- <1.21.9`                   |
| 1.21.11   | Mounts of Mayhem ** | 1.21.9, 1.21.10, 1.21.11     | `1.21.9,1.21.10,1.21.11`            | `>=1.21.9- <1.22`                     |
| 26.1.2    | 2026 (year-based)   | 26.1.2                       | `26.1.2`                            | `>=26.1.2- <26.2`                     |
| 26.2      | 2026 (year-based)   | 26.2                         | `26.2`                              | `>=26.2- <26.3`                       |
| 26.3      | 2026 (year-based)   | 26.3                         | `26.3`                              | `>=26.3- <26.4`                       |

\* The 1.21.1 jar is bytecode-compatible across 1.21 → 1.21.3 because the
plant Block roster is unchanged between Tricky Trials and the pre-Bundles
1.21.2 / 1.21.3 patches. Bundles of Bravery (1.21.4) is the first patch to
introduce new plant blocks (PALE_OAK_SAPLING, CLOSED_EYEBLOSSOM,
OPEN_EYEBLOSSOM), which is why the 1.21.4 build target exists.

\*\* "Copper Age" (1.21.9 / 1.21.10) and "Mounts of Mayhem" (1.21.11) share
the same plant block roster per the Minecraft Wiki "Java Edition" patch notes.
No new placeable plant blocks were added across 1.21.9–1.21.11, so a single
1.21.11 jar serves all three.

### Why a separate 1.21.4 build?

The 1.21.5 ("Spring to Life") source tree references vanilla `Blocks` static
fields that DO NOT EXIST in 1.21.4's vanilla `Blocks` class:

- `Blocks.SHORT_DRY_GRASS`, `Blocks.TALL_DRY_GRASS`
- `Blocks.BUSH`, `Blocks.FIREFLY_BUSH`
- `Blocks.WILDFLOWERS`
- `Blocks.CACTUS_FLOWER`

If the 1.21.5 jar were declared as covering 1.21.4, Fabric Loader would
happily load the jar on 1.21.4 — and class initialisation of
`PlaceablePlants` would then throw `NoSuchFieldError: SHORT_DRY_GRASS` (or
one of the others) the moment any mixin called `Placeable.isDisabled` for
the first time. The mod would crash on every block-placement attempt.

The fix is structural: the 1.21.4 build target is its own Stonecutter
chiseled subproject. Its source tree is preprocessed with `//? if >=1.21.5`
guards stripped in PlaceablePlants.java, removing all six fields above while
preserving PALE_OAK_SAPLING / CLOSED_EYEBLOSSOM / OPEN_EYEBLOSSOM (which DO
exist in 1.21.4 — they shipped in Bundles of Bravery). The resulting jar's
`fabric.mod.json` declares `minecraft: >=1.21.4- <1.21.5`, so Loader will
refuse to load it on neighbouring patches and players see the correct file
on Modrinth / CurseForge.

### Boundary justification (per intermediate version)

| Patch version | Drop                     | New plant blocks vs. previous patch                  | Reason it maps to the listed jar                                                |
|---------------|--------------------------|------------------------------------------------------|---------------------------------------------------------------------------------|
| 1.21          | Tricky Trials            | (baseline of the 1.21 series)                        | Server-protocol-compatible with 1.21.1; same plant roster.                      |
| 1.21.2        | Bundles of Bravery (pre) | None of mod interest                                 | Stable plant API between 1.21.1 and 1.21.4.                                     |
| 1.21.3        | Bundles of Bravery (pre) | None of mod interest                                 | Same as 1.21.2.                                                                 |
| 1.21.4        | Bundles of Bravery       | PALE_OAK_SAPLING, CLOSED_EYEBLOSSOM, OPEN_EYEBLOSSOM | Has its own dedicated build target — see "Why a separate 1.21.4 build?" above.  |
| 1.21.6        | Chase the Skies          | None of mod interest                                 | "Happy Ghasts" — no plant additions per wiki.                                   |
| 1.21.7        | Chase the Skies (hotfix) | None                                                 | Hotfix release inside the same drop.                                            |
| 1.21.9        | Copper Age               | None of mod interest                                 | "Copper Age" — copper bulb, copper torches; no placeable plants added per wiki. |
| 1.21.10       | Copper Age (hotfix)      | None                                                 | Hotfix.                                                                         |

### Adding a new build target

If a future Mojang release introduces a new placeable plant block (or renames
an existing block class — see `BambooSaplingBlock` → `BambooShootBlock` in
1.21.5), the mod must add a NEW first-class Stonecutter target rather than
extend an existing jar's compat range. The procedure is:

1. Create `versions/<new-mc>/gradle.properties` (template: 1.21.5).
2. Add the version string to the `versions(...)` list in `settings.gradle.kts`.
3. Add a matrix entry to the `release.yml` `matrix.include` list, including
   the `game_versions:` comma-list of patch versions the new jar serves.
4. If the new MC version introduces a new plant or renames a class, follow
   the "Upgrade-new-MC-version checklist" below.

## Versions explicitly OUT OF SCOPE for this mod's lifecycle

No versions currently out of scope — 26.1.2, 26.2 and 26.3 were
successfully ported. Future year-based MC versions (26.4+) will be assessed
when released.

## Upgrade-new-MC-version checklist

Use this when adding a new MC version (call it `<MC>`) to the first-class
set. The order matters — earlier steps unblock later ones.

### Step 1 — Stonecutter / build infra

1. Add `<MC>` to the `versions(...)` list in `settings.gradle.kts` inside
   `stonecutter { create(rootProject) { versions(...) } }`.
2. Create `versions/<MC>/gradle.properties` with at least:
   ```
   minecraft_version=<MC>
   yarn_mappings=<MC>+build.<N>
   loader_version=<latest>
   fabric_version=<latest matching <MC>>
   ```
   Use `versions/1.21.5/gradle.properties` as a template.
3. Run `./gradlew "<MC>:build"` once. Capture the cannot-find-symbol errors.

### Step 2 — Plant audit

4. Extract the merged Loom jar's `Blocks.class`:
   ```bash
   jar=$(ls C:/Users/<you>/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/<MC>-*/*.jar | head -1)
   unzip -p "$jar" net/minecraft/block/Blocks.class > /tmp/Blocks_<MC>.class
   javap -p /tmp/Blocks_<MC>.class | grep -iE "PLANT|FLOWER|GRASS|SAPLING|FERN|BUSH|MUSHROOM|CROP|WART|ROOTS|SPROUTS|PETALS|FUNGUS|BERRY|POD|WILDFLOWER|VINE|LEAVES|LICHEN|EYEBLOSSOM|AZALEA|PROPAGULE" | sort -u
   ```
5. Cross-reference the output against `PlaceablePlants.java`. For each new vanilla
   plant block:
    - If it fits the mod's "place a plant on top of any block" mission, add an
      enum entry under `//? if >=<MC>`.
    - If it does NOT (carpet / hanging / underwater / ceiling-attached), add an
      "intentionally excluded" comment in the enum's javadoc with the rationale.

### Step 3 — Code-level conditioning

6. For every cannot-find-symbol from Step 1 step 3, wrap the offending source
   line with `//? if >=<MC>` (introducing the symbol) or `//? if <<MC>` (removing
   it on older versions). Stonecutter strips the line for non-matching targets.
7. For renamed classes (e.g., `BambooSaplingBlock` → `BambooShootBlock`),
   wrap both the import statement AND the `@Mixin(...)` target with `//? if`
   else-blocks.
8. For breaking method-signature changes within a class, wrap the affected
   method body with `//? if`. If the change is large, prefer adding two source
   files and conditioning the import — the Stonecutter docs suggest this
   pattern for any change >5 lines.

### Step 4 — Verification

9. Run `./gradlew chiseledBuild` and confirm all (now five+) versions PASS.
   If any FAIL, fix before merging.
10. Manually launch each version's `runClient` and place at least one of
    each plant category (sapling / flower / crop / sugar cane / cactus /
    mushroom) on a slab and on a leaves block.
11. Run the mushroom non-regression smoke test: place a brown mushroom on a
    stone slab in a dark cave; confirm it does NOT spread to surrounding
    slabs after random-tick.

### Step 5 — Documentation

12. Update this file's "Supported MC versions" table.
13. Bump `mod_version` in `gradle.properties` per CHANGELOG conventions.
14. Update `CHANGELOG.md` with the new MC target.

### Step 6 — Release

15. Tag the release: `git tag v<mod_version>`.
16. Push the tag; CI (`mc-publish` workflow) handles Modrinth, CurseForge, and
    GitHub Releases.

## Reference: 26.x / Mojmap migration (completed)

26.x support was added as a first-class Stonecutter target in the same
repository, using `//? if >=26` conditionals throughout the source tree.
Key changes made:

1. `versions/26.1.2/gradle.properties` — no `yarn_mappings` property triggers
   `loom.officialMojangMappings()` in `build.gradle.kts`.
2. All Yarn `net.minecraft.block.*` → Mojmap `net.minecraft.world.level.block.*`
   imports wrapped in `//? if >=26` conditionals.
3. Java 25 toolchain selected via `stonecutter.current.parsed matches ">=26"`.
4. `canPlaceAt` → `canSurvive`, `WorldView` → `LevelReader`, `ServerWorld` →
   `ServerLevel`, `Random` → `RandomSource` throughout all mixin files.
5. Mixin target for `AbstractBlockStateNaturalTickMixin` changed from
   `AbstractBlock$AbstractBlockState` to `BlockBehaviour$BlockStateBase`.

## Reference: 26.2 (completed)

26.2 was added as a first-class Stonecutter target with **zero source
changes**. Findings from the port:

1. Every `//? if >=26` conditional already written for 26.1.2 matches 26.2,
   so the chiseled 26.2 source tree is byte-identical to the 26.1.2 one.
2. Plant audit (`javap` diff of `net.minecraft.world.level.block.Blocks`
   between the 26.1.2 and 26.2 merged Loom jars) found **no** added, removed,
   or renamed plant blocks. The 26.2 `Blocks` churn is entirely non-plant:
   - added: `SULFUR*`, `CINNABAR*`, `POTENT_SULFUR`, `SULFUR_SPIKE`,
     `POLISHED_*`, `CHISELED_*` variants;
   - restructured: the per-colour fields (`WHITE_WOOL`, `RED_BED`, …) collapsed
     into `ColorCollection` accessors (`WOOL`, `BED`, `CARPET`, `BANNER`,
     `WALL_BANNER`, `CONCRETE`, `CONCRETE_POWDER`, `STAINED_GLASS`,
     `STAINED_GLASS_PANE`, `GLAZED_TERRACOTTA`, `DYED_TERRACOTTA`,
     `DYED_CANDLE`, `DYED_CANDLE_CAKE`, `DYED_SHULKER_BOX`), and the
     weathering-copper fields into `WeatheringCopperCollection`;
   - class churn: `PointedDripstoneBlock` → `SpeleothemBlock`,
     `WeatheringCopperBlocks` → `WeatheringCopperCollection`.
   The mod references none of these, so nothing needed conditioning.
3. **Fabric Loom had to be bumped 1.16.2 → 1.17.20** in
   `gradle/libs.versions.toml`. The 26.2 Fabric API / Cloth Config / ModMenu
   artifacts are built with Loom 1.17.13, and Loom fails configuration with
   `Mod was built with a newer version of Loom (1.17.13), you are using Loom
   (1.16.2)`. The bump is global (the catalog is shared by every target); all
   seven targets build and test green on 1.17.20.
4. The identity-mappings JAR trick is unchanged — `versions/26.2/` carries its
   own copy of `identity-mappings.jar`, as Loom resolves the file relative to
   each subproject directory.

## Reference: 26.3 (completed)

26.3 was added as a first-class Stonecutter target. Findings from the port:

1. Dependencies: Fabric Loader 0.19.5, Fabric API `0.162.0+26.3`, Cloth
   Config `26.3.159`, ModMenu `21.0.0`. Loom 1.17.20 still works — no plugin
   bump needed.
2. Plant audit (`javap` diff of `Blocks` and the `block` package between the
   26.2 and 26.3 merged Loom jars):
   - **added plants**: `POPLAR_SAPLING` (a plain `SaplingBlock` with
     `TreeGrower.POPLAR`, so `SaplingBlockMixin` covers it) and `RED_SHRUB`
     (a `BushBlock`, covered by `PlantBlockMixin` via `VegetationBlock`).
     Both are new `PlaceablePlants` entries gated on `//? if >=26.3`;
   - **excluded**: `SHELF_MUSHROOM` (`ShelfMushroomBlock extends
     HorizontalDirectionalBlock`) attaches to the side of a block, so the
     `isValidFloor` model does not apply;
   - **class churn**: `DirtPathBlock` → `PathBlock`, `RedStoneWireBlock` →
     `RedstoneWireBlock`. The mod only uses the `Blocks.DIRT_PATH` field,
     which is unchanged.
3. `BonemealableBlock.isValidBonemealTarget` / `performBonemeal` gained a
   trailing `BonemealSource` parameter. The mod hooks neither (it wraps
   `BoneMealItem.growCrop`, whose signature is unchanged), and every hooked
   method (`canSurvive`, `randomTick`, `tick`, `getStateForPlacement`) has
   an identical descriptor in 26.2 and 26.3.
4. `BlockTags.CONVERTABLE_TO_MUD` was renamed `CONVERTIBLE_TO_MUD`; the mod
   does not reference it.
