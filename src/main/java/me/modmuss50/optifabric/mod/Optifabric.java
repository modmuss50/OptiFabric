package me.modmuss50.optifabric.mod;

import net.minecraft.ChatFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.SystemUtil;

public class Optifabric {

	public static void checkForErrors() {
		if (OptifineVersion.error != null) {
			ConfirmScreen confirmScreen = new ConfirmScreen(t -> {
				if (t) {
					SystemUtil.getOperatingSystem().open("https://github.com/modmuss50/OptiFabric/blob/master/README.md");
				} else {
					MinecraftClient.getInstance().scheduleStop();
				}
			}, new TextComponent(ChatFormat.RED + "There was an error finding Optifine in the mods folder!"), new TextComponent(OptifineVersion.error), ChatFormat.GREEN + "Open Help", ChatFormat.RED + "Close Game");

			MinecraftClient.getInstance().openScreen(confirmScreen);
		}
	}
}
