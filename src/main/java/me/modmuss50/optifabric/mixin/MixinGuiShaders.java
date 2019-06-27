package me.modmuss50.optifabric.mixin;

import me.modmuss50.optifabric.mod.ShaderHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

//Shows a warning when shaders are enabled
@Pseudo
@Mixin(targets = "net.optifine.shaders.gui.GuiShaders")
public abstract class MixinGuiShaders extends Screen {

	protected MixinGuiShaders(Text text_1) {
		super(text_1);
	}

	@Inject(method = "render", at = @At("RETURN"), remap = false)
	private void render(int int_1, int int_2, float float_1, CallbackInfo ci){
		if(ShaderHelper.isShadersEnabled()){
			drawString(font, "The indigo rendering pipeline has been disabled", 10, 5, Color.RED.getRGB());
			drawString(font, "Disable shaders before reporting issues.", 10, 15, Color.RED.getRGB());
		}
	}


}
