package com.cedj.soulperks.network;

import com.cedj.soulperks.SoulPerks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** C2S: index (0..2) of the perk chosen from the current offer. */
public record ChoosePerkPayload(int index) implements CustomPayload {
	public static final Id<ChoosePerkPayload> ID = new Id<>(SoulPerks.id("choose_perk"));
	public static final PacketCodec<RegistryByteBuf, ChoosePerkPayload> CODEC =
			PacketCodec.tuple(PacketCodecs.VAR_INT, ChoosePerkPayload::index, ChoosePerkPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
