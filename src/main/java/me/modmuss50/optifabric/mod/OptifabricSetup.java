package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricMixinBootstrap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

		try {
			if(FabricLoader.getInstance().isModLoaded("fabric-renderer-indigo")){
				addMixinConfig("optifabric.indigofix.mixins.json");
			}

			addMixinConfig("optifabric.optifine.mixins.json");
		} catch (Exception e){
			if(OptifineVersion.error == null || OptifineVersion.error.isEmpty()){
				OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBE;
				OptifineVersion.error = "Failed to load optifine, check the log for more info \n\n " + e.getMessage();
			}
			throw new RuntimeException("Failed to setup optifine", e);
		}
	}

	private static void addMixinConfig(String mixinConfig){
		try {
			Method method = FabricMixinBootstrap.class.getDeclaredMethod("addConfiguration", String.class);
			method.setAccessible(true);
			method.invoke(null, mixinConfig);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to inject indigo render fixes", e);
		}
	}
}
