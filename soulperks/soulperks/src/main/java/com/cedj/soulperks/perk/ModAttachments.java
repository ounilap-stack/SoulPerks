package com.cedj.soulperks.perk;

import com.cedj.soulperks.SoulPerks;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

@SuppressWarnings("UnstableApiUsage")
public final class ModAttachments {
	/** Saved with the player data. Death handling is done manually in SoulEvents (COPY_FROM). */
	public static final AttachmentType<PerkData> PERK_DATA = AttachmentRegistry.<PerkData>builder()
			.persistent(PerkData.CODEC)
			.initializer(() -> PerkData.EMPTY)
			.buildAndRegister(SoulPerks.id("perk_data"));

	private ModAttachments() {}

	public static void register() {}
}
