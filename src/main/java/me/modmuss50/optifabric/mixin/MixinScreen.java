package me.modmuss50.optifabric.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.CyclingOption;
import net.minecraft.client.options.Option;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.lang.reflect.Field;

@Mixin(Screen.class)
public class MixinScreen extends DrawableHelper {

	@Shadow protected TextRenderer font;

	private static CyclingOption RENDER_REGIONS;

	@Inject(method = "render", at = @At("RETURN"), remap = false)
	private void render(int int_1, int int_2, float float_1, CallbackInfo ci){
//		if(RENDER_REGIONS == null){
//			try {
//				Field field = Option.class.getDeclaredField("RENDER_REGIONS");
//				RENDER_REGIONS = (CyclingOption) field.get(null);
//			} catch (NoSuchFieldException | IllegalAccessException e) {
//				throw new RuntimeException(e);
//			}
//
//		}
//		boolean enabled = RENDER_REGIONS.getMessage(MinecraftClient.getInstance().options).endsWith("ON");
//		if(enabled){
//			drawString(font, "OptiFabric: Render Regions is not supported!", 10, 5, Color.RED.getRGB());
//		}
	}

}
