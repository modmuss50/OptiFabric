package me.modmuss50.optifabric.mod;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

//A class used to extract the optifine jar from the installer
public class OptifineInstaller {

	public static void extract(File installer, File output, File minecrafJar) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, MalformedURLException {
		System.out.println("Running optifine patcher");
		ClassLoader classLoader = new URLClassLoader(new URL[]{installer.toURI().toURL()}, OptifineInstaller.class.getClassLoader());
		Class clazz = classLoader.loadClass("optifine.Patcher");
		Method method = clazz.getDeclaredMethod("process", File.class, File.class, File.class);
		method.invoke(null, minecrafJar, installer, output);
	}

}
