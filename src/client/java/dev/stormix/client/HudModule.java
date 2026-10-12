package dev.stormix.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Ein Modul: eine Anzeige im Spiel oder eine Funktion, die man im Menue ein- und ausschaltet. */
public abstract class HudModule {
	public final String id;
	public final String name;
	public final String description;
	public boolean enabled;
	public int x;
	public int y;

	protected HudModule(String id, String name, String description, int x, int y, boolean enabled) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.x = x;
		this.y = y;
		this.enabled = enabled;
	}

	/** Gibt es etwas auf dem Bildschirm zu zeichnen? */
	public boolean hasHud() {
		return true;
	}

	public int width(Minecraft mc) {
		return 0;
	}

	public int height(Minecraft mc) {
		return 0;
	}

	public void render(GuiGraphicsExtractor g, Minecraft mc, int px, int py) {
	}

	public void tick(Minecraft mc) {
	}

	public void toggle(Minecraft mc) {
		this.enabled = !this.enabled;
		if (!this.enabled) {
			this.onDisabled(mc);
		}
	}

	public void onDisabled(Minecraft mc) {
	}
}
