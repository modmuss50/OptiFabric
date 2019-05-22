package me.modmuss50.optifabric.mod.asm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.function.Consumer;

public class URLUtils {

	public static final Consumer<URL> addURL;

	static {
		ClassLoader loader = MixinPlugin.class.getClassLoader();
		Method addUrlMethod = null;
		for (Method method : loader.getClass().getDeclaredMethods()) {
			/*System.out.println("Type: " + method.getReturnType());
			System.out.println("Params: " + method.getParameterCount() + ", " + Arrays.toString(method.getParameterTypes()));*/
			if (method.getReturnType() == Void.TYPE && method.getParameterCount() == 1 && method.getParameterTypes()[0] == URL.class) {
				addUrlMethod = method; //Probably
				break;
			}
		}
		if (addUrlMethod == null)
			throw new IllegalStateException("Couldn't find method in " + loader);
		try {
			addUrlMethod.setAccessible(true);
			MethodHandle handle = MethodHandles.lookup().unreflect(addUrlMethod);
			addURL = url -> {
				try {
					handle.invoke(loader, url);
				} catch (Throwable t) {
					throw new RuntimeException("Unexpected error adding URL", t);
				}
			};
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Couldn't get handle for " + addUrlMethod, e);
		}
	}

}
