package dev.stormix.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Das Stormix-Menue. Oeffnen und Schliessen mit Rechts-Shift. */
public class MenuScreen extends Screen {
	private static final int ACCENT = 0xFF8F82F5;
	private static final int SIDEBAR = 96;
	private static final String[] TABS = {"Mods", "Cosmetics", "Info"};

	private int tab;
	private String search = "";
	private double scroll;
	private HudModule dragging;
	private int dragDx;
	private int dragDy;
	private int px;
	private int py;
	private int pw;
	private int ph;

	public MenuScreen() {
		super(Component.literal("Stormix Client"));
	}

	@Override
	protected void init() {
		this.pw = Math.min(560, this.width - 30);
		this.ph = Math.min(310, this.height - 30);
		this.px = (this.width - this.pw) / 2;
		this.py = (this.height - this.ph) / 2;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		Modules.save();
		super.onClose();
	}

	private List<HudModule> visibleModules() {
		List<HudModule> out = new ArrayList<>();
		String q = this.search.toLowerCase(Locale.ROOT);
		for (HudModule m : Modules.ALL) {
			if (q.isEmpty() || m.name.toLowerCase(Locale.ROOT).contains(q)) {
				out.add(m);
			}
		}
		return out;
	}

	private int cardW() {
		return (this.pw - SIDEBAR - 24 - 8) / 2;
	}

	private int cardX(int i) {
		return this.px + SIDEBAR + 12 + (i % 2) * (this.cardW() + 8);
	}

	private int cardY(int i) {
		return this.py + 46 + (i / 2) * 48 - (int) this.scroll;
	}

	private int maxScroll(int count) {
		int rows = (count + 1) / 2;
		return Math.max(0, rows * 48 - (this.ph - 56));
	}

	private boolean inPanel(double mx, double my) {
		return mx >= this.px && mx <= this.px + this.pw && my >= this.py && my <= this.py + this.ph;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float a) {
		super.extractRenderState(g, mx, my, a);
		Minecraft mc = this.minecraft;

		g.fill(this.px, this.py, this.px + this.pw, this.py + this.ph, 0xE60C0C12);
		g.outline(this.px, this.py, this.pw, this.ph, 0x40FFFFFF);
		g.fill(this.px, this.py, this.px + SIDEBAR, this.py + this.ph, 0xF0090910);
		g.text(mc.font, "STORMIX", this.px + 12, this.py + 14, ACCENT, true);

		for (int i = 0; i < TABS.length; i++) {
			int ty = this.py + 40 + i * 24;
			boolean on = i == this.tab;
			boolean hover = mx >= this.px && mx < this.px + SIDEBAR && my >= ty && my < ty + 22;
			if (on) {
				g.fill(this.px + 6, ty, this.px + SIDEBAR - 6, ty + 22, 0x408F82F5);
				g.fill(this.px + 6, ty, this.px + 8, ty + 22, ACCENT);
			} else if (hover) {
				g.fill(this.px + 6, ty, this.px + SIDEBAR - 6, ty + 22, 0x20FFFFFF);
			}
			g.text(mc.font, TABS[i], this.px + 16, ty + 7, on ? 0xFFFFFFFF : 0xFFA09CB5, false);
		}
		g.text(mc.font, "Rechts-Shift", this.px + 12, this.py + this.ph - 14, 0xFF6E6A85, false);

		int cx = this.px + SIDEBAR + 12;
		g.text(mc.font, TABS[this.tab], cx, this.py + 16, 0xFFFFFFFF, true);

		if (this.tab == 0) {
			int sx = this.px + this.pw - 142;
			g.fill(sx, this.py + 10, sx + 130, this.py + 28, 0x30FFFFFF);
			boolean empty = this.search.isEmpty();
			g.text(mc.font, empty ? "Suchen ..." : this.search + "_", sx + 6, this.py + 15, empty ? 0xFF8A86A0 : 0xFFFFFFFF, false);

			List<HudModule> list = this.visibleModules();
			this.scroll = Math.max(0, Math.min(this.scroll, this.maxScroll(list.size())));
			g.enableScissor(this.px + SIDEBAR, this.py + 40, this.px + this.pw, this.py + this.ph - 6);
			for (int i = 0; i < list.size(); i++) {
				HudModule m = list.get(i);
				int x = this.cardX(i);
				int y = this.cardY(i);
				int w = this.cardW();
				boolean hover = mx >= x && mx < x + w && my >= y && my < y + 40 && my > this.py + 40 && my < this.py + this.ph - 6;
				g.fill(x, y, x + w, y + 40, hover ? 0x40FFFFFF : 0x28FFFFFF);
				g.text(mc.font, m.name, x + 8, y + 8, 0xFFFFFFFF, true);
				g.text(mc.font, mc.font.plainSubstrByWidth(m.description, w - 52), x + 8, y + 22, 0xFF9692AD, false);
				int tx = x + w - 32;
				g.fill(tx, y + 8, tx + 24, y + 20, m.enabled ? ACCENT : 0xFF3A3A48);
				int kx = m.enabled ? tx + 14 : tx + 2;
				g.fill(kx, y + 10, kx + 8, y + 18, 0xFFFFFFFF);
			}
			g.disableScissor();
		} else if (this.tab == 1) {
			g.text(mc.font, "Eigene Cosmetics kommen als Naechstes:", cx, this.py + 50, 0xFFFFFFFF, false);
			g.text(mc.font, "Capes, Huete und Fluegel. Alle gratis.", cx, this.py + 66, 0xFF9692AD, false);
		} else {
			g.text(mc.font, "Stormix Client 0.1.0", cx, this.py + 50, 0xFFFFFFFF, false);
			g.text(mc.font, "Rechts-Shift oeffnet und schliesst dieses Menue.", cx, this.py + 70, 0xFF9692AD, false);
			g.text(mc.font, "Anzeigen verschieben: Menue offen lassen und die", cx, this.py + 86, 0xFF9692AD, false);
			g.text(mc.font, "Anzeige ausserhalb des Menues mit der Maus ziehen.", cx, this.py + 98, 0xFF9692AD, false);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x();
		double my = event.y();
		Minecraft mc = this.minecraft;
		if (event.button() != 0) {
			return super.mouseClicked(event, doubleClick);
		}
		if (!this.inPanel(mx, my)) {
			for (int i = Modules.ALL.size() - 1; i >= 0; i--) {
				HudModule m = Modules.ALL.get(i);
				if (m.enabled && m.hasHud() && mx >= m.x && mx <= m.x + m.width(mc) && my >= m.y && my <= m.y + m.height(mc)) {
					this.dragging = m;
					this.dragDx = (int) mx - m.x;
					this.dragDy = (int) my - m.y;
					return true;
				}
			}
			return super.mouseClicked(event, doubleClick);
		}
		for (int i = 0; i < TABS.length; i++) {
			int ty = this.py + 40 + i * 24;
			if (mx >= this.px && mx < this.px + SIDEBAR && my >= ty && my < ty + 22) {
				this.tab = i;
				this.scroll = 0;
				return true;
			}
		}
		if (this.tab == 0 && my > this.py + 40 && my < this.py + this.ph - 6) {
			List<HudModule> list = this.visibleModules();
			for (int i = 0; i < list.size(); i++) {
				int x = this.cardX(i);
				int y = this.cardY(i);
				if (mx >= x && mx < x + this.cardW() && my >= y && my < y + 40) {
					list.get(i).toggle(mc);
					Modules.save();
					return true;
				}
			}
		}
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.dragging != null) {
			this.dragging.x = Math.max(0, (int) event.x() - this.dragDx);
			this.dragging.y = Math.max(0, (int) event.y() - this.dragDy);
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.dragging != null) {
			this.dragging = null;
			Modules.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (this.tab == 0) {
			this.scroll = Math.max(0, Math.min(this.scroll - scrollY * 24, this.maxScroll(this.visibleModules().size())));
			return true;
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == 344) {
			this.onClose();
			return true;
		}
		if (this.tab == 0 && event.key() == 259 && !this.search.isEmpty()) {
			this.search = this.search.substring(0, this.search.length() - 1);
			this.scroll = 0;
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (this.tab == 0 && event.isAllowedChatCharacter() && this.search.length() < 24) {
			this.search += event.codepointAsString();
			this.scroll = 0;
			return true;
		}
		return super.charTyped(event);
	}
}
