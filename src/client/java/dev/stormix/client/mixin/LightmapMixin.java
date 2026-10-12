package dev.stormix.client.mixin;

import dev.stormix.client.Modules;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapMixin {
	@Inject(method = "extract", at = @At("TAIL"))
	private void stormix$fullbright(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
		if (Modules.isOn("fullbright")) {
			renderState.brightness = 15.0F;
		}
	}
}
