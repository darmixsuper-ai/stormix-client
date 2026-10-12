package dev.stormix.client.mixin;

import dev.stormix.client.Capes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class PlayerSkinMixin {
	@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
	private void stormix$cape(CallbackInfoReturnable<PlayerSkin> cir) {
		if (Capes.selected.isEmpty()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || (Object) this != mc.player) {
			return;
		}
		PlayerSkin s = cir.getReturnValue();
		ClientAsset.ResourceTexture cape = new ClientAsset.ResourceTexture(Capes.asset(Capes.selected));
		cir.setReturnValue(new PlayerSkin(s.body(), cape, cape, s.model(), s.secure()));
	}
}
