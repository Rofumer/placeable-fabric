package com.wennest.placeable;

//? if >=26 {
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
//?} else {
/*import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
*///?}

/**
 * Rules for bamboo hanging from the underside of a block.
 *
 * <p>Vanilla bamboo is supported bottom-up: each segment survives while the
 * block below is in {@link BlockTags#SUPPORTS_BAMBOO} (bamboo included). A
 * hanging column is supported top-down instead. A column counts as
 * <em>hanging</em> when the block under its lowest segment would not hold a
 * standing column (neither vanilla's tag nor the mod's relaxed floor), and
 * the block over its highest segment is a valid ceiling.
 *
 * <p>The state of a hanging column is derived from the blocks around it on
 * every check; no block state property marks a segment as hanging. A
 * standing column that touches a ceiling therefore keeps the vanilla rules
 * until its floor is removed, at which point it hangs.
 *
 * <p>Growth mirrors vanilla {@code BambooStalkBlock.growBamboo} with the
 * column flipped: new segments are added under the tip, thickness spreads
 * from the tip towards the ceiling, and the same height / stage limits
 * apply.
 */
public final class HangingBamboo {

    /** Vanilla's {@code BambooStalkBlock.MAX_HEIGHT}. */
    public static final int MAX_HEIGHT = 16;

    /**
     * Upper bound on any column walk. Player-stacked bamboo is not capped at
     * {@link #MAX_HEIGHT}, so walks stop at the tallest possible world.
     */
    private static final int MAX_SCAN = 4096;

    private HangingBamboo() {
    }

    /**
     * Hanging bamboo is on when the mod, the bamboo entry and the
     * {@link PlaceableConfig#hangingBamboo} toggle are all enabled.
     */
    public static boolean isEnabled() {
        PlaceableConfig config = Placeable.getConfig();
        return config != null && config.hangingBamboo
                && !Placeable.isDisabled(Blocks.BAMBOO_SAPLING);
    }

    /** {@link #isEnabled()} plus the {@link PlaceableConfig#hangingBambooGrowth} toggle. */
    public static boolean isGrowthEnabled() {
        PlaceableConfig config = Placeable.getConfig();
        return isEnabled() && config.hangingBambooGrowth;
    }

    /**
     * Hanging-bamboo rules apply only to runtime worlds; worldgen (a
     * {@code ChunkRegion}) keeps vanilla bamboo untouched.
     */
    //? if >=26 {
    public static boolean isRuntime(LevelReader world) {
        return world instanceof Level;
    }
    //?} else {
    /*public static boolean isRuntime(WorldView world) {
        return world instanceof World;
    }
    *///?}

    /**
     * Whether the block at {@code ceilingPos} can hold a hanging column.
     * Mirrors {@code Placeable.isValidFloor} for the bottom face: a centre support (like a hanging lantern needs),
     * or leaves, or any non-air block when
     * {@link PlaceableConfig#placedWithoutTopRim} is on.
     */
    //? if >=26 {
    public static boolean isValidCeiling(LevelReader world, BlockPos ceilingPos) {
        BlockState ceiling = world.getBlockState(ceilingPos);
        if (ceiling.isAir() || ceiling.is(Blocks.BAMBOO)) return false;
        PlaceableConfig config = Placeable.getConfig();
        boolean withoutTopRim = config != null && config.placedWithoutTopRim;
        return withoutTopRim
                || Block.canSupportCenter(world, ceilingPos, Direction.DOWN)
                || ceiling.is(BlockTags.LEAVES);
    }
    //?} else {
    /*public static boolean isValidCeiling(WorldView world, BlockPos ceilingPos) {
        BlockState ceiling = world.getBlockState(ceilingPos);
        if (ceiling.isAir() || ceiling.isOf(Blocks.BAMBOO)) return false;
        PlaceableConfig config = Placeable.getConfig();
        boolean withoutTopRim = config != null && config.placedWithoutTopRim;
        return withoutTopRim
                || Block.sideCoversSmallSquare(world, ceilingPos, Direction.DOWN)
                || ceiling.isIn(BlockTags.LEAVES);
    }
    *///?}

    /**
     * Whether the bamboo segments directly above {@code pos} (zero or more)
     * end at a valid ceiling. {@code pos} itself need not be bamboo yet, so
     * this also answers "can a hanging segment be placed here?".
     */
    //? if >=26 {
    public static boolean isCeilingSupported(LevelReader world, BlockPos pos) {
        return isValidCeiling(world, pos.above(countAbove(world, pos, MAX_SCAN) + 1));
    }
    //?} else {
    /*public static boolean isCeilingSupported(WorldView world, BlockPos pos) {
        return isValidCeiling(world, pos.up(countAbove(world, pos, MAX_SCAN) + 1));
    }
    *///?}

    /**
     * Whether the column containing {@code pos} lacks a standing support:
     * the block under its lowest segment is neither in
     * {@link BlockTags#SUPPORTS_BAMBOO} nor a relaxed floor.
     */
    //? if >=26 {
    public static boolean hasNoFloor(LevelReader world, BlockPos pos) {
        BlockPos floorPos = pos.below(countBelow(world, pos, MAX_SCAN) + 1);
        BlockState floor = world.getBlockState(floorPos);
        return !floor.is(BlockTags.SUPPORTS_BAMBOO)
                && !Placeable.isValidFloor(floor, world, floorPos);
    }
    //?} else {
    /*public static boolean hasNoFloor(WorldView world, BlockPos pos) {
        BlockPos floorPos = pos.down(countBelow(world, pos, MAX_SCAN) + 1);
        BlockState floor = world.getBlockState(floorPos);
        return !floor.isIn(BlockTags.BAMBOO_PLANTABLE_ON)
                && !Placeable.isValidFloor(floor, world, floorPos);
    }
    *///?}

    /** A column with no floor that is held by a ceiling. */
    //? if >=26 {
    public static boolean isHanging(LevelReader world, BlockPos pos) {
    //?} else {
    /*public static boolean isHanging(WorldView world, BlockPos pos) {
    *///?}
        return hasNoFloor(world, pos) && isCeilingSupported(world, pos);
    }

    /** Number of consecutive bamboo stalk segments directly above {@code pos}, capped. */
    //? if >=26 {
    public static int countAbove(LevelReader world, BlockPos pos, int cap) {
        int n = 0;
        while (n < cap && world.getBlockState(pos.above(n + 1)).is(Blocks.BAMBOO)) n++;
        return n;
    }
    //?} else {
    /*public static int countAbove(WorldView world, BlockPos pos, int cap) {
        int n = 0;
        while (n < cap && world.getBlockState(pos.up(n + 1)).isOf(Blocks.BAMBOO)) n++;
        return n;
    }
    *///?}

    /** Number of consecutive bamboo stalk segments directly below {@code pos}, capped. */
    //? if >=26 {
    public static int countBelow(LevelReader world, BlockPos pos, int cap) {
        int n = 0;
        while (n < cap && world.getBlockState(pos.below(n + 1)).is(Blocks.BAMBOO)) n++;
        return n;
    }
    //?} else {
    /*public static int countBelow(WorldView world, BlockPos pos, int cap) {
        int n = 0;
        while (n < cap && world.getBlockState(pos.down(n + 1)).isOf(Blocks.BAMBOO)) n++;
        return n;
    }
    *///?}

    /**
     * Leaves for a hanging segment with {@code segmentsBelow} bamboo segments
     * under it, under the configured {@link HangingBambooLeaves} mode.
     */
    public static BambooLeaves leavesFor(int segmentsBelow) {
        PlaceableConfig config = Placeable.getConfig();
        HangingBambooLeaves mode = config == null || config.hangingBambooLeaves == null
                ? HangingBambooLeaves.FULL
                : config.hangingBambooLeaves;
        return switch (mode) {
            case FULL -> BambooLeaves.LARGE;
            case NONE -> BambooLeaves.NONE;
            case MIRRORED -> segmentsBelow == 0 ? BambooLeaves.LARGE
                    : segmentsBelow == 1 ? BambooLeaves.SMALL
                    : BambooLeaves.NONE;
        };
    }

    /**
     * State for a player-placed hanging segment at {@code pos}: a new tip,
     * as thick as the segment above it.
     */
    //? if >=26 {
    public static BlockState placementState(LevelReader world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.above());
        int age = above.is(Blocks.BAMBOO) ? above.getValue(BambooStalkBlock.AGE) : 0;
        return Blocks.BAMBOO.defaultBlockState()
                .setValue(BambooStalkBlock.AGE, age)
                .setValue(BambooStalkBlock.LEAVES, leavesFor(0));
    }
    //?} else {
    /*public static BlockState placementState(WorldView world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.up());
        int age = above.isOf(Blocks.BAMBOO) ? above.get(BambooBlock.AGE) : 0;
        return Blocks.BAMBOO.getDefaultState()
                .with(BambooBlock.AGE, age)
                .with(BambooBlock.LEAVES, leavesFor(0));
    }
    *///?}

    /**
     * Re-derives a hanging segment's shape-dependent properties after a
     * neighbour change: the leaves for its position in the column, and
     * thickness spreading up from a thicker segment below (vanilla spreads
     * it down from above).
     */
    //? if >=26 {
    public static BlockState reshape(BlockState state, LevelReader world, BlockPos pos,
                                     Direction direction, BlockState neighborState) {
        BlockState out = state.setValue(BambooStalkBlock.LEAVES, leavesFor(countBelow(world, pos, 2)));
        if (direction == Direction.DOWN && neighborState.is(Blocks.BAMBOO)
                && neighborState.getValue(BambooStalkBlock.AGE) > out.getValue(BambooStalkBlock.AGE)) {
            out = out.cycle(BambooStalkBlock.AGE);
        }
        return out;
    }
    //?} else {
    /*public static BlockState reshape(BlockState state, WorldView world, BlockPos pos,
                                     Direction direction, BlockState neighborState) {
        BlockState out = state.with(BambooBlock.LEAVES, leavesFor(countBelow(world, pos, 2)));
        if (direction == Direction.DOWN && neighborState.isOf(Blocks.BAMBOO)
                && neighborState.get(BambooBlock.AGE) > out.get(BambooBlock.AGE)) {
            out = out.cycle(BambooBlock.AGE);
        }
        return out;
    }
    *///?}

    /**
     * Random-tick growth of a hanging column, mirroring vanilla
     * {@code BambooStalkBlock.randomTick}: only the tip grows, one time in
     * three, into an empty block lit to at least 9.
     */
    //? if >=26 {
    public static void randomGrow(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (state.getValue(BambooStalkBlock.STAGE) != 0) return;
        if (random.nextInt(3) != 0) return;
        BlockPos growthPos = pos.below();
        if (!world.isEmptyBlock(growthPos) || world.getRawBrightness(growthPos, 0) < 9) return;
    //?} else {
    /*public static void randomGrow(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(BambooBlock.STAGE) != 0) return;
        if (random.nextInt(3) != 0) return;
        BlockPos growthPos = pos.down();
        if (!world.isAir(growthPos) || world.getBaseLightLevel(growthPos, 0) < 9) return;
    *///?}
        int height = countAbove(world, pos, MAX_HEIGHT) + 1;
        if (height < MAX_HEIGHT) {
            growDown(state, world, pos, random, height);
        }
    }

    /**
     * Whether bone meal can grow the hanging column containing {@code pos}:
     * mirrors vanilla {@code isValidBonemealTarget} with the tip at the
     * bottom.
     */
    //? if >=26 {
    public static boolean canBonemeal(LevelReader world, BlockPos pos) {
        int below = countBelow(world, pos, MAX_HEIGHT);
        int above = countAbove(world, pos, MAX_HEIGHT);
        BlockPos tipPos = pos.below(below);
        BlockPos growthPos = tipPos.below();
        return above + below + 1 < MAX_HEIGHT
                && world.getBlockState(tipPos).getValue(BambooStalkBlock.STAGE) != 1
                && world.isInsideBuildHeight(growthPos)
                && world.isEmptyBlock(growthPos);
    }
    //?} else {
    /*public static boolean canBonemeal(WorldView world, BlockPos pos) {
        int below = countBelow(world, pos, MAX_HEIGHT);
        int above = countAbove(world, pos, MAX_HEIGHT);
        BlockPos tipPos = pos.down(below);
        BlockPos growthPos = tipPos.down();
        return above + below + 1 < MAX_HEIGHT
                && world.getBlockState(tipPos).get(BambooBlock.STAGE) != 1
                && !world.isOutOfHeightLimit(growthPos)
                && world.isAir(growthPos);
    }
    *///?}

    /**
     * Bone-meal growth of a hanging column, mirroring vanilla
     * {@code performBonemeal}: one or two new segments under the tip.
     */
    //? if >=26 {
    public static void bonemeal(ServerLevel world, RandomSource random, BlockPos pos) {
    //?} else {
    /*public static void bonemeal(ServerWorld world, Random random, BlockPos pos) {
    *///?}
        int below = countBelow(world, pos, MAX_HEIGHT);
        int total = below + countAbove(world, pos, MAX_HEIGHT) + 1;
        int newSegments = 1 + random.nextInt(2);
        for (int i = 0; i < newSegments; i++) {
            //? if >=26 {
            BlockPos tipPos = pos.below(below);
            BlockState tipState = world.getBlockState(tipPos);
            BlockPos growthPos = tipPos.below();
            if (total >= MAX_HEIGHT || tipState.getValue(BambooStalkBlock.STAGE) == 1
                    || !world.isEmptyBlock(growthPos) || world.isOutsideBuildHeight(growthPos)) {
                return;
            }
            //?} else {
            /*BlockPos tipPos = pos.down(below);
            BlockState tipState = world.getBlockState(tipPos);
            BlockPos growthPos = tipPos.down();
            if (total >= MAX_HEIGHT || tipState.get(BambooBlock.STAGE) == 1
                    || !world.isAir(growthPos) || world.isOutOfHeightLimit(growthPos)) {
                return;
            }
            *///?}
            growDown(tipState, world, tipPos, random, total);
            below++;
            total++;
        }
    }

    /**
     * Adds one segment under the tip at {@code tipPos}. Mirror of vanilla
     * {@code growBamboo}: the new segment is thick once the tip is thick or
     * the column is at least three segments long, and may stop growing past
     * height 11. Leaves on the segments above are re-laid out by their own
     * shape update (see {@link #reshape}).
     */
    //? if >=26 {
    private static void growDown(BlockState tipState, ServerLevel world, BlockPos tipPos,
                                 RandomSource random, int height) {
        int age = tipState.getValue(BambooStalkBlock.AGE) == 1
                || world.getBlockState(tipPos.above(2)).is(Blocks.BAMBOO) ? 1 : 0;
        int stage = (height >= 11 && random.nextFloat() < 0.25F) || height == 15 ? 1 : 0;
        world.setBlockAndUpdate(tipPos.below(), Blocks.BAMBOO.defaultBlockState()
                .setValue(BambooStalkBlock.AGE, age)
                .setValue(BambooStalkBlock.LEAVES, leavesFor(0))
                .setValue(BambooStalkBlock.STAGE, stage));
    }
    //?} else {
    /*private static void growDown(BlockState tipState, ServerWorld world, BlockPos tipPos,
                                 Random random, int height) {
        int age = tipState.get(BambooBlock.AGE) == 1
                || world.getBlockState(tipPos.up(2)).isOf(Blocks.BAMBOO) ? 1 : 0;
        int stage = (height >= 11 && random.nextFloat() < 0.25F) || height == 15 ? 1 : 0;
        world.setBlockState(tipPos.down(), Blocks.BAMBOO.getDefaultState()
                .with(BambooBlock.AGE, age)
                .with(BambooBlock.LEAVES, leavesFor(0))
                .with(BambooBlock.STAGE, stage));
    }
    *///?}
}
