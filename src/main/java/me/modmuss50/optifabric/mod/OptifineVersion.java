package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.patcher.ASMUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.jar.JarFile;

public class OptifineVersion {

	public static Optional<Pair<String, String>> error = Optional.empty();
	public static String version;

	public static File findOptifineJar() throws FileNotFoundException {
		File modsDir = new File(FabricLoader.getInstance().getGameDirectory(), "mods");
		File[] mods = modsDir.listFiles();
		if (mods != null) {
			for (File file : mods) {
				if (file.isDirectory()) {
					continue;
				}
				if (file.getName().endsWith(".jar")) {
					if (OptifineVersion.isOptifine(file)) {
						System.out.println("Found " + version);
						return file;
					}
				}
			}
		}

		error = Optional.of(Pair.of("OptiFabric could not find the Optifine extracted jar file in the mods folder. \n\n Would you like to open the help page?", "https://gist.github.com/modmuss50/4b2dbfc3488a0a1f1e72d037406f77af"));

		throw new FileNotFoundException("Could not find optifine jar");
	}

	private static boolean isOptifine(File file) {
		try {
			readJar(file);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static void readJar(File file) throws IOException {
		JarFile jarFile = new JarFile(file);
		ClassNode classNode = ASMUtils.asClassNode(jarFile.getJarEntry("Config.class"), jarFile);
		for (FieldNode fieldNode : classNode.fields) {
			if (fieldNode.name.equals("VERSION")) {
				version = (String) fieldNode.value;
			}
		}
		if(false){ //TODO check to see if its an installer jar
			error = Optional.of(Pair.of("You have not extracted the Optifine mod using the installer \n\n Would you like to open the help page?", "https://gist.github.com/modmuss50/be44623562b6a0bac1bf8bef6d835a5f"));
		}
	}

}
