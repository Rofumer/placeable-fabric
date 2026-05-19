package com.wennest.placeable.mixin;

import com.wennest.placeable.Placeable;
//? if >=26 {
/*import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;*/
//?} else {
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PropaguleBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
//?}
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Targets {@link PropaguleBlock#randomTick} (mangrove propagule). Cancels
 * growth unless the propagule is hanging or rooted on dirt-tagged soil that
 * is not dirt-path, preserving vanilla mangrove ecology even when the player
 * places the propagule on a relaxed floor.
 *
 * <p>Although {@link PropaguleBlock} extends {@link net.minecraft.block.SaplingBlock},
 * it <em>overrides</em> {@code randomTick} with hanging-aware logic that
 * does NOT super-call {@code SaplingBlock.randomTick}. Therefore
 * {@link SaplingBlockMixin}'s {@code randomTick} injection never fires for
 * propagules — a dedicated mixin is needed to keep the growth-prevention
 * guard active for both the hanging and planted phases.
 */
//? if >=26 {
/*@Mixin(MangrovePropaguleBlock.class)*/
//?} else {
@Mixin(PropaguleBlock.class)
//?}
public class PropaguleBlockMixin {

    @Shadow
    @Final
    public static BooleanProperty HANGING;

    /**
     * Cancels growth when the propagule is planted on a non-dirt floor.
     * Hanging propagules are allowed to mature normally (vanilla aging
     * pipeline), as is dirt-tagged soil except dirt-path.
     */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    //? if >=26 {
    /*public void placeable$randomTickMixin(BlockState blockState, ServerLevel world, BlockPos blockPos, RandomSource random, CallbackInfo ci) {*/
    //?} else {
    public void placeable$randomTickMixin(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random, CallbackInfo ci) {
    //?}
        if (Placeable.isDisabled(blockState)) {
            return;
        }

        //? if >=26 {
        /*if (blockState.getValue(HANGING)) {*/
        //?} else {
        if (blockState.get(HANGING)) {
        //?}
            return;
        }

        //? if >=26 {
        /*BlockState underBlockState = world.getBlockState(blockPos.below());
        if (underBlockState.is(BlockTags.DIRT) && !underBlockState.is(Blocks.DIRT_PATH)) {*/
        //?} else {
        BlockState underBlockState = world.getBlockState(blockPos.down());
        if (underBlockState.isIn(BlockTags.DIRT) && !underBlockState.isOf(Blocks.DIRT_PATH)) {
        //?}
            return;
        }

        ci.cancel();
    }
}
