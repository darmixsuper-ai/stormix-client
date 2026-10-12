package dev.stormix.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class StormixClient implements ClientModInitializer {
	private static boolean lastShift;

	@Override
	public void onInitializeClient() {
		Modules.load();

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("stormixclient", "modules"), (g, deltaTracker) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) {
				return;
			}
			for (HudModule m : Modules.ALL) {
				if (m.enabled && m.hasHud()) {
					m.render(g, mc, m.x, m.y);
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			boolean down = InputConstants.isKeyDown(mc.getWindow(), 344);
			if (down && !lastShift && mc.player != null && mc.gui.screen() == null) {
				mc.gui.setScreen(new MenuScreen());
			}
			lastShift = down;
			for (HudModule m : Modules.ALL) {
				m.tick(mc);
			}
		});
	}
}
