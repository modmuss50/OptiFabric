package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.patcher.ASMUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class OptifineVersion {

	public static boolean validOptifine = false;
	public static boolean isInstaller = false;
	public static String version;

	public static File findOptifineJar() {
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

		return new File("C:\\Users\\mark\\Desktop\\OptiFine_1.14_HD_U_F1_pre2_MOD.jar");
	}

	private static boolean isOptifine(File file) {
		try {
			readJar(file);
			return validOptifine;
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
				validOptifine = true;
			}
		}
	}

}
