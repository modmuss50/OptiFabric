package me.modmuss50.optifabric.mod;

import me.modmuss50.optifabric.patcher.ASMUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class OptifineVersion {

	public static String error = null;
	public static String version;
	public static String minecraftVersion;
	public static JarType jarType;

	public static File findOptifineJar() throws IOException {
		File modsDir = new File(FabricLoader.getInstance().getGameDirectory(), "mods");
		File[] mods = modsDir.listFiles();
		if (mods != null) {
			for (File file : mods) {
				if (file.isDirectory()) {
					continue;
				}
				if (file.getName().endsWith(".jar")) {
					JarType type = getJarType(file);
					if (type.error) {
						throw new RuntimeException("An error occurred when trying to find the optifine jar: " + error);
					}
					if (type == JarType.OPIFINE_MOD || type == JarType.OPTFINE_INSTALLER) {
						jarType = type;
						return file;
					}
				}
			}
		}

		error = "OptiFabric could not find the Optifine jar in the mods folder.";
		throw new FileNotFoundException("Could not find optifine jar");
	}

	private static JarType getJarType(File file) throws IOException {
		JarFile jarFile = new JarFile(file);
		JarEntry jarEntry = jarFile.getJarEntry("Config.class"); // I hope this is enough to detect optifine
		if (jarEntry == null) {
			return JarType.SOMETHINGELSE;
		}
		ClassNode classNode = ASMUtils.asClassNode(jarEntry, jarFile);
		for (FieldNode fieldNode : classNode.fields) {
			if (fieldNode.name.equals("VERSION")) {
				version = (String) fieldNode.value;
			}
			if (fieldNode.name.equals("MC_VERSION")) {
				minecraftVersion = (String) fieldNode.value;
			}
		}

		if (version == null || version.isEmpty() || minecraftVersion == null || minecraftVersion.isEmpty()) {
			return JarType.INCOMPATIBE;
		}

		//I hope this isnt too early
		MinecraftVersion mcVersion = (MinecraftVersion) MinecraftVersion.create();
		if (!mcVersion.getName().equals(minecraftVersion)) {
			error = "This version of optifine is not compatible with the current minecraft version";
			return JarType.INCOMPATIBE;
		}

		Holder<Boolean> isInstaller = new Holder<>(false);
		ZipUtil.iterate(file, (in, zipEntry) -> {
			if (zipEntry.getName().startsWith("patch/")) {
				isInstaller.setValue(true);
			}
		});

		if (isInstaller.getValue()) {
			return JarType.OPTFINE_INSTALLER;
		} else {
			return JarType.OPIFINE_MOD;
		}
	}

	public enum JarType {
		OPIFINE_MOD(false),
		OPTFINE_INSTALLER(false),
		INCOMPATIBE(true),
		SOMETHINGELSE(false);

		boolean error;

		JarType(boolean error) {
			this.error = error;
		}

		public boolean isError() {
			return error;
		}
	}

	private static class Holder<T> {

		T value;

		private Holder(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public static <T> Holder<T> of(T value) {
			return new Holder<>(value);
		}

	}

}
