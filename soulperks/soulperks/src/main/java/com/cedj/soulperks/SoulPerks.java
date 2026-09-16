package com.cedj.soulperks;

import com.cedj.soulperks.command.SoulPerksCommand;
import com.cedj.soulperks.event.SoulEvents;
import com.cedj.soulperks.item.ModComponents;
import com.cedj.soulperks.item.ModItems;
import com.cedj.soulperks.network.ModNetworking;
import com.cedj.soulperks.perk.ModAttachments;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoulPerks implements ModInitializer {
	public static final String MOD_ID = "soulperks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** If false, a player loses all their perks when they die. */
	public static final boolean KEEP_PERKS_ON_DEATH = true;

	/** How many perk choices a soul offers. */
	public static final int CHOICES_PER_SOUL = 3;

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModComponents.register();
		ModItems.register();
		ModAttachments.register();
		ModNetworking.register();
		SoulEvents.register();
		SoulPerksCommand.register();
		LOGGER.info("Soul Perks loaded");
	}
}
