package com.cedj.soulperks.network;

import com.cedj.soulperks.SoulPerks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.List;

/** S2C: "choose one of these perks". levels = current stacks of each offered perk. */
public record OfferPerksPayload(List<String> perks, List<Integer> levels, int queued) implements CustomPayload {
	public static final Id<OfferPerksPayload> ID = new Id<>(SoulPerks.id("offer_perks"));
	public static final PacketCodec<RegistryByteBuf, OfferPerksPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.STRING.collect(PacketCodecs.toList()), OfferPerksPayload::perks,
			PacketCodecs.VAR_INT.collect(PacketCodecs.toList()), OfferPerksPayload::levels,
			PacketCodecs.VAR_INT, OfferPerksPayload::queued,
			OfferPerksPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
