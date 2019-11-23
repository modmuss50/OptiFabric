package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.patcher.ClassCache;
import me.modmuss50.optifabric.patcher.LambadaRebuiler;
import me.modmuss50.optifabric.patcher.PatchSplitter;
import me.modmuss50.optifabric.patcher.RemapUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncher;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.launch.common.MappingConfiguration;
import net.fabricmc.loader.launch.knot.Knot;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;
import net.fabricmc.loader.util.mappings.TinyRemapperMappingsHelper;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.tinyremapper.IMappingProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OptifineSetup {

	private File workingDir = new File(FabricLoader.getInstance().getGameDirectory(), ".optifine");
	private File versionDir;
	private MappingConfiguration mappingConfiguration = new MappingConfiguration();

	private FabricLauncher fabricLauncher = FabricLauncherBase.getLauncher();


	public Pair<File, ClassCache> getRuntime() throws Throwable {
		if (!workingDir.exists()) {
			workingDir.mkdirs();
		}
		File optifineModJar = OptifineVersion.findOptifineJar();

		byte[] modHash = fileHash(optifineModJar);

		versionDir = new File(workingDir, OptifineVersion.version);
		if (!versionDir.exists()) {
			versionDir.mkdirs();
		}

		File remappedJar = new File(versionDir, "Optifine-mapped.jar");
		File optifinePatches = new File(versionDir, "Optifine.classes");

		ClassCache classCache = null;
		if(remappedJar.exists() && optifinePatches.exists()){
			classCache = ClassCache.read(optifinePatches);
			//Validate that the classCache found is for the same input jar
			if(!Arrays.equals(classCache.getHash(), modHash)){
				System.out.println("Class cache is from a different optifine jar, deleting and re-generating");
				classCache = null;
				optifinePatches.delete();
			}
		}

		if (remappedJar.exists() && classCache != null) {
			System.out.println("Found existing patched optifine jar, using that");
			return Pair.of(remappedJar, classCache);
		}

		if (OptifineVersion.jarType == OptifineVersion.JarType.OPTFINE_INSTALLER) {
			File optifineMod = new File(versionDir, "/Optifine-mod.jar");
			if (!optifineMod.exists()) {
				OptifineInstaller.extract(optifineModJar, optifineMod, getMinecraftJar().toFile());
			}
			optifineModJar = optifineMod;
		}

		System.out.println("Setting up optifine for the first time, this may take a few seconds.");

		//A jar without srgs
		File jarOfTheFree = new File(versionDir, "/Optifine-jarofthefree.jar");
		List<String> srgs = new ArrayList<>();

		System.out.println("De-Volderfiying jar");

		//Find all the SRG named classes and remove them
		ZipUtil.iterate(optifineModJar, (in, zipEntry) -> {
			String name = zipEntry.getName();
			if(name.startsWith("com/mojang/blaze3d/platform/")){
				if(name.contains("$")){
					String[] split = name.replace(".class", "").split("\\$");
					if(split.length >= 2){
						if(split[1].length() > 2){
							srgs.add(name);
						}
					}
				}
			}

			if(name.startsWith("srg/") || name.startsWith("net/minecraft/")){
				srgs.add(name);
			}
		});

		if(jarOfTheFree.exists()){
			jarOfTheFree.delete();
		}

		ZipUtil.removeEntries(optifineModJar, srgs.toArray(new String[0]), jarOfTheFree);

		System.out.println("Building lambada fix mappings");
		LambadaRebuiler rebuiler = new LambadaRebuiler(jarOfTheFree, getMinecraftJar().toFile());
		rebuiler.buildLambadaMap();

		System.out.println("Remapping optifine with fixed lambada names");
		File lambadaFixJar = new File(versionDir, "/Optifine-lambadafix.jar");
		RemapUtils.mapJar(lambadaFixJar.toPath(), jarOfTheFree.toPath(), rebuiler, getLibs());

		remapOptifine(lambadaFixJar.toPath(), remappedJar);

		classCache = PatchSplitter.generateClassCache(remappedJar, optifinePatches, modHash);

		if(true){
			//We are done, lets get rid of the stuff we no longer need
			lambadaFixJar.delete();
			jarOfTheFree.delete();

			if(OptifineVersion.jarType == OptifineVersion.JarType.OPTFINE_INSTALLER){
				optifineModJar.delete();
			}

			File extractedMappings = new File(versionDir, "mappings.tiny");
			File fieldMappings = new File(versionDir, "mappings.full.tiny");
			extractedMappings.delete();
			fieldMappings.delete();
		}

		boolean extractClasses = Boolean.parseBoolean(System.getProperty("optifabric.extract", "false"));
		if(extractClasses){
			System.out.println("Extracting optifine classes");
			File optifineClasses = new File(versionDir, "optifine-classes");
			if(optifineClasses.exists()){
				FileUtils.deleteDirectory(optifineClasses);
			}
			ZipUtil.unpack(remappedJar, optifineClasses);
		}

		return Pair.of(remappedJar, classCache);
	}

	private void remapOptifine(Path input, File remappedJar) throws Exception {
		String namespace = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
		System.out.println("Remapping optifine to :" + namespace);

		List<Path> mcLibs = getLibs();
		mcLibs.add(getMinecraftJar());

		RemapUtils.mapJar(remappedJar.toPath(), input, createMappings("official", namespace), mcLibs);
	}

	//Optifine currently has two fields that match the same name as Yarn mappings, we'll rename Optifine's to something else
	IMappingProvider createMappings(String from, String to) {
		//In dev
		if (fabricLauncher.isDevelopment()) {
			try {
				File fullMappings = extractMappings();
				return (out) -> {
					RemapUtils.getTinyRemapper(fullMappings, from, to).load(out);
					out.acceptField(new IMappingProvider.Member("cyf", "CLOUDS", "Lcxt;"),
							"CLOUDS_OF");
					out.acceptField(new IMappingProvider.Member("dng", "renderDistance", "I"),
							"renderDistance_OF");
				};
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		//In prod
		TinyTree mappingsNew = new TinyTree() {
			private final TinyTree mappings = mappingConfiguration.getMappings();

			@Override
			public TinyMetadata getMetadata() {
				return mappings.getMetadata();
			}

			@Override
			public Map<String, ClassDef> getDefaultNamespaceClassMap() {
				return mappings.getDefaultNamespaceClassMap();
			}

			@Override
			public Collection<ClassDef> getClasses() {
				return mappings.getClasses();
			}
		};
		return TinyRemapperMappingsHelper.create(mappingsNew, from, to);
	}

	//Gets the minecraft librarys
	List<Path> getLibs() {
		return fabricLauncher.getLoadTimeDependencies().stream().map(url -> {
			try {
				return UrlUtil.asPath(url);
			} catch (UrlConversionException e) {
				throw new RuntimeException(e);
			}
		}).filter(Files::exists).collect(Collectors.toList());
	}

	//Gets the offical minecraft jar
	Path getMinecraftJar() throws FileNotFoundException {
		Optional<Path> entrypointResult = findFirstClass(Knot.class.getClassLoader(), Collections.singletonList("net.minecraft.client.main.Main"));
		if (!entrypointResult.isPresent()) {
			throw new RuntimeException("Failed to find minecraft jar");
		}
		if (!Files.exists(entrypointResult.get())) {
			throw new RuntimeException("Failed to locate minecraft jar");
		}
		if (fabricLauncher.isDevelopment()) {
			Path path = entrypointResult.get().getParent();
			Path minecraftJar = path.resolve(String.format("minecraft-%s-client.jar", OptifineVersion.minecraftVersion)); //Lets hope you are using loom in dev
			if (!Files.exists(minecraftJar)) {
				throw new FileNotFoundException("Could not find minecraft jar!");
			}
			return minecraftJar;
		}
		return entrypointResult.get();
	}

	//Stolen from fabric loader
	static Optional<Path> findFirstClass(ClassLoader loader, List<String> classNames) {
		List<String> entrypointFilenames = classNames.stream().map((ep) -> ep.replace('.', '/') + ".class").collect(Collectors.toList());

		for (int i = 0; i < entrypointFilenames.size(); i++) {
			String className = classNames.get(i);
			String classFilename = entrypointFilenames.get(i);
			Optional<Path> classSourcePath = getSource(loader, classFilename);
			if (classSourcePath.isPresent()) {
				return Optional.of(classSourcePath.get());
			}
		}

		return Optional.empty();
	}

	static Optional<Path> getSource(ClassLoader loader, String filename) {
		URL url;
		if ((url = loader.getResource(filename)) != null) {
			try {
				URL urlSource = UrlUtil.getSource(filename, url);
				Path classSourceFile = UrlUtil.asPath(urlSource);

				return Optional.of(classSourceFile);
			} catch (UrlConversionException e) {
				// TODO: Point to a logger
				e.printStackTrace();
			}
		}

		return Optional.empty();
	}

	//Extracts the devtime mappings out of yarn into a file
	File extractMappings() throws IOException {
		File extractedMappings = new File(versionDir, "mappings.tiny");
		if (extractedMappings.exists()) {
			extractedMappings.delete();
		}
		InputStream mappingStream = FabricLauncherBase.class.getClassLoader().getResourceAsStream("mappings/mappings.tiny");
		FileUtils.copyInputStreamToFile(mappingStream, extractedMappings);
		if (!extractedMappings.exists()) {
			throw new RuntimeException("failed to extract mappings!");
		}
		return extractedMappings;
	}

	byte[] fileHash(File input) throws IOException {
		try (InputStream is = new FileInputStream(input)) {
			return DigestUtils.md5(is);
		}
	}
}
