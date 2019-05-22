package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.tree.ClassNode;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class OptifineInjector {

	File classesDir;

	public OptifineInjector(File classesDir) {
		this.classesDir = classesDir;
	}

	public void setup() throws IOException {
		String classes = FileUtils.readFileToString(new File(classesDir, "fabric_classes.txt"), StandardCharsets.UTF_8);

		for (String clazz : classes.split(";")) {
			ClassTinkerers.addTransformation(clazz.replaceAll("/", "."), transformer);
		}
	}

	public final Consumer<ClassNode> transformer = target -> {
		try {

			//I cannot imagine this being very good at all
			ClassNode source = getSourceClassNode(target);
			target.methods = source.methods;
			target.fields = source.fields;
			target.interfaces = source.interfaces;

		} catch (IOException e) {
			throw new RuntimeException("Failed to extractClasses class");
		}
	};

	private ClassNode getSourceClassNode(ClassNode classNode) throws IOException {
		String name = classNode.name.replaceAll("\\.", "/") + ".class";
		File file = new File(classesDir, name);
		if (!file.exists()) {
			throw new FileNotFoundException("Could not find" + name);
		}
		InputStream is = new FileInputStream(file);
		byte[] bytes = IOUtils.toByteArray(is);
		is.close();
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
