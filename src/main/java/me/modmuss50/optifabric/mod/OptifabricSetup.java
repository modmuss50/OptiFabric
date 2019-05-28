package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.patcher.ClassCache;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;

@SuppressWarnings("unused")
public class OptifabricSetup implements Runnable {

	//This is called early on to allow us to get the transformers in beofore minecraft starts
	@Override
	public void run() {
		try {
			OptifineSetup optifineSetup = new OptifineSetup();
			Pair<File, ClassCache> runtime = optifineSetup.getRuntime();

			//Add the optifine jar to the classpath, as
			ClassTinkerers.addURL(runtime.getLeft().toURI().toURL());

			OptifineInjector injector = new OptifineInjector(runtime.getRight());
			injector.setup();
		} catch (Throwable e) {
			if(OptifineVersion.error == null || OptifineVersion.error.isEmpty()){
				OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBE;
				OptifineVersion.error = "Failed to load optifine, check the log for more info \n\n " + e.getMessage();
			}
			throw new RuntimeException("Failed to setup optifine", e);
		}

		if(FabricLoader.getInstance().isModLoaded("fabric-renderer-indigo")){
			Mixins.addConfiguration("optifabric.indigofix.mixins.json");
		}

		Mixins.addConfiguration("optifabric.optifine.mixins.json");
	}

}
