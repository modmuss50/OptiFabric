package me.modmuss50.optifabric.mixin;

import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.optifine.Config")
public class MixinOptifineConfig {

	@Shadow
	private static GameOptions gameSettings;

	@SuppressWarnings({"InvalidMemberReference", "UnresolvedMixinReference"})
	@Inject(method = {"isAnimatedTerrain", "isAnimatedTextures", "isSwampColors", "isRandomEntities", "isSmoothBiome", "isCustomColors", "isCustomSky", "isCustomFonts", "isShowCapes", "isConnectedTextures", "isNaturalTextures", "isEmissiveTextures", "isConnectedTexturesFancy", "isFastRender", "isTranslucentBlocksFancy", "isSmoothWorld", "isLazyChunkLoading", "isDynamicFov", "isAlternateBlocks", "isCustomItems", "isDynamicLights", "isDynamicLightsFast", "isCustomEntityModels", "isCustomGuis", "isSmoothFps", "isShowGlErrors"},
			at = @At("HEAD"), cancellable = true, remap = false)
	private static void isRandomEntities(CallbackInfoReturnable<Boolean> returnable) {
		if (gameSettings == null) {
			returnable.setReturnValue(false);
		}
	}

}
