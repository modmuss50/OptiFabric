package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.patcher.ASMUtils;
import net.fabricmc.loader.api.FabricLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FrameNode;
import org.spongepowered.asm.lib.tree.MethodNode;

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

	//I have no idea why and how this works, if you know better please let me know
	public final Consumer<ClassNode> transformer = target -> {
		try {

			//I cannot imagine this being very good at all
			ClassNode source = getSourceClassNode(target);
			target.methods = source.methods;
			target.fields = source.fields;
			target.interfaces = source.interfaces;
			target.superName = source.superName;

			//Classes should be read with frames expanded (as Mixin itself does it), in which case this should all be fine
			for (MethodNode methodNode : target.methods) {
				for (AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
					if (insnNode instanceof FrameNode) {
						FrameNode frameNode = (FrameNode) insnNode;
						if (frameNode.local == null) {
							throw new IllegalStateException("Null locals in " + frameNode.type + " frame @ " + source.name + "#" + methodNode.name + methodNode.desc);
						}
					}
				}
			}

			// Lets make every class we touch public
			if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
				target.access = modAccess(target.access);
				target.methods.forEach(methodNode -> methodNode.access = modAccess(methodNode.access));
				target.fields.forEach(fieldNode -> fieldNode.access = modAccess(fieldNode.access));
			}

		} catch (IOException e) {
			throw new RuntimeException("Failed to extractClasses class");
		}
	};

	private static int modAccess(int access) {
		if ((access & 0x7) != Opcodes.ACC_PRIVATE) {
			return (access & (~0x7)) | Opcodes.ACC_PUBLIC;
		} else {
			return access;
		}
	}

	private ClassNode getSourceClassNode(ClassNode classNode) throws IOException {
		String name = classNode.name.replaceAll("\\.", "/") + ".class";
		File file = new File(classesDir, name);
		if (!file.exists()) {
			throw new FileNotFoundException("Could not find" + name);
		}
		InputStream is = new FileInputStream(file);
		byte[] bytes = IOUtils.toByteArray(is);
		is.close();
		return ASMUtils.readClassFromBytes(bytes);
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
