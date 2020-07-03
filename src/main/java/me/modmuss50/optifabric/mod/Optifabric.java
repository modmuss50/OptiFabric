package me.modmuss50.optifabric.mod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class Optifabric implements ModInitializer {

	public static void checkForErrors() {
		if (OptifabricError.hasError()) {
			ConfirmScreen confirmScreen = new ConfirmScreen(t -> {
				if (t) {
					Util.getOperatingSystem().open(OptifabricError.getErrorURL());
				} else {
					MinecraftClient.getInstance().scheduleStop();
				}
			}, new LiteralText(Formatting.RED + "There was an error loading OptiFabric!"), new LiteralText(OptifabricError.getError()),  new LiteralText(OptifabricError.getHelpButtonText()).formatted(Formatting.GREEN), new LiteralText("Close Game").formatted(Formatting.RED));

			MinecraftClient.getInstance().openScreen(confirmScreen);
		}
	}

	@Override
	public void onInitialize() {

	}
}
