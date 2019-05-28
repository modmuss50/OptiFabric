package me.modmuss50.optifabric.patcher;

import org.apache.commons.io.IOUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//Pulls out the patched classes and saves into a classCache, and also creates an optifine jar without these classes
public class PatchSplitter {

	public static ClassCache generateClassCache(File inputFile, File classCacheOutput, byte[] inputHash) throws IOException {
		ClassCache classCache = new ClassCache(inputHash);
		try (JarFile jarFile = new JarFile(inputFile)) {
			Enumeration<JarEntry> entrys = jarFile.entries();
			while (entrys.hasMoreElements()) {
				JarEntry entry = entrys.nextElement();
				if ((entry.getName().startsWith("net/minecraft/") || entry.getName().startsWith("com/mojang/")) && entry.getName().endsWith(".class")) {
					try(InputStream inputStream = jarFile.getInputStream(entry)){
						String name = entry.getName();
						byte[] bytes = IOUtils.toByteArray(inputStream);
						classCache.addClass(name, bytes);
					}
				}
			}
		}


		//Remove all the classes that are going to be patched in, we dont want theses on the classpath
		ZipUtil.removeEntries(inputFile, classCache.getClasses().stream().toArray(String[]::new));

		System.out.println("Found " + classCache.getClasses().size() + " patched classes");
		classCache.save(classCacheOutput);
		return classCache;
	}

}
