package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.patcher.ASMUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.jar.JarFile;

public class OptifineVersion {

	public static Optional<Pair<String, String>> error = Optional.empty();
	public static String version;
	public static String minecraftVersion;

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
		} catch (NullPointerException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
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
			if (fieldNode.name.equals("MC_VERSION")) {
				minecraftVersion = (String) fieldNode.value;
			}
		}

		//I hope this isnt too early
		MinecraftVersion mcVersion = (MinecraftVersion) MinecraftVersion.create();
		if (!mcVersion.getName().equals(minecraftVersion)) {
			error = Optional.of(Pair.of("This version of optifine is incompatible with the current version of minecraft", "https://optifine.net/downloads"));
			throw new UnsupportedOperationException("The provided optifine version is incompatible with this version of minecraft");
		}

		if (false) { //TODO check to see if its an installer jar
			error = Optional.of(Pair.of("You have not extracted the Optifine mod using the installer \n\n Would you like to open the help page?", "https://gist.github.com/modmuss50/be44623562b6a0bac1bf8bef6d835a5f"));
			throw new UnsupportedOperationException("Invalid optifine jar");
		}
	}

}
