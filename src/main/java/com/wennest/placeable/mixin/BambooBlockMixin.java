package com.wennest.placeable.mixin;

import com.wennest.placeable.HangingBamboo;
import com.wennest.placeable.Placeable;
//? if >=26 {
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
//?} else {
/*import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
*///?}
//? if >=26.3 {
import net.minecraft.world.level.block.BonemealSource;
//?}
//? if >=1.21.2 && <26 {
/*import net.minecraft.world.tick.ScheduledTickView;
*///?} else if <1.21.2 {
/*import net.minecraft.world.WorldAccess;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Relaxes the placement rule of vanilla {@link BambooBlock} so the mod's
 * floor predicate (any block with a top rim, leaves, or dirt path) is
 * accepted in addition to the {@link BlockTags#BAMBOO_PLANTABLE_ON} floors
 * vanilla allows.
 *
 * <p>Defers to vanilla during worldgen and inside natural-tick frames so
 * vanilla feature placement and natural ecology spread are never widened.
 *
 * <p>Also guards bamboo growth so a stalk placed on a non-bamboo-plantable
 * floor (e.g., cobblestone) does not silently propagate upward via random
 * tick, and substitutes a bamboo-shoot placement state when the player would
 * place regular bamboo on a mod-relaxed floor — mirroring vanilla's
 * shoot-first-then-stalk behavior on the wider floor set.
 */
//? if >=26 {
@Mixin(BambooStalkBlock.class)
//?} else {
/*@Mixin(BambooBlock.class)
*///?}
public class BambooBlockMixin {

    /**
     * Centralized reference to the bamboo "shoot" / "sapling" block. Used by
     * both growth-prevention (via random-tick cancellation keyed on this
     * block's enable flag) and the placement-state substitution.
     *
     * <p>The vanilla field is still named {@code BAMBOO_SAPLING} on every
     * supported MC version (the shoot rename in 1.21.5 changed the
     * <em>class</em> {@code BambooSaplingBlock -> BambooShootBlock}, not the
     * registry / Blocks-class field).
     */
    @Unique
    private static final Block BAMBOO_KEY = Blocks.BAMBOO_SAPLING;

    /**
     * HEAD-injected override of {@code BambooBlock.canPlaceAt}. Standard
     * relaxed-floor shape.
     */
    //? if >=26 {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    public void placeable$canPlantAnywhere(BlockState state, LevelReader world, BlockPos pos,
    //?} else {
    /*@Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    public void placeable$canPlantAnywhere(BlockState state, WorldView world, BlockPos pos,
    *///?}
                                           CallbackInfoReturnable<Boolean> cir) {
        // Defer to vanilla during worldgen and inside natural-tick frames.
        if (Placeable.shouldBypass(world, pos)) return;
        if (Placeable.isDisabled(state)) return;
        if (Placeable.isValidFloor(world, pos)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Survival rule for a column with no floor: it lives while it hangs from
     * a valid ceiling (see {@link HangingBamboo}). Vanilla would keep every
     * segment above a bamboo block alive, so without this a column whose
     * ceiling is removed would float.
     *
     * <p>Unlike the relaxed-floor rule this one is NOT bypassed inside
     * natural-tick frames: bamboo has no natural spread that consults
     * {@code canSurvive}, and the hanging column's own scheduled survival
     * tick and growth run inside those frames. Worldgen is still left to
     * vanilla.
     */
    //? if >=26 {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    public void placeable$canHang(BlockState state, LevelReader world, BlockPos pos,
    //?} else {
    /*@Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    public void placeable$canHang(BlockState state, WorldView world, BlockPos pos,
    *///?}
                                  CallbackInfoReturnable<Boolean> cir) {
        if (!HangingBamboo.isRuntime(world)) return;
        if (!HangingBamboo.isEnabled()) return;
        if (HangingBamboo.hasNoFloor(world, pos)) {
            cir.setReturnValue(HangingBamboo.isCeilingSupported(world, pos));
        }
    }

    /**
     * Keeps a hanging segment's leaves and thickness in step with the column
     * as segments are added or removed under it (see
     * {@link HangingBamboo#reshape}).
     */
    //? if >=26 {
    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    public void placeable$reshapeHanging(BlockState state, LevelReader world, ScheduledTickAccess ticks,
                                         BlockPos pos, Direction direction, BlockPos neighborPos,
                                         BlockState neighborState, RandomSource random,
                                         CallbackInfoReturnable<BlockState> cir) {
    //?} else if >=1.21.2 {
    /*@Inject(method = "getStateForNeighborUpdate", at = @At("RETURN"), cancellable = true)
    public void placeable$reshapeHanging(BlockState state, WorldView world, ScheduledTickView tickView,
                                         BlockPos pos, Direction direction, BlockPos neighborPos,
                                         BlockState neighborState, Random random,
                                         CallbackInfoReturnable<BlockState> cir) {
    *///?} else {
    /*@Inject(method = "getStateForNeighborUpdate", at = @At("RETURN"), cancellable = true)
    public void placeable$reshapeHanging(BlockState state, Direction direction, BlockState neighborState,
                                         WorldAccess world, BlockPos pos, BlockPos neighborPos,
                                         CallbackInfoReturnable<BlockState> cir) {
    *///?}
        BlockState result = cir.getReturnValue();
        //? if >=26 {
        if (!result.is(Blocks.BAMBOO)) return;
        //?} else {
        /*if (!result.isOf(Blocks.BAMBOO)) return;
        *///?}
        if (!HangingBamboo.isRuntime(world)) return;
        if (!HangingBamboo.isEnabled()) return;
        if (!HangingBamboo.isHanging(world, pos)) return;
        cir.setReturnValue(HangingBamboo.reshape(result, world, pos, direction, neighborState));
    }

    /**
     * Bone meal on a hanging column grows it downward when
     * {@code hangingBambooGrowth} is on, and does nothing otherwise.
     */
    //? if >=26.3 {
    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), cancellable = true)
    public void placeable$canBonemealHanging(LevelReader world, BlockPos pos, BlockState state,
                                             BonemealSource source, CallbackInfoReturnable<Boolean> cir) {
    //?} else if >=26 {
    /*@Inject(method = "isValidBonemealTarget", at = @At("HEAD"), cancellable = true)
    public void placeable$canBonemealHanging(LevelReader world, BlockPos pos, BlockState state,
                                             CallbackInfoReturnable<Boolean> cir) {
    *///?} else {
    /*@Inject(method = "isFertilizable", at = @At("HEAD"), cancellable = true)
    public void placeable$canBonemealHanging(WorldView world, BlockPos pos, BlockState state,
                                             CallbackInfoReturnable<Boolean> cir) {
    *///?}
        if (!HangingBamboo.isEnabled()) return;
        if (!HangingBamboo.hasNoFloor(world, pos)) return;
        cir.setReturnValue(HangingBamboo.isGrowthEnabled()
                && HangingBamboo.isCeilingSupported(world, pos)
                && HangingBamboo.canBonemeal(world, pos));
    }

    //? if >=26.3 {
    @Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    public void placeable$bonemealHanging(ServerLevel world, RandomSource random, BlockPos pos, BlockState state,
                                          BonemealSource source, CallbackInfo ci) {
    //?} else if >=26 {
    /*@Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    public void placeable$bonemealHanging(ServerLevel world, RandomSource random, BlockPos pos, BlockState state,
                                          CallbackInfo ci) {
    *///?} else {
    /*@Inject(method = "grow", at = @At("HEAD"), cancellable = true)
    public void placeable$bonemealHanging(ServerWorld world, Random random, BlockPos pos, BlockState state,
                                          CallbackInfo ci) {
    *///?}
        if (!HangingBamboo.isEnabled()) return;
        if (!HangingBamboo.hasNoFloor(world, pos)) return;
        if (HangingBamboo.isGrowthEnabled() && HangingBamboo.isCeilingSupported(world, pos)) {
            HangingBamboo.bonemeal(world, random, pos);
        }
        ci.cancel();
    }

    /**
     * Keeps a mod-placed stalk alive through its own scheduled survival tick.
     *
     * <p>Vanilla {@code updateShape} schedules a tick whenever
     * {@code canSurvive} fails, and {@code tick} then destroys the block if
     * {@code canSurvive} still fails. Bone meal growth runs inside the
     * {@link BoneMealItemMixin} natural-tick bracket, so the root stalk's
     * {@code updateShape} (fired when a segment is added above it) and the
     * resulting {@code tick} (wrapped by
     * {@link AbstractBlockStateNaturalTickMixin}) both see vanilla's strict
     * floor rule — and a bone-mealed stalk on e.g. a slab broke itself.
     * Survival of an already-placed block is player intent, not natural
     * spread, so the relaxed floor rule applies here.
     */
    //? if >=26 {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void placeable$keepModPlacedStalk(BlockState state, ServerLevel world, BlockPos pos,
                                             RandomSource random, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    public void placeable$keepModPlacedStalk(BlockState state, ServerWorld world, BlockPos pos,
                                             Random random, CallbackInfo ci) {
    *///?}
        if (Placeable.isDisabled(BAMBOO_KEY)) return;
        // A floorless column is judged by placeable$canHang, which vanilla's
        // tick consults; with placedWithoutTopRim the bamboo segment below
        // would otherwise count as a relaxed floor and keep a column whose
        // ceiling is gone.
        if (HangingBamboo.isEnabled() && HangingBamboo.hasNoFloor(world, pos)) return;
        if (Placeable.isValidFloor(world, pos)) {
            ci.cancel();
        }
    }

    /**
     * Cancels the bamboo random-tick growth path when the stack root rests on
     * a non-{@link BlockTags#BAMBOO_PLANTABLE_ON} floor. Without this guard a
     * player-placed bamboo on cobblestone would still grow upward into an
     * indefinitely tall stalk (vanilla never validates the root after the
     * initial placement).
     *
     * <p>The enable check keys on {@link #BAMBOO_KEY} (the shoot block), not
     * on {@code state}'s block, because the user-facing config toggle is
     * "BAMBOO" mapped to the shoot block in
     * {@link com.wennest.placeable.PlaceablePlants}.
     */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    //? if >=26 {
    public void placeable$randomTickMixin(BlockState state, ServerLevel world, BlockPos pos,
                                          RandomSource random, CallbackInfo ci) {
    //?} else {
    /*public void placeable$randomTickMixin(BlockState state, ServerWorld world, BlockPos pos,
                                          Random random, CallbackInfo ci) {
    *///?}
        if (Placeable.isDisabled(BAMBOO_KEY)) {
            return;
        }

        // Hanging columns grow downward instead, and only when enabled; the
        // root walk below cancels their vanilla (upward) growth otherwise.
        if (HangingBamboo.isGrowthEnabled() && HangingBamboo.isHanging(world, pos)) {
            HangingBamboo.randomGrow(state, world, pos, random);
            ci.cancel();
            return;
        }

        // Walk down to the root of the stack to find the actual floor.
        int i = 1;
        //? if >=26 {
        while (world.getBlockState(pos.below(i)).is(Blocks.BAMBOO)) {
        //?} else {
        /*while (world.getBlockState(pos.down(i)).isOf(Blocks.BAMBOO)) {
        *///?}
            i++;
        }

        //? if >=26 {
        BlockState floor = world.getBlockState(pos.below(i));
        if (!floor.is(BlockTags.SUPPORTS_BAMBOO)) {
        //?} else {
        /*BlockState floor = world.getBlockState(pos.down(i));
        if (!floor.isIn(BlockTags.BAMBOO_PLANTABLE_ON)) {
        *///?}
            // Cancel vanilla's growth body — this stalk was placed on a
            // mod-relaxed floor and must NOT propagate upward.
            ci.cancel();
        }
    }

    /**
     * Substitutes a bamboo-shoot placement state when the player would place
     * regular bamboo on a mod-relaxed (non-bamboo-plantable) floor; mirrors
     * vanilla's shoot-first-then-stalk behavior on the wider floor set.
     *
     * <p>Injected at {@code RETURN} (not {@code TAIL}) with a null-check, so
     * this only substitutes when no prior return value is set; it never
     * stomps another mixin's earlier decision.
     */
    //? if >=26 {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    public void placeable$getPlacementStateMixin(BlockPlaceContext ctx,
    //?} else {
    /*@Inject(method = "getPlacementState", at = @At("RETURN"), cancellable = true)
    public void placeable$getPlacementStateMixin(ItemPlacementContext ctx,
    *///?}
                                                 CallbackInfoReturnable<BlockState> cir) {
        // Do not stomp another mixin's / vanilla's existing placement.
        if (cir.getReturnValue() != null) return;
        // Defer to vanilla during worldgen and inside natural-tick frames.
        //? if >=26 {
        if (Placeable.shouldBypass(ctx.getLevel(), ctx.getClickedPos())) return;
        if (Placeable.isDisabled(BAMBOO_KEY)) return;

        if (Placeable.isValidFloor(ctx.getLevel(), ctx.getClickedPos())) {
            cir.setReturnValue(BAMBOO_KEY.defaultBlockState());
        } else if (HangingBamboo.isEnabled()
                && ctx.getLevel().getFluidState(ctx.getClickedPos()).isEmpty()
                && HangingBamboo.isCeilingSupported(ctx.getLevel(), ctx.getClickedPos())) {
            // No floor, but a ceiling (or a hanging column) above: hang a
            // stalk. Hanging columns skip the shoot stage.
            cir.setReturnValue(HangingBamboo.placementState(ctx.getLevel(), ctx.getClickedPos()));
        //?} else {
        /*if (Placeable.shouldBypass(ctx.getWorld(), ctx.getBlockPos())) return;
        if (Placeable.isDisabled(BAMBOO_KEY)) return;

        if (Placeable.isValidFloor(ctx.getWorld(), ctx.getBlockPos())) {
            cir.setReturnValue(BAMBOO_KEY.getDefaultState());
        } else if (HangingBamboo.isEnabled()
                && ctx.getWorld().getFluidState(ctx.getBlockPos()).isEmpty()
                && HangingBamboo.isCeilingSupported(ctx.getWorld(), ctx.getBlockPos())) {
            cir.setReturnValue(HangingBamboo.placementState(ctx.getWorld(), ctx.getBlockPos()));
        *///?}
        }
    }
}
