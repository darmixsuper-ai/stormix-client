package dev.stormix.client;

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
			boolean down = Compat.keyDown(mc, 344);
			if (down && !lastShift && mc.player != null && Compat.screen(mc) == null) {
				Compat.setScreen(mc, new MenuScreen());
			}
			lastShift = down;
			for (HudModule m : Modules.ALL) {
				m.tick(mc);
			}
		});
	}
}
