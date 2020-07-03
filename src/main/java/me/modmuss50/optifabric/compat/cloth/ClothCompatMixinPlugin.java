package me.modmuss50.optifabric.compat.cloth;

import me.modmuss50.optifabric.util.RemappingUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ClothCompatMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return false;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return Collections.singletonList("DummyMixinGameRenderer");
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		String renderName = RemappingUtils.getMethodName("class_757", "method_3192", "(FJZ)V");
		String gameRendererName = RemappingUtils.getClassName("class_757");
		String screenName = RemappingUtils.getClassName("class_437");


		if (mixinClassName.equals("me.modmuss50.optifabric.compat.cloth.mixin.DummyMixinGameRenderer")) {
			for (MethodNode method : targetClass.methods) {
				if(method.name.equals(renderName)) {
					List<AbstractInsnNode> nukeList = new ArrayList<>();
					for (int i = 0; i < method.instructions.size(); i++) {
						AbstractInsnNode node = method.instructions.get(i);
						if (node instanceof MethodInsnNode) {
							MethodInsnNode methodInsnNode = (MethodInsnNode) node;
							if (methodInsnNode.getOpcode() != Opcodes.INVOKEVIRTUAL) {
								continue; // Nope
							}
							if (!methodInsnNode.owner.equals(gameRendererName)) {
								continue; // Not that lucky
							}
							// this most likely the mixin im after, if not well not sure what what I can do about it
							if (!methodInsnNode.name.contains("$renderScreen$")) {
								continue; /// Still nope
							}
							// We should finally have the method call we are after, lets nuke it
							nukeList.add(methodInsnNode);
							while (true) {
								i--;
								AbstractInsnNode nextNode = method.instructions.get(i);
								if (nextNode instanceof MethodInsnNode) {
									MethodInsnNode previousMethodInst = (MethodInsnNode) nextNode;
									if (previousMethodInst.owner.equals(screenName)) {
										break; // Prob time to stop here
									}
								}
								nukeList.add(nextNode); // Goodbye
							}
							break;
						}
					}
					nukeList.forEach((i) -> method.instructions.remove(i)); // See ya!
				}
			}
		}
	}
}
