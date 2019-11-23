package me.modmuss50.optifabric.patcher;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;


public class ChunkRendererFix {

	//This removes the small change that optifine made to ChunkRenderer that is only required by forge
	public static void fix(ClassNode classNode){
		MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();


		for(MethodNode methodNode : classNode.methods){
			for (int i = 0; i < methodNode.instructions.size(); i++) {
				AbstractInsnNode insnNode = methodNode.instructions.get(i);
				if(insnNode instanceof MethodInsnNode){
					MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
					if(methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL){
						if(methodInsnNode.name.equals("renderBlock")){

							/*

							class_2680 - net/minecraft/block/BlockState
							class_2338 - net/minecraft/util/math/BlockPos
							class_1920 - net/minecraft/world/ExtendedBlockView
							class_287  - net/minecraft/client/render/BufferBuilder

							 */

							String desc = "(Lclass_2680;Lclass_2338;Lclass_1920;Lclass_287;Ljava/util/Random;)Z";
							String name = mappingResolver.mapMethodName("intermediary", "net.minecraft.class_776", "method_3355", desc.replaceAll("Lclass_", "Lnet/minecraft/class_"));

							//Horrible but works
							desc = desc.replace("class_2680", mappingResolver.mapClassName("intermediary", "net.minecraft.class_2680").replaceAll("\\.", "/"));
							desc = desc.replace("class_2338", mappingResolver.mapClassName("intermediary", "net.minecraft.class_2338").replaceAll("\\.", "/"));
							desc = desc.replace("class_1920", mappingResolver.mapClassName("intermediary", "net.minecraft.class_1920").replaceAll("\\.", "/"));
							desc = desc.replace("class_287", mappingResolver.mapClassName("intermediary", "net.minecraft.class_287").replaceAll("\\.", "/"));


							System.out.println(String.format("Replacement `renderBlock` call:  %s.%s", name, desc));

							//tesselateBlock.(Lnet.minecraft.block.BlockState;Lnet.minecraft.util.math.BlockPos;Lclass_1920;Lnet.minecraft.client.render.BufferBuilder;Ljava/util/Random;)Z
							//tesselateBlock.(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/ExtendedBlockView;Lnet/minecraft/client/render/BufferBuilder;Ljava/util/Random;)Z"


							//Replaces the method call with the vanilla one, this calls down to the same method just without the forge model data
							methodInsnNode.name = name;
							methodInsnNode.desc = desc;


							//Remove the model data call
							methodNode.instructions.remove(methodNode.instructions.get(i -1));
						}
					}
				}
			}
		}
	}
	
}
