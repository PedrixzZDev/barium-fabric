package pedrixzz.barium.mixin.render.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Chunk.class)
public abstract class ChunkOptimizer {
  @Shadow
  private BlockState[] blockStates;

  @Shadow
  private int[] heightMap;

  private final Map<BlockPos, BlockState> blockStateCache = new HashMap<>();

  @Inject(at = @At("HEAD"), method = "getBlockState(II)Lnet/minecraft/block/BlockState;")
  private void optimizeGetBlockState(int x, int z, CallbackInfo ci) {
    BlockPos pos = new BlockPos(x, 0, z);
    if (blockStateCache.containsKey(pos)) {
      ci.setReturnValue(blockStateCache.get(pos));
    }
  }

  @Inject(at = @At("HEAD"), method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
  private void updateBlockStateCache(BlockPos pos, BlockState state, CallbackInfo ci) {
    blockStateCache.put(pos, state);
  }

  @Inject(at = @At("HEAD"), method = "removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z")
  private void invalidateBlockStateCache(BlockPos pos, boolean isMoving, CallbackInfo ci) {
    blockStateCache.remove(pos);
  }
}
