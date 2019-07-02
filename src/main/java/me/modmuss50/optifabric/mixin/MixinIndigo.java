package me.modmuss50.optifabric.mixin;

import net.fabricmc.indigo.Indigo;
import net.fabricmc.indigo.renderer.aocalc.AoConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Indigo.class)
public class MixinIndigo {

	@Shadow @Final @Mutable
	public static AoConfig AMBIENT_OCCLUSION_MODE;

	@Shadow @Final @Mutable
	public static boolean ALWAYS_TESSELATE_INDIGO;


	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void staticInit(CallbackInfo ci){
		//Force these config options to make shaders work, sorta
		AMBIENT_OCCLUSION_MODE = AoConfig.VANILLA;
		ALWAYS_TESSELATE_INDIGO = false;
	}


}
