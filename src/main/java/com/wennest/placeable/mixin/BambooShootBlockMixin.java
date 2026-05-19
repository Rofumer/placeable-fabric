package com.wennest.placeable.mixin;

import com.wennest.placeable.Placeable;
//? if >=26 {
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
//?} else {
/*import net.minecraft.block.BambooShootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Relaxes the placement rule of vanilla {@link BambooShootBlock} — the small,
 * young variant placed first when bamboo grows from a sapling — so the mod's
 * floor predicate (any block with a top rim, leaves, or dirt path) is
 * accepted in addition to {@link BlockTags#BAMBOO_PLANTABLE_ON} floors.
 *
 * <p>Defers to vanilla during worldgen and inside natural-tick frames so
 * vanilla feature placement and natural ecology spread are never widened.
 *
 * <p>Also prevents random-tick growth when the shoot rests on a
 * non-bamboo-plantable floor (mod-relaxed placement). Without this guard a
 * shoot placed on cobblestone or a stair would silently mature into a full
 * stalk.
 *
 * <p><b>Cross-version note</b>: Mojang's 1.21.5 rename was
 * {@code BambooSaplingBlock -> BambooShootBlock}. The Yarn mappings used by
 * this project expose the class as {@code BambooShootBlock} on every
 * supported version (1.21.1 / 1.21.5 / 1.21.8 / 1.21.11), so no Stonecutter
 * conditional is required for the import or the {@code @Mixin} target. The
 * {@code Blocks.BAMBOO_SAPLING} field used elsewhere is similarly stable
 * across these versions.
 */
//? if >=26 {
@Mixin(BambooSaplingBlock.class)
//?} else {
/*@Mixin(BambooShootBlock.class)
*///?}
public class BambooShootBlockMixin {

    /**
     * HEAD-injected override of {@code BambooShootBlock#canPlaceAt}.
     * Standard relaxed-floor shape.
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
     * Cancels random-tick growth when the shoot rests on a
     * non-bamboo-plantable floor (mod-relaxed placement).
     */
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    //? if >=26 {
    public void placeable$randomTickMixin(BlockState state, ServerLevel world, BlockPos pos,
                                          RandomSource random, CallbackInfo ci) {
    //?} else {
    /*public void placeable$randomTickMixin(BlockState state, ServerWorld world, BlockPos pos,
                                          Random random, CallbackInfo ci) {
    *///?}
        if (Placeable.isDisabled(state)) {
            return;
        }

        //? if >=26 {
        BlockState floor = world.getBlockState(pos.below());
        if (!floor.is(BlockTags.SUPPORTS_BAMBOO)) {
        //?} else {
        /*BlockState floor = world.getBlockState(pos.down());
        if (!floor.isIn(BlockTags.BAMBOO_PLANTABLE_ON)) {
        *///?}
            // Suppress vanilla's growth body — this shoot was placed on a
            // mod-relaxed floor and must not mature.
            ci.cancel();
        }
    }
}
