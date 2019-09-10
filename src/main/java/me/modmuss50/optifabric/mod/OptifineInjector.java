package me.modmuss50.optifabric.mod;

import com.chocohead.mm.api.ClassTinkerers;
import me.modmuss50.optifabric.patcher.ASMUtils;
import me.modmuss50.optifabric.patcher.ChunkRendererFix;
import me.modmuss50.optifabric.patcher.ClassCache;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.FrameNode;
import org.spongepowered.asm.lib.tree.MethodNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;

public class OptifineInjector {

	ClassCache classCache;

	String chunkRenderer;
	String particleManager;

	public OptifineInjector(ClassCache classCache) {
		this.classCache = classCache;
	}

	public void setup() throws IOException {
		chunkRenderer = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_851").replaceAll("\\.", "/");
		particleManager = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_702").replaceAll("\\.", "/");

		classCache.getClasses().forEach(s -> ClassTinkerers.addTransformation(s.replaceAll("/", ".").substring(0, s.length() - 6), transformer));
	}

	//I have no idea why and how this works, if you know better please let me know
	public final Consumer<ClassNode> transformer = target -> {

		//I cannot imagine this being very good at all
		ClassNode source = getSourceClassNode(target);

		//Patch the class to fix
		if(target.name.equals(chunkRenderer)){
			ChunkRendererFix.fix(source);
		}

		//Skip applying incompatible ParticleManager changes
		if(target.name.equals(particleManager)){
			return;
		}

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

	};

	private static int modAccess(int access) {
		if ((access & 0x7) != Opcodes.ACC_PRIVATE) {
			return (access & (~0x7)) | Opcodes.ACC_PUBLIC;
		} else {
			return access;
		}
	}

	private ClassNode getSourceClassNode(ClassNode classNode) {
		String name = classNode.name.replaceAll("\\.", "/") + ".class";
		byte[] bytes = classCache.getAndRemove(name);
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
