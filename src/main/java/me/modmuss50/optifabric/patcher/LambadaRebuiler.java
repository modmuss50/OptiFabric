package me.modmuss50.optifabric.patcher;

import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.MemberInstance;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LambadaRebuiler implements IMappingProvider {

	private File optifineFile;
	private File minecraftClientFile;

	private JarFile optifineJar;
	private JarFile clientJar;

	private Map<String, String> methodMap = new HashMap<>();
	private List<String> usedMethods = new ArrayList<>(); //Used to prevent duplicates

	public LambadaRebuiler(File optifineFile, File minecraftClientFile) throws IOException {
		this.optifineFile = optifineFile;
		this.minecraftClientFile = minecraftClientFile;
		optifineJar = new JarFile(optifineFile);
		clientJar = new JarFile(minecraftClientFile);

	}

	public void buildLambadaMap() throws IOException {
		Enumeration<JarEntry> entrys = optifineJar.entries();
		while (entrys.hasMoreElements()) {
			JarEntry entry = entrys.nextElement();
			if (entry.getName().endsWith(".class") && !entry.getName().startsWith("net/") && !entry.getName().startsWith("optifine/") && !entry.getName().startsWith("javax/")) {
				buildClassMap(entry);
			}
		}
		optifineJar.close();
		clientJar.close();
	}

	private void buildClassMap(JarEntry jarEntry) throws IOException {
		ClassNode classNode = ASMUtils.asClassNode(jarEntry, optifineJar);
		List<MethodNode> lambadaNodes = new ArrayList<>();
		for (MethodNode methodNode : classNode.methods) {
			if (!methodNode.name.startsWith("lambda$") || methodNode.name.startsWith("lambda$static")) {
				continue;
			}
			lambadaNodes.add(methodNode);
		}
		if (lambadaNodes.isEmpty()) {
			return;
		}
		ClassNode minecraftClass = ASMUtils.asClassNode(clientJar.getJarEntry(jarEntry.getName()), clientJar);
		if (!minecraftClass.name.equals(classNode.name)) {
			throw new RuntimeException("Something went wrong");
		}
		for (MethodNode methodNode : lambadaNodes) {
			MethodNode actualNode = findMethod(methodNode, classNode, minecraftClass);
			if (actualNode == null) {
				continue;
			}
			String key = classNode.name + "." + MemberInstance.getMethodId(actualNode.name, actualNode.desc);
			if (usedMethods.contains(key)) {
				System.out.println("Skipping duplicate: " + key);
				continue;
			}
			usedMethods.add(classNode.name + "." + MemberInstance.getMethodId(actualNode.name, actualNode.desc));
			methodMap.put(classNode.name + "/" + MemberInstance.getMethodId(methodNode.name, methodNode.desc), actualNode.name);
		}
	}

	private MethodNode findMethod(MethodNode optifineMethod, ClassNode optifineClass, ClassNode minecraftClass) {
		{
			MethodNode lastNode = null;
			int identiacalMethods = 0;
			for (MethodNode methodNode : minecraftClass.methods) {
				if (ASMUtils.isSynthetic(methodNode.access) && methodNode.desc.equals(optifineMethod.desc)) {
					identiacalMethods++;
					lastNode = methodNode;
				}
			}
			if (identiacalMethods == 1) {
				return lastNode;
			}
		}

		//TODO some room for some better detection here

		return null;
	}

	@Override
	public void load(Map<String, String> classMap, Map<String, String> fieldMap, Map<String, String> methodMap) {
		methodMap.putAll(this.methodMap);
	}
}
