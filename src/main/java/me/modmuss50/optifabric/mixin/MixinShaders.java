package me.modmuss50.optifabric.mixin;

import me.modmuss50.optifabric.mod.ShaderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.optifine.shaders.Shaders")
public class MixinShaders {

	@Shadow
	private static boolean shaderPackLoaded;

	@Inject(method = "loadShaderPack", at = @At("RETURN"), remap = false)
	private static void loadShaderPack(CallbackInfo ci) {
		ShaderHelper.changeShaderState(shaderPackLoaded);
	}

}
