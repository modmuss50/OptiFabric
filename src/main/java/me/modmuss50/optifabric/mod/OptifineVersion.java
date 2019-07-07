package me.modmuss50.optifabric.mod;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.modmuss50.optifabric.patcher.ASMUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FieldNode;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class OptifineVersion {


	public static String version;
	public static String minecraftVersion;
	public static JarType jarType;

	public static File findOptifineJar() throws IOException {
		File modsDir = new File(FabricLoader.getInstance().getGameDirectory(), "mods");
		File[] mods = modsDir.listFiles();

		File optifineJar = null;

		if (mods != null) {
			for (File file : mods) {
				if (file.isDirectory()) {
					continue;
				}
				if (file.getName().endsWith(".jar")) {
					JarType type = getJarType(file);
					if (type.error) {
						throw new RuntimeException("An error occurred when trying to find the optifine jar: " + type.name());
					}
					if (type == JarType.OPIFINE_MOD || type == JarType.OPTFINE_INSTALLER) {
						if(optifineJar != null){
							OptifabricError.setError("Found 2 or more optifine jars, please ensure you only have 1 copy of optifine in the mods folder!");
							throw new FileNotFoundException("Multiple optifine jars");
						}
						jarType = type;
						optifineJar =  file;
					}
				}
			}
		}

		if(optifineJar != null){
			return optifineJar;
		}

		OptifabricError.setError("OptiFabric could not find the Optifine jar in the mods folder.");
		throw new FileNotFoundException("Could not find optifine jar");
	}

	private static JarType getJarType(File file) throws IOException {
		ClassNode classNode;
		try (JarFile jarFile = new JarFile(file)) {
			JarEntry jarEntry = jarFile.getJarEntry("net/optifine/Config.class"); // New 1.14.3 location
			if (jarEntry == null) {
				return JarType.SOMETHINGELSE;
			}
			classNode = ASMUtils.asClassNode(jarEntry, jarFile);
		}

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

		String currentMcVersion = "unknown";
		try {
			try(InputStream is = OptifineVersion.class.getResourceAsStream("/version.json")){
				try(InputStreamReader isr = new InputStreamReader(is)){
					JsonObject jsonObject = new Gson().fromJson(isr, JsonObject.class);
					currentMcVersion = jsonObject.get("name").getAsString();
				}
			}
		} catch (Exception e){
			OptifabricError.setError("Failed to find minecraft version");
			e.printStackTrace();
			return JarType.INCOMPATIBE;
		}

		if (!currentMcVersion.equals(minecraftVersion)) {
			OptifabricError.setError(String.format("This version of optifine is not compatible with the current minecraft version\n\n Optifine requires %s you have %s", minecraftVersion, currentMcVersion));
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
