package me.modmuss50.optifabric.patcher.fixes;

import me.modmuss50.optifabric.util.RemappingUtils;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public class SpriteAtlasTextureFix implements ClassFixer {

	private String stitchName = RemappingUtils.getMethodName("class_1059", "method_18163", "(Lnet/minecraft/class_3300;Ljava/util/stream/Stream;Lnet/minecraft/class_3695;I)Lnet/minecraft/class_1059$class_4007;");

	@Override
	public void fix(ClassNode optifine, ClassNode minecraft) {
		for (MethodNode methodNode : optifine.methods) {
			if (methodNode.name.equals(stitchName)) {
				for (LocalVariableNode localVariable : methodNode.localVariables) {
					if (localVariable.name.equals("locsEmissive")) {
						//Make this a HashSet and not just a Set so mixin only has 1 target
						localVariable.desc = "Ljava/util/HashSet;";
					}
				}
			}
		}
	}
}
