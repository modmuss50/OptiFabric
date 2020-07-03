package me.modmuss50.optifabric.mixin;

import me.modmuss50.optifabric.mod.Optifabric;
import me.modmuss50.optifabric.mod.OptifabricError;
import me.modmuss50.optifabric.mod.OptifineVersion;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {

	@Shadow
	@Final
	private boolean doBackgroundFade;

	@Shadow
	private long backgroundFadeStart;

	protected MixinTitleScreen(Text component_1) {
		super(component_1);
	}

	@Inject(method = "init", at = @At("RETURN"))
	private void init(CallbackInfo info) {
		Optifabric.checkForErrors();
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		if (!OptifabricError.hasError()) {
			float fadeTime = this.doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0F : 1.0F;
			float fadeColor = this.doBackgroundFade ? MathHelper.clamp(fadeTime - 1.0F, 0.0F, 1.0F) : 1.0F;

			int int_6 = MathHelper.ceil(fadeColor * 255.0F) << 24;
			if ((int_6 & -67108864) != 0) {
				this.drawStringWithShadow(matrices, this.textRenderer, OptifineVersion.version, 2, this.height - 20, 16777215 | int_6);
			}
		}
	}

}
