package com.cedj.soulperks.network;

import com.cedj.soulperks.perk.PerkManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class ModNetworking {
	private ModNetworking() {}

	public static void register() {
		PayloadTypeRegistry.playS2C().register(OfferPerksPayload.ID, OfferPerksPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ChoosePerkPayload.ID, ChoosePerkPayload.CODEC);

		// Runs on the server thread.
		ServerPlayNetworking.registerGlobalReceiver(ChoosePerkPayload.ID,
				(payload, context) -> PerkManager.choose(context.player(), payload.index()));
	}
}
