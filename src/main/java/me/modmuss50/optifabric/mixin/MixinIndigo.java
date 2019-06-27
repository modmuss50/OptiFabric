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
	private static AoConfig optifabric_backupAOM;

	@Shadow @Final @Mutable
	public static boolean ALWAYS_TESSELATE_INDIGO;
	private static boolean optifabric_backupATI;

	private static boolean wasEnabled;

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void staticInit(CallbackInfo ci){

		//Saves the configs from disk in-case shaders are disabled
		optifabric_backupAOM = AMBIENT_OCCLUSION_MODE;
		optifabric_backupATI = ALWAYS_TESSELATE_INDIGO;
	}

	//Called via reflection
	@SuppressWarnings("unused")
	private static void optifabric_ShaderChange(boolean enabled){
		if(enabled){
			System.out.println("Enabling optifabric shader support in Indigo");

			//Force these config options to make shaders work, sorta
			AMBIENT_OCCLUSION_MODE = AoConfig.VANILLA;
			ALWAYS_TESSELATE_INDIGO = false;


			wasEnabled = true;
		} else if (wasEnabled) {
			System.out.println("Disabling optifabric  shader support in Indigo");
			wasEnabled = false;

			//Go back to the backup values
			AMBIENT_OCCLUSION_MODE = optifabric_backupAOM;
			ALWAYS_TESSELATE_INDIGO = optifabric_backupATI;
		}
	}

}
