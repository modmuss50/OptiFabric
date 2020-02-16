package me.modmuss50.optifabric.patcher.fixes;

import me.modmuss50.optifabric.util.RemappingUtils;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

//https://github.com/JamiesWhiteShirt/trumpet-skeleton-fabric/blob/45a1a99169593f0ff0f4a6985769fa108f70bceb/src/main/java/com/jamieswhiteshirt/trumpetskeleton/mixin/client/HeldItemRendererMixin.java
public class HeldItemRendererFix implements ClassFixer {

	//renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V
	private String renderFirstPersonItemName = RemappingUtils.getMethodName("class_759", "method_3228", "(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V");
	//net/minecraft/util/Hand
	private String handName = RemappingUtils.fromIntermediary("class_1268");

	//Use the vanilla method here, while adding back some of optifines tweaks to it
	@Override
	public void fix(ClassNode optifine, ClassNode minecraft) {
		Validate.notNull(renderFirstPersonItemName, "Failed to find name for renderFirstPersonItem");
		Validate.notNull(handName, "Failed to find name for handName");
		MethodNode oldNode = minecraft.methods.stream().filter(methodNode -> methodNode.name.equals(renderFirstPersonItemName)).findFirst().orElse(null);
		Validate.notNull(oldNode, "Failed to find old " + renderFirstPersonItemName + " method");

		InsnList insnList = new InsnList();
		insnList.add(getInstList("net/optifine/Config", "isShaders", "()Z"));

		insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
		insnList.add(getInstList("net/optifine/shaders/Shaders", "isSkipRenderHand", String.format("(L%s;)Z", handName)));

		oldNode.instructions.insertBefore(oldNode.instructions.getFirst(), insnList);

		//Remove the old method
		optifine.methods.removeIf(methodNode -> methodNode.name.equals(renderFirstPersonItemName));
		//Add the replacement method back
		optifine.methods.add(oldNode);
	}

	private InsnList getInstList(String owner, String name, String descriptor) {
		InsnList insnList = new InsnList();
		insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, descriptor, false));
		LabelNode jumpNode = new LabelNode();
		insnList.add(new JumpInsnNode(Opcodes.IFEQ, jumpNode));
		insnList.add(new InsnNode(Opcodes.RETURN));
		insnList.add(jumpNode);
		return insnList;
	}
}
