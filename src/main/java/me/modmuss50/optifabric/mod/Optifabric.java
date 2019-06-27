package me.modmuss50.optifabric.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.util.SystemUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class Optifabric implements ModInitializer {

	public static void checkForErrors() {
		if (OptifineVersion.error != null) {
			ConfirmScreen confirmScreen = new ConfirmScreen(t -> {
				if (t) {
					SystemUtil.getOperatingSystem().open("https://github.com/modmuss50/OptiFabric/blob/master/README.md");
				} else {
					MinecraftClient.getInstance().scheduleStop();
				}
			}, new LiteralText(Formatting.RED + "There was an error finding Optifine in the mods folder!"), new LiteralText(OptifineVersion.error), Formatting.GREEN + "Open Help", Formatting.RED + "Close Game");

			MinecraftClient.getInstance().openScreen(confirmScreen);
		}
	}

	@Override
	public void onInitialize() {
		if(FabricLoader.getInstance().isModLoaded("fabric-renderer-indigo")){
			ShaderHelper.indigoFixSetup();
		}
	}
}
