package me.modmuss50.optifabric.mixin;

import net.fabricmc.indigo.renderer.accessor.AccessChunkRendererRegion;
import net.fabricmc.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Fabric API's indigo renderer support for optifine. This stops it from crashing, I dont really know whats going on here
@Pseudo
@Mixin(targets = "net.optifine.override.ChunkCacheOF")
public abstract class MixinChunkCacheOF implements AccessChunkRendererRegion {
	private TerrainRenderContext fabric_renderer;

	//This was taken from https://github.com/FabricMC/fabric/blob/master/fabric-renderer-indigo/src/main/java/net/fabricmc/indigo/renderer/mixin/MixinChunkRenderTask.java honesly no idea what it does, but it doesnt crash the game here
	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	private void constructor(ChunkRendererRegion chunkCache, BlockPos posFromIn, BlockPos posToIn, int subIn, CallbackInfo info){
		if(chunkCache != null) {
			final TerrainRenderContext renderer  = TerrainRenderContext.POOL.get();
			renderer.setBlockView(chunkCache);
			fabric_setRenderer(renderer);
		}
	}

	@Override
	public TerrainRenderContext fabric_getRenderer() {
		return fabric_renderer;
	}

	@Override
	public void fabric_setRenderer(TerrainRenderContext renderer) {
		fabric_renderer = renderer;
	}
}
