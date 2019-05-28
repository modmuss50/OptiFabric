package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;

public class OptifabricSetup implements Runnable {

	//This is called early on to allow us to get the transformers in beofore minecraft starts
	@Override
	public void run() {
		try {
			OptifineSetup optifineSetup = new OptifineSetup();
			File file = optifineSetup.getPatchedJar();

			//Add the optifine jar to the classpath, as
			ClassTinkerers.addURL(file.toURI().toURL());

			OptifineInjector injector = new OptifineInjector(optifineSetup.getClassesDir());
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

		if(!FabricLoader.getInstance().isDevelopmentEnvironment() || getClass().getResource("OptiFabric-refmap.json") == null){
			Mixins.addConfiguration("optifabric.mixins.json");
		} else {
			//In dev, with an intermediary named optifabric jar we can skip showing a nice crash screen
			if(OptifineVersion.error != null){
				throw new RuntimeException("Failed to setup optifine: " + OptifineVersion.error);
			}
		}
	}

}
