package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.patcher.ClassCache;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.util.version.SemanticVersionImpl;
import net.fabricmc.loader.util.version.SemanticVersionPredicateParser;
import net.fabricmc.loader.util.version.VersionParsingException;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class OptifabricSetup implements Runnable {

	public static final String OPTIFABRIC_INCOMPATIBLE = "optifabric:incompatible";

	//This is called early on to allow us to get the transformers in beofore minecraft starts
	@Override
	public void run() {
		if(!validateLoaderVersion()) return;
		if(!validateMods()) return;

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
			if (!isVersionValid("fabric-renderer-indigo", ">=0.1.8")) {
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

	private boolean validateMods() {
		List<ModMetadata> incompatibleMods = new ArrayList<>();
		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			ModMetadata metadata = container.getMetadata();
			if(metadata.containsCustomValue(OPTIFABRIC_INCOMPATIBLE)) {
				incompatibleMods.add(metadata);
			}
		}
		if (!incompatibleMods.isEmpty()) {
			OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBE;
			StringBuilder errorMessage = new StringBuilder("One or more mods have stated they are incompatible with OptiFabric\nPlease remove OptiFabric or the following mods:\n");
			for (ModMetadata metadata : incompatibleMods) {
				errorMessage.append(metadata.getName())
						.append(" (")
						.append(metadata.getId())
						.append(")\n");
			}
			OptifabricError.setError(errorMessage.toString());
		}
		return incompatibleMods.isEmpty();
	}

	private boolean validateLoaderVersion() {
		try {
			if (!isVersionValid("fabricloader", ">=0.7.0")) {
				if(!OptifabricError.hasError()){
					OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBE;
					OptifabricError.setError("You are using an outdated version of Fabric Loader, please update!\n\nRe-run the installer, or update via your launcher. See the link for help!", "https://fabricmc.net/wiki/install");
					OptifabricError.setHelpButtonText("Installation Instructions");
					return false;
				}
			}
		} catch (Throwable e){
			if(!OptifabricError.hasError()){
				OptifineVersion.jarType = OptifineVersion.JarType.INCOMPATIBE;
				OptifabricError.setError("Failed to load optifine, check the log for more info \n\n " + e.getMessage());
			}
			throw new RuntimeException("Failed to setup optifine", e);
		}
		return true;
	}

	private boolean isVersionValid(String modID, String validVersion) throws VersionParsingException {
		ModMetadata modMetadata = getModMetaData(modID);
		if(modMetadata == null) {
			throw new RuntimeException(String.format("Failed to get mod container for %s, something has broke badly.", modID));
		}

		Predicate<SemanticVersionImpl> predicate = SemanticVersionPredicateParser.create(validVersion);
		SemanticVersionImpl version = new SemanticVersionImpl(modMetadata.getVersion().getFriendlyString(), false);
		return predicate.test(version);
	}

	private ModMetadata getModMetaData(String modId) {
		Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);
		return modContainer.map(ModContainer::getMetadata).orElse(null);
	}

}
