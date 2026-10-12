package dev.stormix.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class Modules {
	public static final List<HudModule> ALL = new ArrayList<>();

	private static final long START = System.currentTimeMillis();

	private Modules() {
	}

	public static boolean isOn(String id) {
		for (HudModule m : ALL) {
			if (m.id.equals(id)) {
				return m.enabled;
			}
		}
		return false;
	}

	static {
		ALL.add(new TextModule("fps", "FPS", "Zeigt deine Bilder pro Sekunde an.", 4, 4, true) {
			@Override
			protected String text(Minecraft mc) {
				return "FPS: " + mc.getFps();
			}
		});
		ALL.add(new TextModule("ping", "Ping", "Zeigt deinen Ping zum Server an.", 4, 20, false) {
			@Override
			protected String text(Minecraft mc) {
				if (mc.player == null || mc.getConnection() == null) {
					return "Ping: -";
				}
				PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
				return info == null ? "Ping: -" : "Ping: " + info.getLatency() + " ms";
			}
		});
		ALL.add(new TextModule("coords", "Koordinaten", "Zeigt die Koordinaten, an denen du dich gerade befindest.", 4, 36, true) {
			@Override
			protected String text(Minecraft mc) {
				if (mc.player == null) {
					return "XYZ: -";
				}
				return String.format(Locale.ROOT, "XYZ: %.0f / %.0f / %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
			}
		});
		ALL.add(new TextModule("speed", "Speedometer", "Zeigt deine Geschwindigkeit in Metern pro Sekunde.", 4, 52, false) {
			@Override
			protected String text(Minecraft mc) {
				if (mc.player == null) {
					return "Speed: -";
				}
				Vec3 d = mc.player.getDeltaMovement();
				return String.format(Locale.ROOT, "Speed: %.1f m/s", Math.sqrt(d.x * d.x + d.z * d.z) * 20.0);
			}
		});
		ALL.add(new TextModule("clock", "Uhrzeit", "Zeigt die aktuelle Uhrzeit an.", 4, 68, false) {
			private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

			@Override
			protected String text(Minecraft mc) {
				return LocalTime.now().format(this.fmt);
			}
		});
		ALL.add(new TextModule("cps", "CPS", "Zeigt, wie oft du pro Sekunde klickst.", 4, 84, false) {
			private final ArrayDeque<Long> clicks = new ArrayDeque<>();
			private boolean last;

			@Override
			protected String text(Minecraft mc) {
				long now = System.currentTimeMillis();
				boolean down = mc.options.keyAttack.isDown();
				if (down && !this.last) {
					this.clicks.add(now);
				}
				this.last = down;
				while (!this.clicks.isEmpty() && now - this.clicks.peekFirst() > 1000L) {
					this.clicks.pollFirst();
				}
				return "CPS: " + this.clicks.size();
			}
		});
		ALL.add(new HudModule("keystrokes", "Tasten-Anzeige", "Zeigt WASD und die Maustasten, wenn du sie drückst.", 4, 110, true) {
			@Override
			public int width(Minecraft mc) {
				return 64;
			}

			@Override
			public int height(Minecraft mc) {
				return 64;
			}

			private void key(GuiGraphicsExtractor g, Minecraft mc, int x, int y, int w, int h, String s, boolean down) {
				g.fill(x, y, x + w, y + h, down ? 0xC0FFFFFF : 0x90000000);
				g.text(mc.font, s, x + (w - mc.font.width(s)) / 2, y + (h - 8) / 2, down ? 0xFF101018 : 0xFFFFFFFF, false);
			}

			@Override
			public void render(GuiGraphicsExtractor g, Minecraft mc, int px, int py) {
				this.key(g, mc, px + 22, py, 20, 20, "W", mc.options.keyUp.isDown());
				this.key(g, mc, px, py + 22, 20, 20, "A", mc.options.keyLeft.isDown());
				this.key(g, mc, px + 22, py + 22, 20, 20, "S", mc.options.keyDown.isDown());
				this.key(g, mc, px + 44, py + 22, 20, 20, "D", mc.options.keyRight.isDown());
				this.key(g, mc, px, py + 44, 31, 20, "LMB", mc.options.keyAttack.isDown());
				this.key(g, mc, px + 33, py + 44, 31, 20, "RMB", mc.options.keyUse.isDown());
			}
		});
		ALL.add(new HudModule("armor", "Rüstungs-Status", "Zeigt die Haltbarkeit deiner Rüstung an.", 4, 180, true) {
			private final EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

			@Override
			public int width(Minecraft mc) {
				return 70;
			}

			@Override
			public int height(Minecraft mc) {
				return 72;
			}

			@Override
			public void render(GuiGraphicsExtractor g, Minecraft mc, int px, int py) {
				if (mc.player == null) {
					return;
				}
				for (int i = 0; i < this.slots.length; i++) {
					ItemStack s = mc.player.getItemBySlot(this.slots[i]);
					if (s.isEmpty()) {
						continue;
					}
					int yy = py + i * 18;
					g.item(s, px, yy);
					if (s.isDamageableItem()) {
						String t = (s.getMaxDamage() - s.getDamageValue()) + "/" + s.getMaxDamage();
						g.text(mc.font, t, px + 20, yy + 5, 0xFFFFFFFF, true);
					}
				}
			}
		});
		ALL.add(new TextModule("day", "Tag-Zähler", "Zeigt, wie viele Minecraft-Tage diese Welt schon läuft.", 4, 100, false) {
			@Override
			protected String text(Minecraft mc) {
				return mc.level == null ? "Tag: -" : "Tag: " + (mc.level.getGameTime() / 24000L + 1L);
			}
		});
		ALL.add(new TextModule("uptime", "Spielzeit", "Zeigt, wie lange du schon im Spiel bist.", 4, 116, false) {
			@Override
			protected String text(Minecraft mc) {
				long sec = (System.currentTimeMillis() - START) / 1000L;
				return String.format(Locale.ROOT, "Online: %d:%02d:%02d", sec / 3600, (sec / 60) % 60, sec % 60);
			}
		});
		ALL.add(new TextModule("ram", "Speicher", "Zeigt, wie viel Arbeitsspeicher das Spiel benutzt.", 4, 132, false) {
			@Override
			protected String text(Minecraft mc) {
				Runtime r = Runtime.getRuntime();
				return "RAM: " + ((r.totalMemory() - r.freeMemory()) / 1048576L) + " MB";
			}
		});
		ALL.add(new TextModule("direction", "Richtung", "Zeigt, in welche Himmelsrichtung du schaust.", 4, 148, false) {
			private final String[] names = {"Süd", "Südwest", "West", "Nordwest", "Nord", "Nordost", "Ost", "Südost"};

			@Override
			protected String text(Minecraft mc) {
				if (mc.player == null) {
					return "Richtung: -";
				}
				int i = Math.floorMod(Math.round(mc.player.getYRot() / 45.0F), 8);
				return "Richtung: " + this.names[i];
			}
		});
		ALL.add(new TextModule("light", "Lichtlevel", "Zeigt das Licht von Blöcken an deiner Position. Unter 8 spawnen Monster.", 4, 164, false) {
			@Override
			protected String text(Minecraft mc) {
				if (mc.player == null || mc.level == null) {
					return "Licht: -";
				}
				return "Licht: " + mc.level.getBrightness(LightLayer.BLOCK, mc.player.blockPosition());
			}
		});
		ALL.add(new TextModule("saturation", "Sättigung", "Zeigt deine versteckte Sättigung. Wichtig fürs Heilen.", 4, 180, false) {
			@Override
			protected String text(Minecraft mc) {
				if (mc.player == null) {
					return "Sättigung: -";
				}
				return String.format(Locale.ROOT, "Sättigung: %.1f", mc.player.getFoodData().getSaturationLevel());
			}
		});
		ALL.add(new TextModule("itemcount", "Item-Zähler", "Zählt, wie viele Stück vom Item in deiner Hand du insgesamt hast.", 4, 196, false) {
			@Override
			protected String text(Minecraft mc) {
				if (mc.player == null) {
					return "Items: -";
				}
				ItemStack held = mc.player.getInventory().getSelectedItem();
				if (held.isEmpty()) {
					return "Items: 0";
				}
				int n = 0;
				for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
					ItemStack s = mc.player.getInventory().getItem(i);
					if (!s.isEmpty() && s.is(held.getItem())) {
						n += s.getCount();
					}
				}
				return "Items: " + n;
			}
		});
		ALL.add(new HudModule("potions", "Trank-Status", "Zeigt deine aktiven Effekte mit Restzeit.", 4, 260, true) {
			private List<String> lines(Minecraft mc) {
				List<String> out = new ArrayList<>();
				if (mc.player == null) {
					return out;
				}
				for (MobEffectInstance e : mc.player.getActiveEffects()) {
					String n = e.getEffect().value().getDisplayName().getString();
					int lvl = e.getAmplifier() + 1;
					int d = e.getDuration();
					String t = d < 0 ? "∞" : String.format(Locale.ROOT, "%d:%02d", d / 1200, (d / 20) % 60);
					out.add(n + (lvl > 1 ? " " + lvl : "") + "  " + t);
				}
				return out;
			}

			@Override
			public int width(Minecraft mc) {
				int w = 0;
				for (String l : this.lines(mc)) {
					w = Math.max(w, mc.font.width(l) + 8);
				}
				return w;
			}

			@Override
			public int height(Minecraft mc) {
				return this.lines(mc).size() * 13;
			}

			@Override
			public void render(GuiGraphicsExtractor g, Minecraft mc, int px, int py) {
				int i = 0;
				for (String l : this.lines(mc)) {
					int y = py + i * 13;
					g.fill(px, y, px + mc.font.width(l) + 8, y + 12, 0x90000000);
					g.text(mc.font, l, px + 4, y + 2, 0xFFFFFFFF, true);
					i++;
				}
			}
		});
		ALL.add(new HudModule("fullbright", "Fullbright", "Macht alles hell, auch in Höhlen und nachts.", 0, 0, false) {
			@Override
			public boolean hasHud() {
				return false;
			}
		});
		ALL.add(new HudModule("zoom", "Zoom", "Halte die Taste C, um heranzuzoomen.", 0, 0, true) {
			private Integer oldFov;

			@Override
			public boolean hasHud() {
				return false;
			}

			@Override
			public void tick(Minecraft mc) {
				boolean want = this.enabled && mc.player != null && mc.gui.screen() == null
					&& InputConstants.isKeyDown(mc.getWindow(), 67);
				if (want && this.oldFov == null) {
					this.oldFov = mc.options.fov().get();
					mc.options.fov().set(30);
				} else if (!want && this.oldFov != null) {
					mc.options.fov().set(this.oldFov);
					this.oldFov = null;
				}
			}

			@Override
			public void onDisabled(Minecraft mc) {
				if (this.oldFov != null) {
					mc.options.fov().set(this.oldFov);
					this.oldFov = null;
				}
			}
		});
		ALL.add(new HudModule("togglesprint", "Toggle Sprint", "Sprinten ohne die Taste gedrückt zu halten.", 0, 0, false) {
			@Override
			public boolean hasHud() {
				return false;
			}

			@Override
			public void tick(Minecraft mc) {
				if (this.enabled && !mc.options.toggleSprint().get()) {
					mc.options.toggleSprint().set(true);
				}
			}

			@Override
			public void onDisabled(Minecraft mc) {
				mc.options.toggleSprint().set(false);
			}
		});
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("stormix-client.json");
	}

	public static void load() {
		try {
			Path p = file();
			if (!Files.exists(p)) {
				return;
			}
			JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
			if (root.has("_cape")) {
				Capes.selected = root.get("_cape").getAsString();
			}
			for (HudModule m : ALL) {
				if (root.has(m.id)) {
					JsonObject o = root.getAsJsonObject(m.id);
					m.enabled = o.get("enabled").getAsBoolean();
					m.x = o.get("x").getAsInt();
					m.y = o.get("y").getAsInt();
				}
			}
		} catch (Exception e) {
			System.out.println("[Stormix] Einstellungen konnten nicht gelesen werden: " + e);
		}
	}

	public static void save() {
		try {
			JsonObject root = new JsonObject();
			root.addProperty("_cape", Capes.selected);
			for (HudModule m : ALL) {
				JsonObject o = new JsonObject();
				o.addProperty("enabled", m.enabled);
				o.addProperty("x", m.x);
				o.addProperty("y", m.y);
				root.add(m.id, o);
			}
			Files.writeString(file(), new GsonBuilder().setPrettyPrinting().create().toJson(root));
		} catch (Exception e) {
			System.out.println("[Stormix] Einstellungen konnten nicht gespeichert werden: " + e);
		}
	}
}
