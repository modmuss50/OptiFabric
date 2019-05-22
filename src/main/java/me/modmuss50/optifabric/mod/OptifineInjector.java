package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.tree.ClassNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.jar.JarFile;

public class OptifineInjector {

	JarFile optifineJar;

	public OptifineInjector(JarFile optifineJar) {
		this.optifineJar = optifineJar;
	}

	public void setup() throws IOException {

		InputStream is = optifineJar.getInputStream(optifineJar.getJarEntry("fabric_classes.txt"));
		String classes = toString(is, StandardCharsets.UTF_8);

		for (String clazz : classes.split(";")) {
			ClassTinkerers.addTransformation(clazz.replaceAll("/", "."), transformer);
		}
	}

	public final Consumer<ClassNode> transformer = target -> {
		try {
			//System.out.println("Patching " + target.name);

			ClassNode source = getSourceClassNode(target);
			target.methods = source.methods;
			target.fields = source.fields;
			target.interfaces = source.interfaces;
			target.superName = source.superName;
			target.access = source.access;
			target.attrs = source.attrs;
			target.innerClasses = source.innerClasses;

		} catch (IOException e) {
			throw new RuntimeException("Failed to patch class");
		}
	};

	private ClassNode getSourceClassNode(ClassNode classNode) throws IOException {
		String name = classNode.name.replaceAll("\\.", "/") + ".class";
		InputStream is = optifineJar.getInputStream(optifineJar.getJarEntry(name));
		if (is == null) {
			throw new RuntimeException("Failed to find" + name);
		}
		byte[] bytes = IOUtils.toByteArray(is);
		return readClassFromBytes(bytes);
	}

	private ClassNode readClassFromBytes(byte[] bytes) {
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		return classNode;
	}

	public String toString(InputStream inputStream, Charset charset) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
		return stringBuilder.toString();
	}

}
