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
		if (OptifineVersion.error.isPresent()) {
			YesNoScreen yesNoScreen = new YesNoScreen(t -> {
				if (t) {
					SystemUtil.getOperatingSystem().open(OptifineVersion.error.get().getRight());
				} else {
					MinecraftClient.getInstance().scheduleStop();
				}
			}, new TextComponent(ChatFormat.RED + "There was an error finding Optifine in the mods folder!"), new TextComponent(OptifineVersion.error.get().getLeft()), ChatFormat.GREEN + "Open Help", ChatFormat.RED + "Close Game");

			MinecraftClient.getInstance().openScreen(yesNoScreen);
		}
	}
}
