package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;

import java.io.File;
import java.util.jar.JarFile;

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
			throw new RuntimeException("Failed to setup optifine", e);
		}
	}
}
