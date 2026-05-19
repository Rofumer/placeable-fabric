package com.wennest.placeable.mixin;

import com.wennest.placeable.Placeable;
//? if >=26 {
/*import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;*/
//?} else {
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PitcherCropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Targets {@link PitcherCropBlock#randomTick}. Cancels growth unless the
 * pitcher crop is rooted on farmland, keeping the vanilla
 * farming-on-farmland-only ecology rule intact even when the player places
 * the crop on a relaxed floor.
 *
 * <p>{@code PitcherCropBlock} chains through {@code TallPlantBlock.canPlaceAt}
 * (so {@link PlantBlockMixin} covers placement transitively) but provides
 * its own {@code randomTick} growth pipeline that needs an explicit floor
 * guard here.
 */
@Mixin(PitcherCropBlock.class)
public class PitcherCropBlockMixin {
    /**
     * Cancels growth when the pitcher crop is not on farmland, leaving
     * vanilla growth ecology untouched after player-relaxed placement.
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
        /*BlockState underBlockState = world.getBlockState(blockPos.below());
        if (underBlockState.is(Blocks.FARMLAND)) {*/
        //?} else {
        BlockState underBlockState = world.getBlockState(blockPos.down());
        if (underBlockState.isOf(Blocks.FARMLAND)) {
        //?}
            return;
        }

        ci.cancel();
    }
}
