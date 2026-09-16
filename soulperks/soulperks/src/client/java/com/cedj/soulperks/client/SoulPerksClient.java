package com.cedj.soulperks.client;

import com.cedj.soulperks.client.screen.PerkSelectScreen;
import com.cedj.soulperks.network.OfferPerksPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SoulPerksClient implements ClientModInitializer {
	/** Offer waiting to be shown; opened as soon as no other screen is open. */
	private static OfferPerksPayload pendingOffer;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(OfferPerksPayload.ID,
				(payload, context) -> pendingOffer = payload);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (pendingOffer != null && client.player != null && client.currentScreen == null) {
				client.setScreen(new PerkSelectScreen(pendingOffer));
				pendingOffer = null;
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> pendingOffer = null);
	}
}
