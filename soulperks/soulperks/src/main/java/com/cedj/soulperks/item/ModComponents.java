package com.cedj.soulperks.item;

import com.cedj.soulperks.SoulPerks;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModComponents {
	/** Name of the player this soul was taken from. */
	public static final ComponentType<String> SOUL_OWNER = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			SoulPerks.id("soul_owner"),
			ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build()
	);

	private ModComponents() {}

	public static void register() {
		// Forces class loading so the static field is registered.
	}
}
