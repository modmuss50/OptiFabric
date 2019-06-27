package me.modmuss50.optifabric.mod;

import net.fabricmc.indigo.Indigo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ShaderHelper {

	private static boolean shadersEnabled;

	private static List<Consumer<Boolean>> changeConsumers = new ArrayList<>();

	public static boolean isShadersEnabled(){
		return shadersEnabled;
	}

	public static void toggleHandler(Consumer<Boolean> consumer){
		changeConsumers.add(consumer);
	}

	public static void changeShaderState(boolean enabled){
		shadersEnabled = enabled;
		changeConsumers.forEach(booleanConsumer -> booleanConsumer.accept(shadersEnabled));
	}

	public static void indigoFixSetup(){
		ShaderHelper.toggleHandler(new Consumer<Boolean>() {
			Method method = null;

			@Override
			public void accept(Boolean aBoolean) {
				try {
					if (method == null){
						method = Indigo.class.getDeclaredMethod("optifabric_ShaderChange", boolean.class);
						method.setAccessible(true);
					}
					method.invoke(null, aBoolean);
				} catch (Exception e){
					throw new RuntimeException(e);
				}
			}
		});
	}

}
