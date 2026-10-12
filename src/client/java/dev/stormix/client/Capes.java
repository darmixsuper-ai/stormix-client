package dev.stormix.client;

import java.util.List;
import net.minecraft.resources.Identifier;

/** Die Capes von Stormix. Alle gratis. Sie sind nur fuer dich sichtbar. */
public final class Capes {
	public record Cape(String id, String name) {
	}

	public static final List<Cape> ALL = List.of(
		new Cape("stormix", "Stormix"),
		new Cape("violett", "Violett"),
		new Cape("flamme", "Flamme"),
		new Cape("ozean", "Ozean"),
		new Cape("matrix", "Matrix"),
		new Cape("gold", "Gold"),
		new Cape("regenbogen", "Regenbogen"),
		new Cape("schweiz", "Schweiz")
	);

	/** Leer bedeutet: kein Stormix-Cape. */
	public static String selected = "";

	private Capes() {
	}

	/** Pfad fuer die Vorschau im Menue. */
	public static Identifier preview(String id) {
		return Identifier.fromNamespaceAndPath("stormixclient", "textures/cape/" + id + ".png");
	}

	/** Kennung, die Minecraft fuer das Cape benutzt. */
	public static Identifier asset(String id) {
		return Identifier.fromNamespaceAndPath("stormixclient", "cape/" + id);
	}
}
