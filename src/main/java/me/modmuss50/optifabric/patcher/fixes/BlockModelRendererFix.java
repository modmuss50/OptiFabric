package me.modmuss50.optifabric.patcher.fixes;

import me.modmuss50.optifabric.util.RemappingUtils;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

//Just add the old method back, seems to not crash.
public class BlockModelRendererFix implements ClassFixer {

	//renderQuad(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFFIIIII)V
	private String renderQuadName = RemappingUtils.getMethodName("class_778", "method_23073", "(Lnet/minecraft/class_1920;Lnet/minecraft/class_2680;Lnet/minecraft/class_2338;Lnet/minecraft/class_4588;Lnet/minecraft/class_4587$class_4665;Lnet/minecraft/class_777;FFFFIIIII)V");

	@Override
	public void fix(ClassNode optifine, ClassNode minecraft) {
		Validate.notNull(renderQuadName, "Could not find renderQuad name");
		MethodNode oldNode = minecraft.methods.stream().filter(methodNode -> methodNode.name.equals(renderQuadName)).findFirst().orElse(null);
		Validate.notNull(oldNode, "Failed to find old " + renderQuadName + " method");
		optifine.methods.add(oldNode);
	}
}
