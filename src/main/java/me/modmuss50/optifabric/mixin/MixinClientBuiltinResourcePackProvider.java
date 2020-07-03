package me.modmuss50.optifabric.mixin;

import me.modmuss50.optifabric.mod.OptifabricSetup;
import me.modmuss50.optifabric.util.OptifineZipResourcePack;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientBuiltinResourcePackProvider.class)
public class MixinClientBuiltinResourcePackProvider {

	@Inject(method = "register", at = @At("RETURN"))
	public <T extends ResourcePackProfile> void register(Consumer<T> consumer, ResourcePackProfile.Factory<T> factory, CallbackInfo info)  {
		File file = OptifabricSetup.optifineRuntimeJar;
		if (file != null && file.isFile()) {
			T optifineResourcePack = ResourcePackProfile.of("optifine", false, OptifineZipResourcePack.getSupplier(file), factory, ResourcePackProfile.InsertionPosition.TOP, ResourcePackSource.PACK_SOURCE_BUILTIN);
			consumer.accept(optifineResourcePack);
		}
	}
}
