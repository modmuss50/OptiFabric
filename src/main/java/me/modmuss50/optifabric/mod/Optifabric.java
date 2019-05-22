package me.modmuss50.optifabric.mod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.ChatFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.menu.YesNoScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.SystemUtil;

public class Optifabric implements ModInitializer {

	@Override
	public void onInitialize() {

	}

	public static void checkForErrors() {
		if (!OptifineVersion.validOptifine) {
			String helpPage = "https://gist.github.com/modmuss50/4b2dbfc3488a0a1f1e72d037406f77af";
			String error = "OptiFabric could not find the Optifine extracted jar file in the mods folder. \n\n Would you like to open the help page?";
			if (OptifineVersion.isInstaller) {
				error = "You have not extracted the Optifine mod using the installer \n\n Would you like to open the help page?";
				helpPage = "https://gist.github.com/modmuss50/be44623562b6a0bac1bf8bef6d835a5f";
			}
			String finalHelpPage = helpPage;
			YesNoScreen yesNoScreen = new YesNoScreen(t -> {
				if (t) {
					SystemUtil.getOperatingSystem().open(finalHelpPage);
				} else {
					MinecraftClient.getInstance().scheduleStop();
				}
			}, new TextComponent(ChatFormat.RED + "There was an error finding Optifine in the mods folder!"), new TextComponent(error), ChatFormat.GREEN + "Open Help", ChatFormat.RED + "Close Game");

			MinecraftClient.getInstance().openScreen(yesNoScreen);
		}
	}
}
