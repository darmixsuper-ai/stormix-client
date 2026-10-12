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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class Modules {
	public static final List<HudModule> ALL = new ArrayList<>();

	private Modules() {
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
		ALL.add(new HudModule("keystrokes", "Tasten-Anzeige", "Zeigt WASD und die Maustasten, wenn du sie drueckst.", 4, 110, true) {
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
		ALL.add(new HudModule("armor", "Ruestungs-Status", "Zeigt die Haltbarkeit deiner Ruestung an.", 4, 180, true) {
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
		ALL.add(new HudModule("togglesprint", "Toggle Sprint", "Sprinten ohne die Taste gedrueckt zu halten.", 0, 0, false) {
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
