package me.modmuss50.optifabric.mod.asm;

import me.modmuss50.optifabric.mod.OptifineInjector;
import me.modmuss50.optifabric.mod.OptifineSetup;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

public class MixinPlugin implements IMixinConfigPlugin {

	static {
		try {
			OptifineSetup optifineSetup = new OptifineSetup();
			File file = optifineSetup.getPatchedJar();

			URLUtils.addURL.accept(file.toURI().toURL());

			OptifineInjector injector = new OptifineInjector(new JarFile(file));
			injector.setup();
		} catch (Throwable e) {
			throw new RuntimeException("Failed to setup optifine", e);
		}
	}

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
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
