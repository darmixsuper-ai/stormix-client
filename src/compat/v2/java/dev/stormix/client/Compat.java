package dev.stormix.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Kleine Unterschiede zwischen den Minecraft-Versionen. */
public final class Compat {
	private Compat() {
	}

	public static boolean keyDown(Minecraft mc, int key) {
		return InputConstants.isKeyDown(mc.getWindow(), key);
	}

	public static Screen screen(Minecraft mc) {
		return mc.gui.screen();
	}

	public static void setScreen(Minecraft mc, Screen screen) {
		mc.gui.setScreen(screen);
	}
}
