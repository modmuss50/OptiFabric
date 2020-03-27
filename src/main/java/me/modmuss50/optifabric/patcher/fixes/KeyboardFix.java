package me.modmuss50.optifabric.patcher.fixes;

import me.modmuss50.optifabric.util.RemappingUtils;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class KeyboardFix implements ClassFixer {
	//net/minecraft/client/Keyboard.onKey(JIIII)V
	private String onKeyName = RemappingUtils.getMethodName("class_309", "method_1466", "(JIIII)V");

	@Override
	public void fix(ClassNode optifine, ClassNode minecraft) {
		Validate.notNull(onKeyName, "onKeyName null");

		//Remove the old "broken" method
		optifine.methods.removeIf(methodNode -> methodNode.name.equals(onKeyName));

		//Find the vanilla method
		MethodNode methodNode = minecraft.methods.stream().filter(methodNode1 -> methodNode1.name.equals(onKeyName)).findFirst().orElse(null);
		Validate.notNull(methodNode, "old method null");

		//Add the vanilla method back in
		optifine.methods.add(methodNode);

		Function<MethodNode, Set<String>> findAnonymousMethods = methodNode12 -> {
			Set<String> anonymousMethods1 = new HashSet<>();
			//Find all speical anonymous method calls, a more direct and targeted approach to the lambada fixer
			for (AbstractInsnNode instruction : methodNode12.instructions) {
				if (instruction instanceof InvokeDynamicInsnNode) {
					InvokeDynamicInsnNode dynamicInsnNode = (InvokeDynamicInsnNode) instruction;
					for (Object bsmArg : dynamicInsnNode.bsmArgs) {
						if (bsmArg instanceof Handle) {
							Handle handle = (Handle) bsmArg;
							if (handle.getTag() == Opcodes.H_INVOKESPECIAL) {
								anonymousMethods1.add(handle.getName());
							}
						}
					}
				}
			}
			return anonymousMethods1;
		};

		Set<String> anonymousMethods = new HashSet<>(findAnonymousMethods.apply(methodNode));

		//Now recursively look into those anonymous methods for more!
		long added = 1;
		while (added > 0) {
			List<String> temp = new ArrayList<>();
			for (String anonymousMethodName : anonymousMethods) {
				for (MethodNode anonymousMethod : minecraft.methods) {
					if (anonymousMethod.name.equals(anonymousMethodName)) {
						Set<String> names = findAnonymousMethods.apply(anonymousMethod);
						added = names.stream().filter(s -> !anonymousMethods.contains(s)).count();
						temp.addAll(names);
					}
				}
			}
			anonymousMethods.addAll(temp);
		}

		//Copy all the synthetic methods back in that we possibly broke by replacing the method
		for (MethodNode method : minecraft.methods) {
			if ((method.access & Opcodes.ACC_SYNTHETIC) != 0) {
				boolean found = false;
				for (MethodNode node : optifine.methods) {
					if (node.name.equals(method.name)) {
						found = true;
						break;
					}
				}
				//Didnt find the method in optifines classes, we detected it as a possible candidate for being lost, so lets add it back
				if (!found && anonymousMethods.contains(method.name)) {
					optifine.methods.add(method);
				}
			}
		}

	}
}
