package dev.stormix.client;

import net.fabricmc.api.ClientModInitializer;

public class StormixClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		System.out.println("[Stormix] Client geladen");
	}
}
