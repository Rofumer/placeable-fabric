package com.wennest.placeable.mixin;

import com.wennest.placeable.Placeable;
//? if >=26 {
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
//?} else {
/*import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Relaxes the placement rule of vanilla {@link SugarCaneBlock} so the mod's
 * floor predicate (any block with a top rim, leaves, or dirt path) is
 * accepted in addition to vanilla's "dirt or sand adjacent to water" rule.
 *
 * <p>Defers to vanilla during worldgen and inside natural-tick frames so
 * vanilla feature placement and natural ecology spread are never widened.
 *
 * <p>Also prevents random-tick growth when the cane stack is rooted on a
 * non-vanilla floor (anything other than a block vanilla accepts for cane
 * placement, with adjacent water or frosted ice). This keeps mod-placed cane
 * decorative — it persists but never propagates upward into a stack that
 * would violate vanilla growth invariants.
 *
 * <p>For MC >=26, the vanilla-validity check is delegated to
 * {@code SugarCaneBlock.canSurvive} itself (called with natural-tick depth
 * incremented so our own {@code canSurvive} override bypasses). This avoids
 * reimplementing the soil+water rule with block tags whose contents may differ
 * across versions (e.g. {@code BlockTags.DIRT} no longer includes
 * {@code grass_block} or {@code mud} in 26.x).
 *
 * <p>{@code SugarCaneBlock} fully overrides {@code canPlaceAt} (no
 * super-call), so {@link PlantBlockMixin}'s transitive coverage does not
 * apply.
 */
@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {

    /**
     * HEAD-injected override of {@code SugarCaneBlock.canPlaceAt}. Standard
     * relaxed-floor shape.
     */
    //? if >=26 {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    public void placeable$canPlantAnywhere(BlockState state, LevelReader world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
    //?} else {
    /*@Inject(method = "canPlaceAt", at = @At("HEAD"), cancellable = true)
    public void placeable$canPlantAnywhere(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
    *///?}
        if (Placeable.shouldBypass(world, pos)) return;
        if (Placeable.isDisabled(state)) return;
        if (Placeable.isValidFloor(world, pos)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Keeps a mod-placed cane alive through its own scheduled survival tick.
     *
     * <p>Vanilla {@code updateShape} schedules a tick whenever
     * {@code canSurvive} fails, and {@code tick} then destroys the block if
     * {@code canSurvive} still fails. When cane growth is triggered from a
     * natural-tick bracket (e.g. bone meal made applicable to cane by another
     * mod, bracketed by {@link BoneMealItemMixin}), both calls see vanilla's
     * strict floor rule and the root cane broke itself. Survival of an
     * already-placed block is player intent, so the relaxed floor rule
     * applies here.
     */
    //? if >=26 {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void placeable$keepModPlacedCane(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "scheduledTick", at = @At("HEAD"), cancellable = true)
    public void placeable$keepModPlacedCane(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
    *///?}
        if (Placeable.isDisabled(state)) return;
        if (Placeable.isValidFloor(world, pos)) {
            ci.cancel();
        }
    }

    /**
     * Cancels growth when the cane stack is rooted on a non-vanilla floor.
     * Walks down the stack to find the actual ground block, then checks
     * whether that location would pass vanilla's own placement rule.
     *
     * <p>For MC >=26 the check is delegated to {@code canSurvive} on the
     * bottom cane so the soil+water logic stays in sync with vanilla and
     * does not break when block-tag contents change between versions.
     */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    //? if >=26 {
    public void randomTickMixin(BlockState blockState, ServerLevel world, BlockPos blockPos, RandomSource random, CallbackInfo ci) {
    //?} else {
    /*public void randomTickMixin(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random, CallbackInfo ci) {
    *///?}
        if (Placeable.isDisabled(blockState)) {
            return;
        }

        int i = 1;
        //? if >=26 {
        while (i < 3 && world.getBlockState(blockPos.below(i)).is(Blocks.SUGAR_CANE)) {
        //?} else {
        /*while (i < 3 && world.getBlockState(blockPos.down(i)).isOf(Blocks.SUGAR_CANE)) {
        *///?}
            ++i;
        }

        //? if >=26 {
        // Delegate vanilla-validity to canSurvive so we match whatever soil+water
        // rules vanilla enforces in this MC version without reimplementing them.
        // AbstractBlockStateNaturalTickMixin already set isNaturalTick() = true,
        // but we call enterNaturalTick() again for safety in case that wrap missed.
        BlockPos groundBlockPos = blockPos.below(i);
        boolean canGrow = false;
        Placeable.enterNaturalTick();
        try {
            BlockPos bottomCanePos = groundBlockPos.above();
            canGrow = world.getBlockState(bottomCanePos).canSurvive(world, bottomCanePos);
        } finally {
            Placeable.exitNaturalTick();
        }
        if (!canGrow) {
            ci.cancel();
        }
        //?} else {
        /*BlockPos groundBlockPos = blockPos.down(i);
        BlockState groundBlockState = world.getBlockState(groundBlockPos);
        if (!groundBlockState.isIn(BlockTags.DIRT) && !groundBlockState.isIn(BlockTags.SAND)) {
            ci.cancel();
            return;
        }

        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockState targetBlockState = world.getBlockState(groundBlockPos.offset(direction));
            FluidState targetFluidState = world.getFluidState(groundBlockPos.offset(direction));

            if (targetFluidState.isIn(FluidTags.WATER) || targetBlockState.isOf(Blocks.FROSTED_ICE)) {
                return;
            }
        }

        ci.cancel();
        *///?}
    }
}
