package com.wennest.placeable.mixin;

import com.wennest.placeable.Placeable;
//? if >=26 {
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
//?} else {
/*import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Targets {@link SweetBerryBushBlock#randomTick}. Cancels berry-bush growth
 * when the bush is rooted on anything other than dirt-tagged soil or
 * farmland, keeping vanilla ecology untouched even when the player has
 * placed the bush on a relaxed floor.
 *
 * <p>{@code SweetBerryBushBlock} owns its own {@code randomTick} growth
 * pipeline; without this guard a berry bush placed on cobblestone would
 * still ripen and produce berries.
 */
@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {
    /**
     * Cancels growth when the bush is not on dirt or farmland, preserving
     * vanilla ripening rules after player-relaxed placement.
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
        if (underBlockState.is(BlockTags.DIRT) || underBlockState.is(Blocks.FARMLAND)) {
        //?} else {
        /*BlockState underBlockState = world.getBlockState(blockPos.down());
        if (underBlockState.isIn(BlockTags.DIRT) || underBlockState.isOf(Blocks.FARMLAND)) {
        *///?}
            return;
        }

        ci.cancel();
    }
}
