package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.patcher.ClassCache;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.util.version.SemanticVersionImpl;
import net.fabricmc.loader.util.version.SemanticVersionPredicateParser;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
			if(!OptifabricError.hasError()){
				OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBE;
				OptifabricError.setError("Failed to load optifine, check the log for more info \n\n " + e.getMessage());
			}
			throw new RuntimeException("Failed to setup optifine", e);
		}

		if(FabricLoader.getInstance().isModLoaded("fabric-renderer-indigo")){
			validateIndigoVersion();
			Mixins.addConfiguration("optifabric.indigofix.mixins.json");
		}

		Mixins.addConfiguration("optifabric.optifine.mixins.json");
	}

	//I check the version like this as I want to show issues on our error screen
	private void validateIndigoVersion() {
		try {
			ModContainer indigoContainer = FabricLoader.getInstance().getModContainer("fabric-renderer-indigo").orElseThrow((Supplier<Throwable>) () -> new RuntimeException("Failed to get indigo's mod container, something has broke badly."));
			Version indigoVersion = indigoContainer.getMetadata().getVersion();

			Predicate<SemanticVersionImpl> predicate = SemanticVersionPredicateParser.create(">=0.1.8");
			SemanticVersionImpl version = new SemanticVersionImpl(indigoVersion.getFriendlyString(), false);

			if (!predicate.test(version)) {
				if(!OptifabricError.hasError()){
					OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBE;
					OptifabricError.setError("You are using an outdated version of Fabric (API), please update!\n\nDownload the jar from the link bellow and replace the existing Fabric (API) jar in your mods folder.", "https://www.curseforge.com/minecraft/mc-mods/fabric-api/files");
					OptifabricError.setHelpButtonText("Download Fabric (API)");
				}
			}

		} catch (Throwable e){
			if(!OptifabricError.hasError()){
				OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBE;
				OptifabricError.setError("Failed to load optifine, check the log for more info \n\n " + e.getMessage());
			}
			throw new RuntimeException("Failed to setup optifine", e);
		}
	}

}
