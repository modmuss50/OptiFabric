package me.modmuss50.optifabric.patcher;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//Pulls out the patched classes and saves them into a directory, and then returns a jar without the patched classes
public class PatchSplitter {

	private File inputFile;
	private File outputFile;

	public PatchSplitter(File inputFile, File outputFile) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public void extractClasses(File directory) throws IOException {
		System.out.println("Generating optifine class extractClasses list");
		if (outputFile.exists()) {
			outputFile.delete();
		}
		FileUtils.copyFile(inputFile, outputFile);

		List<String> patchedClasses = new ArrayList<>();

		try (JarFile jarFile = new JarFile(inputFile)) {
			Enumeration<JarEntry> entrys = jarFile.entries();
			while (entrys.hasMoreElements()) {
				JarEntry entry = entrys.nextElement();
				if ((entry.getName().startsWith("net/minecraft/") || entry.getName().startsWith("com/mojang/")) && entry.getName().endsWith(".class")) {
					patchedClasses.add(entry.getName().substring(0, entry.getName().length() - 6));
				}
			}
		}

		System.out.println("Found " + patchedClasses.size() + " patched classes");

		//Write out all the classes to disk
		FileUtils.writeStringToFile(new File(directory, "fabric_classes.txt"), String.join(";", patchedClasses), StandardCharsets.UTF_8);

		//Extract the jar
		ZipUtil.unpack(outputFile, directory);
		//Remove all the classes that are going to be patched in, we dont want theses on the classpath
		ZipUtil.removeEntries(outputFile, patchedClasses.stream().map(s -> s + ".class").toArray(String[]::new));

	}

}
