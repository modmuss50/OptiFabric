package me.modmuss50.optifabric.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class RemappingUtils {

	private static final MappingResolver reslover = FabricLoader.getInstance().getMappingResolver();
	private static final String INTERMEDIARY = "intermediary";

	public static String fromIntermediary(String className) {
		return fromIntermediaryDot(className).replaceAll("\\.", "/");
	}
	public static String fromIntermediaryDot(String className) {
		return reslover.mapClassName(INTERMEDIARY, "net.minecraft." + className);
	}

	public static String getMethodName(String owner, String methodName, String desc) {
		return reslover.mapMethodName(INTERMEDIARY, "net.minecraft." + owner, methodName, desc);
	}

}
