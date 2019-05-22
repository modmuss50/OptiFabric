package me.modmuss50.optifabric.patcher;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//Patches the intermediary optifine jar to work on fabric
public class FabricPatcher {

	private File inputFile;
	private File outputFile;

	public FabricPatcher(File inputFile, File outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public void patch() throws IOException {
		System.out.println("Generating optifine class patch list");
		if (outputFile.exists()) {
			outputFile.delete();
		}
		FileUtils.copyFile(inputFile, outputFile);

		List<String> patchedClasses = new ArrayList<>();

		JarFile jarFile = new JarFile(inputFile);
		Enumeration<JarEntry> entrys = jarFile.entries();
		while (entrys.hasMoreElements()) {
			JarEntry entry = entrys.nextElement();
			if ((entry.getName().startsWith("net/minecraft/") || entry.getName().startsWith("com/mojang/")) && entry.getName().endsWith(".class")) {
				patchedClasses.add(entry.getName().substring(0, entry.getName().length() - 6));
			}
		}

		System.out.println("Found " + patchedClasses.size() + " patched classes");

		ZipUtil.addEntry(outputFile, "fabric_classes.txt", String.join(";", patchedClasses).getBytes());
	}

}
