package me.modmuss50.optifabric.patcher.fixes;

import me.modmuss50.optifabric.util.RemappingUtils;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ThreadedAnvilChunkStorageFix implements ClassFixer {

	private String method_20458 = RemappingUtils.getMethodName("class_3898", "method_20460", "(Lnet/minecraft/server/world/ChunkHolder;Lcom/mojang/datafixers/util/Either;)Lcom/mojang/datafixers/util/Either;");
	private String lambda_method_17227 = RemappingUtils.getMethodName("class_3898", "method_17227", "(Lnet/minecraft/server/world/ChunkHolder;Lnet/minecraft/world/chunk/Chunk;)Lnet/minecraft/world/chunk/Chunk;");
	private String lambda_method_18843 = RemappingUtils.getMethodName("class_3898", "method_18843", "(Lnet/minecraft/server/world/ChunkHolder;Ljava/util/concurrent/CompletableFuture;JLnet/minecraft/world/chunk/Chunk;)V");

	@Override
	public void fix(ClassNode optifine, ClassNode minecraft) {
		Validate.notNull(method_20458, "Failed to find name");
		Validate.notNull(lambda_method_17227, "Failed to lambda name");

		//put the old methods backs
		replaceOrCopyMethod(optifine, minecraft, method_20458);
		replaceOrCopyMethod(optifine, minecraft, lambda_method_17227);
		replaceOrCopyMethod(optifine, minecraft, lambda_method_18843);
	}

	private void replaceOrCopyMethod(ClassNode optifine, ClassNode minecraft, String name) {
		MethodNode vanillaNode = null;

		for (MethodNode method : minecraft.methods) {
			if (method.name.equals(name)) {
				vanillaNode = method;
				break;
			}
		}

		optifine.methods.removeIf((m) -> m.name.equals(name));
		optifine.methods.add(vanillaNode);
	}
}
