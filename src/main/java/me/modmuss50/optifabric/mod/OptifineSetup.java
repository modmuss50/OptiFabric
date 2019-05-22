package me.modmuss50.optifabric.mod;

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
import net.fabricmc.mappings.ClassEntry;
import net.fabricmc.mappings.FieldEntry;
import net.fabricmc.mappings.Mappings;
import net.fabricmc.mappings.MethodEntry;
import net.fabricmc.stitch.commands.CommandProposeFieldNames;
import net.fabricmc.tinyremapper.IMappingProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OptifineSetup {

	private File workingDir = new File(FabricLoader.getInstance().getGameDirectory(), ".optifine");
	private MappingConfiguration mappingConfiguration = new MappingConfiguration();

	private FabricLauncher fabricLauncher = FabricLauncherBase.getLauncher();

	private String inputHash;

	public File getPatchedJar() throws Throwable {
		File optifineModJar = OptifineVersion.findOptifineJar();
		inputHash = fileHash(optifineModJar.toPath());

		File fabricOptifineJar = new File(workingDir, String.format("OptiFine-fabric-%s.jar", inputHash));
		if (fabricOptifineJar.exists()) {
			System.out.println("Found existing patched optifine jar, using that");
			return fabricOptifineJar;
		}

		System.out.println("Setting up optifine for the first time, this may take a few seconds.");

		System.out.println("Building lambada fix mappings");
		LambadaRebuiler rebuiler = new LambadaRebuiler(optifineModJar, getMinecraftJar().toFile());
		rebuiler.buildLambadaMap();

		System.out.println("Remapping optifine with fixed lambada names");
		File lambadaFixJar = new File(workingDir, String.format("OptiFine-lambadafix-%s.jar", inputHash));
		RemapUtils.mapJar(lambadaFixJar.toPath(), optifineModJar.toPath(), rebuiler, getLibs());

		File remappedJar = remapOptifine(lambadaFixJar.toPath());

		PatchSplitter patcher = new PatchSplitter(remappedJar, fabricOptifineJar);
		patcher.extractClasses(getClassesDir());

		return fabricOptifineJar;
	}

	private File remapOptifine(Path input) throws Exception {
		String namespace = FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
		System.out.println("Remapping optifine to :" + namespace);

		List<Path> mcLibs = getLibs();
		mcLibs.add(getMinecraftJar());

		File remappedJar = new File(workingDir, String.format("OptiFine-mapped-%s.jar", inputHash));
		RemapUtils.mapJar(remappedJar.toPath(), input, createWorkaround(mappingConfiguration.getMappings(), "official", namespace), mcLibs);

		return remappedJar;
	}

	//This is fun, not sure why theses 2 fields dont like to be remapped
	IMappingProvider createWorkaround(Mappings mappings, String from, String to) {
		//In dev
		if (fabricLauncher.isDevelopment()) {
			try {
				File fullMappings = getDevMappings();
				return (classMap, fieldMap, methodMap) -> {
					RemapUtils.getTinyRemapper(fullMappings, from, to).load(classMap, fieldMap, methodMap);
					fieldMap.entrySet().removeIf(e -> e.getValue().equals("CLOUDS") || e.getValue().equals("renderDistance"));
				};
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		//In prod
		Mappings mappingsNew = new Mappings() {
			@Override
			public Collection<String> getNamespaces() {
				return mappings.getNamespaces();
			}

			@Override
			public Collection<ClassEntry> getClassEntries() {
				return mappings.getClassEntries();
			}

			@Override
			public Collection<FieldEntry> getFieldEntries() {
				return mappings.getFieldEntries().stream().filter(fieldEntry -> {
					String name = fieldEntry.get("intermediary").getName();
					return !(name.equals("field_1937") || name.equals("field_4062")); //Optifine isnt happy about these names
				}).collect(Collectors.toList());
			}

			@Override
			public Collection<MethodEntry> getMethodEntries() {
				return mappings.getMethodEntries();
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
		}).filter(path -> Files.exists(path)).collect(Collectors.toList());
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
			Path minecraftJar = path.resolve("minecraft-1.14-client.jar"); //Lets hope you are using loom in dev
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

	//We need to generate the full mappings with enum names as loom does not have these on the classpath
	File getDevMappings() throws Exception {
		CommandProposeFieldNames fieldNames = new CommandProposeFieldNames();
		File fieldMappings = new File(workingDir, "mappings.full.tiny");
		if (fieldMappings.exists()) {
			fieldMappings.delete();
		}
		fieldNames.run(new String[]{getMinecraftJar().normalize().toString(), extractMappings().getAbsolutePath(), fieldMappings.getAbsolutePath(), "--writeAll"});
		return fieldMappings;
	}

	//Extracts the devtime mappings out of yarn into a file
	File extractMappings() throws IOException {
		File extractedMappings = new File(workingDir, "mappings.tiny");
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

	String fileHash(Path input) throws IOException {
		try (InputStream is = Files.newInputStream(input)) {
			return DigestUtils.md5Hex(is);
		}
	}

	public String getInputHash() {
		return inputHash;
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public File getClassesDir() {
		return new File(workingDir, "classes-" + getInputHash());
	}
}
