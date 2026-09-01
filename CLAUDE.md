# Placeable Plants — Claude Code Context

## Project overview

Fabric mod (Stonecutter multi-version) that relaxes plant placement rules so
players can place plants on top of any block with a top-rim, leaves, or dirt
path. Supports MC 1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.11, **26.1.2**, and
**26.2**. Source tree is unified; per-version differences use `//? if`
Stonecutter preprocessor comments. VCS reset version is **1.21.5**; the
current Stonecutter *active* version is **26.2** (`stonecutter.gradle.kts`).

## Key files

| Path | Purpose |
|------|---------|
| `build.gradle.kts` | Central build script (all versions share it) |
| `settings.gradle.kts` | Stonecutter version list & central-script declaration |
| `gradle/libs.versions.toml` | Plugin versions (fabric-loom, gson, lombok) |
| `versions/<mc>/gradle.properties` | Per-version coordinates |
| `src/main/java/com/wennest/placeable/Placeable.java` | Core logic, `isValidFloor`, `shouldBypass` |
| `src/main/java/com/wennest/placeable/PlaceablePlants.java` | Enum of placeable plants |
| `src/main/java/com/wennest/placeable/mixin/*.java` | All mixins |
| `docs/VERSIONS.md` | Supported-versions matrix, upgrade checklist |

## 26.1.2 port — status

The port is **~95% complete**. All Java source files have been updated with
`//? if >=26 { /*…*/ //?} else { … //?}` Stonecutter conditionals covering
Mojmap package renames, method renames, and API changes. The build system has
been updated to handle the non-obfuscated nature of Minecraft 26.x.

### What is DONE

1. **All mixin files** updated with `//? if >=26` conditionals:
   - Imports: `net.minecraft.block.*` → `net.minecraft.world.level.block.*`
   - Method renames: `canPlaceAt` → `canSurvive`, `scheduledTick` → `tick`,
     `getPlacementState` → `getStateForPlacement`
   - API: `pos.down()` → `pos.below()`, `pos.up()` → `pos.above()`,
     `pos.offset()` → `pos.relative()`, `isOf()` → `is()`, `isIn()` → `is()`
   - `BlockPos.ORIGIN` → `BlockPos.ZERO`
   - `getDefaultState()` → `defaultBlockState()`
   - `getWorld()` → `getLevel()`, `getBlockPos()` → `getClickedPos()`
   - `ServerWorld` → `ServerLevel`, `Random` → `RandomSource`
   - `WorldView` → `LevelReader`, `World` → `Level`
   - `ItemPlacementContext` → `BlockPlaceContext`
   - `BlockView` → `BlockGetter`, `EmptyBlockView` → `EmptyBlockGetter`
   - `Direction.Type.HORIZONTAL` → `Direction.Plane.HORIZONTAL`
   - `SideShapeType.RIGID` → `SupportType.FULL`
   - `isSideSolid(…)` → `isFaceSturdy(…)`
   - `isSolidBlock(…)` → `isSolidRender()`
   - `isOpaque()` → `canOcclude()`
   - `BlockPos.iterate(a, b)` → `BlockPos.betweenClosed(a, b)`
   - `world.isAir(pos)` → `world.isEmptyBlock(pos)`
   - `world.getBaseLightLevel(pos, 0)` → `world.getRawBrightness(pos, 0)`
   - `world.setBlockState(pos, state, 2)` → `world.setBlock(pos, state, 2)`
   - `Text` → `Component`, `I18n.translate` → `I18n.get`
   - `getTranslationKey()` → `getDescriptionId()`
   - Class renames in Mojmap:
     - `MushroomPlantBlock` → `MushroomBlock`
     - `PropaguleBlock` → `MangrovePropaguleBlock`
     - `AbstractBlock$AbstractBlockState` → `BlockBehaviour$BlockStateBase`
   - Registry tag packages: `net.minecraft.registry.tag.BlockTags` →
     `net.minecraft.tags.BlockTags`
   - `@WrapMethod(method = "scheduledTick")` → `"tick"` for >=26

2. **Test files** updated: `IsValidFloorTest`, `PlaceablePlantsTest`,
   `PlaceableTest`, `TestBootstrap` — all with `//? if >=26` conditionals.

3. **`build.gradle.kts`**:
   - Java toolchain: 25 for >=26, 21 otherwise
   - `mixin_java_level`: JAVA_25 for >=26
   - Mappings: **identity mapping JAR** for >=26 (see below)

4. **`versions/26.1.2/gradle.properties`** created with:
   ```
   minecraft=26.1.2
   fabric_loader=0.19.2
   fabric_api=0.149.1+26.1.2
   cloth_config=26.1.154
   modmenu=18.0.0-beta.1
   minecraft_dep=>=26.1.2- <26.2
   ```

5. **`versions/26.1.2/identity-mappings.jar`** — a minimal Tiny v2 JAR with
   just the header `tiny\t2\t0\tofficial\tnamed` (no actual mappings). Needed
   because Fabric Loom 1.16.2 (`fabric-loom` plugin) requires the `mappings`
   configuration to have at least one dependency even for non-obfuscated builds.
   The build script uses:
   ```kotlin
   loom.noIntermediateMappings()
   mappings(files("${projectDir}/identity-mappings.jar"))
   ```

6. **`docs/VERSIONS.md`** updated with 26.1.2 entry.

7. **Additional fixes applied during port** (discovered at runtime):
   - `BambooBlock` → `BambooStalkBlock`, `BambooShootBlock` → `BambooSaplingBlock`
   - `BlockTags.BAMBOO_PLANTABLE_ON` → `BlockTags.SUPPORTS_BAMBOO`
   - `BlockTags.MUSHROOM_GROW_BLOCK` → `BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT`
   - `PlantBlock` → `VegetationBlock` (PlantBlockMixin target for >=26)
   - `BoneMealItem.useOnGround` removed in 26.x — wrap suppressed for >=26
   - `BoneMealItem.useOnFertilizable` → `BoneMealItem.growCrop` in 26.x
   - `fabric.mod.json`: `"fabric": "*"` → `"fabric-api": "*"` (26.x dropped `provides: ["fabric"]`)
   - `TestBootstrap`: `SharedConstants.tryDetectVersion()` exists in 26.x, must be called before `Bootstrap.bootStrap()`
   - Test JVM args for >=26: `-Dfabric.runtimeMappingNamespace=named`, `-Dfabric.development=true`, `-Dfabric.remapClasspathFile=<empty>`, `-Dnet.bytebuddy.experimental=true`

---

## ✅ PORT IS COMPLETE

All 7 versions (1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.11, 26.1.2, 26.2) build
and pass all tests. `./gradlew chiseledBuild` succeeds without errors.

## 26.2 port — complete (branch `26.2`)

26.2 required **zero source changes**. Everything gated on `//? if >=26`
already matches 26.2, and a `javap` diff of `Blocks` between the 26.1.2 and
26.2 merged Loom jars showed no plant-block additions, removals, or renames.
26.2's `Blocks` churn is entirely non-plant: sulfur / cinnabar / speleothem
additions, `PointedDripstoneBlock` → `SpeleothemBlock`, and the collapse of
the per-colour and weathering-copper field families into `ColorCollection` /
`WeatheringCopperCollection`. The mod references none of them.

What the port did need:

- `versions/26.2/gradle.properties` — `fabric_loader=0.19.3`,
  `fabric_api=0.158.0+26.2`, `cloth_config=26.2.155`, `modmenu=20.0.1`,
  `minecraft_dep=>=26.2- <26.3`. No `yarn_mappings` (same as 26.1.2).
- `versions/26.2/identity-mappings.jar` — copied from 26.1.2; Loom resolves
  the path per-subproject so each 26.x target needs its own copy.
- `"26.2"` added to `versions(...)` in `settings.gradle.kts`.
- **Fabric Loom 1.16.2 → 1.17.20** in `gradle/libs.versions.toml`. Required:
  the 26.2 Fabric API / Cloth / ModMenu artifacts are built with Loom 1.17.13
  and 1.16.2 fails configuration with `Mod was built with a newer version of
  Loom (1.17.13)`. The bump is global; all seven targets stay green.
- CI: `26.2` added to the `release.yml` publish matrix and the `build.yml`
  test task list; the release workflow's JDK selector is now
  `startsWith(matrix.minecraft, '26.')` instead of an equality check on
  `26.1.2`.

### Remaining task — Runtime verification (Priority 4)

Run `./gradlew :26.1.2:runClient` / `:26.2:runClient` and manually test plant
placement in-game.
The `placeable.mixins.json` uses `${mixin_java_level}` substitution (JAVA_25
for >=26) which is verified at compile time; runtime sanity should confirm
the mod loads and plants can be placed on non-standard floors.

---

## Build system notes for 26.1.2

### Why identity-mappings.jar exists

Fabric Loom 1.16.2 (`fabric-loom` plugin, `LoomGradlePlugin`) requires the
`mappings` Gradle configuration to have ≥1 dependency. For MC 26.x (non-
obfuscated), there are no Mojang proguard mappings and no Fabric Intermediary.
The solution: a 484-byte JAR at `versions/26.1.2/identity-mappings.jar`
containing `mappings/mappings.tiny` with content:
```
tiny	2	0	official	named
```
This is an empty Tiny v2 file — no classes/methods/fields mapped, so every name
passes through unchanged (identity). Combined with `loom.noIntermediateMappings()`
it satisfies Loom without doing any actual remapping.

### Why NOT `net.fabricmc.fabric-loom` plugin

`net.fabricmc.fabric-loom` (`LoomNoRemapGradlePlugin`) literally just calls
`project.plugins.apply("fabric-loom")` — it IS `fabric-loom`. The fabric-
example-mod for 26.1.2 uses it with Loom `1.16-SNAPSHOT` (build date 2026-05-14,
after the 1.16.2 release) which may have added no-mappings support. Using the
snapshot in production is undesirable; the identity-JAR approach works with the
stable 1.16.2 release.

### Stonecutter conditionals pattern

VCS active version is 1.21.5, so `//? if >=26` blocks are wrapped in `/* */` in
the VCS tree and activated for the 26.1.2 chiseled subproject:

```java
//? if >=26 {
/*import net.minecraft.world.level.block.SomeBlock;*/  // inactive in VCS
//?} else {
import net.minecraft.block.SomeBlock;  // active in VCS
//?}
```

---

## Commit state

26.x work lives on branch `26.2`. Run `git diff --stat main` to see the
full list of modified files.
