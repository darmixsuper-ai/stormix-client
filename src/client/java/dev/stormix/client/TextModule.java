package dev.stormix.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Eine einfache Textanzeige mit dunklem Hintergrund. */
public abstract class TextModule extends HudModule {
	protected TextModule(String id, String name, String description, int x, int y, boolean enabled) {
		super(id, name, description, x, y, enabled);
	}

	protected abstract String text(Minecraft mc);

	@Override
	public int width(Minecraft mc) {
		return mc.font.width(this.text(mc)) + 8;
	}

	@Override
	public int height(Minecraft mc) {
		return 14;
	}

	@Override
	public void render(GuiGraphicsExtractor g, Minecraft mc, int px, int py) {
		String t = this.text(mc);
		g.fill(px, py, px + mc.font.width(t) + 8, py + 14, 0x90000000);
		g.text(mc.font, t, px + 4, py + 3, 0xFFFFFFFF, true);
	}
}
