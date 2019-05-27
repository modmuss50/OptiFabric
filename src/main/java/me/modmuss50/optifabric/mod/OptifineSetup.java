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
import net.fabricmc.mappings.EntryTriple;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Optional;
import java.util.stream.Collectors;

public class OptifineSetup {

	private File workingDir = new File(FabricLoader.getInstance().getGameDirectory(), ".optifine");
	private MappingConfiguration mappingConfiguration = new MappingConfiguration();

	private FabricLauncher fabricLauncher = FabricLauncherBase.getLauncher();

	private String inputHash;

	public File getPatchedJar() throws Throwable {
		if (!workingDir.exists()) {
			workingDir.mkdirs();
		}
		File optifineModJar = OptifineVersion.findOptifineJar();
		inputHash = fileHash(optifineModJar.toPath());

		if (OptifineVersion.jarType == OptifineVersion.JarType.OPTFINE_INSTALLER) {
			File optifineMod = new File(workingDir, String.format("OptiFine-mod-%s.jar", inputHash));
			if (!optifineMod.exists()) {
				OptifineInstaller.extract(optifineModJar, optifineMod, getMinecraftJar().toFile());
			}
			optifineModJar = optifineMod;
		}

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
		RemapUtils.mapJar(remappedJar.toPath(), input, createMappings("official", namespace), mcLibs);

		return remappedJar;
	}

	//Optifine currently has two fields that match the same name as Yarn mappings, we'll rename Optifine's to something else
	IMappingProvider createMappings(String from, String to) {
		//In dev
		if (fabricLauncher.isDevelopment()) {
			try {
				File fullMappings = getDevMappings();
				return (classMap, fieldMap, methodMap) -> {
					RemapUtils.getTinyRemapper(fullMappings, from, to).load(classMap, fieldMap, methodMap);

					Map<String, String> extra = new HashMap<>();
					Pattern regex = Pattern.compile("(\\w+)\\/(\\w+);;([\\w/;]+)");

					for (Entry<String, String> entry : fieldMap.entrySet()) {
						if ("CLOUDS".equals(entry.getValue())) {
							Matcher matcher = regex.matcher(entry.getKey());
							if (!matcher.matches()) throw new IllegalStateException("Couldn't match " + entry.getKey() + " => " + entry.getValue());
							extra.put(matcher.group(1) + "/CLOUDS;;" + matcher.group(3), "CLOUDS_OF");
						}

						if ("renderDistance".equals(entry.getValue())) {
							Matcher matcher = regex.matcher(entry.getKey());
							if (!matcher.matches()) throw new IllegalStateException("Couldn't match " + entry.getKey() + " => " + entry.getValue());
							extra.put(matcher.group(1) + "/renderDistance;;" + matcher.group(3), "renderDistance_OF");
						}
					}

					fieldMap.putAll(extra);
				};
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		//In prod
		Mappings mappingsNew = new Mappings() {
			private final Mappings mappings = mappingConfiguration.getMappings();

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
				Collection<FieldEntry> fields = mappings.getFieldEntries();

				for (FieldEntry field : fields) {
					String interName = field.get("intermediary").getName();

					//Option#CLOUDS
					if ("field_1937".equals(interName)) {
						fields.add(namespace -> {
							EntryTriple real = field.get(namespace);
							return new EntryTriple(real.getOwner(), "official".equals(namespace) ? "CLOUDS" : "CLOUDS_OF", real.getDesc());
						});
					}

					//WorldRenderer#renderDistance
					if ("field_4062".equals(interName)) {
						fields.add(namespace -> {
							EntryTriple real = field.get(namespace);
							return new EntryTriple(real.getOwner(), "official".equals(namespace) ? "renderDistance" : "renderDistance_OF", real.getDesc());
						});
					}
				}

				return fields;
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
