package com.wennest.placeable.mixin;

import com.wennest.placeable.Placeable;
//? if >=26 {
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
//?} else {
/*import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Targets {@link NetherWartBlock#randomTick}. Cancels growth unless the wart
 * is rooted on soul sand, preserving vanilla nether-wart ecology even when
 * the player has placed the wart on a relaxed floor.
 *
 * <p>{@code NetherWartBlock} owns its own {@code randomTick} growth logic;
 * relaxing only {@code canPlaceAt} via {@link PlantBlockMixin} would let
 * wart mature anywhere.
 */
@Mixin(NetherWartBlock.class)
public class NetherWartBlockMixin {
    /**
     * Cancels growth when the wart is not on soul sand, keeping vanilla
     * ecology untouched after player-relaxed placement.
     */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    //? if >=26 {
    public void placeable$randomTickMixin(BlockState blockState, ServerLevel world, BlockPos blockPos, RandomSource random, CallbackInfo ci) {
    //?} else {
    /*public void placeable$randomTickMixin(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random, CallbackInfo ci) {
    *///?}
        if (Placeable.isDisabled(blockState)) {
            return;
        }

        //? if >=26 {
        BlockState underBlockState = world.getBlockState(blockPos.below());
        if (underBlockState.is(Blocks.SOUL_SAND)) {
        //?} else {
        /*BlockState underBlockState = world.getBlockState(blockPos.down());
        if (underBlockState.isOf(Blocks.SOUL_SAND)) {
        *///?}
            return;
        }

        ci.cancel();
    }
}
