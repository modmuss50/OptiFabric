package me.modmuss50.optifabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//Suppresses some warnings in the log
@Pseudo
@Mixin(targets = "net.optifine.reflect.ReflectorClass")
public class MixinReflectorClass {

	@Shadow
	private String targetClassName;

	@Shadow
	private boolean checked;

	@Inject(method = "getTargetClass", at = @At("HEAD"), cancellable = true, remap = false)
	public void getTargetClass(CallbackInfoReturnable<Class> infoReturnable) {
		String name = targetClassName.replaceAll("/", ".");
		if(name.startsWith("net.minecraft.launchwrapper") || name.startsWith("net.minecraftforge")){
			checked = true;
		}
	}

}
