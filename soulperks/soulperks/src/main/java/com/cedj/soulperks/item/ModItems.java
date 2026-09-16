package com.cedj.soulperks.item;

import com.cedj.soulperks.SoulPerks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
	public static final Item SOUL = Registry.register(
			Registries.ITEM,
			SoulPerks.id("soul"),
			new SoulItem(new Item.Settings().maxCount(16).rarity(Rarity.EPIC).fireproof())
	);

	private ModItems() {}

	public static void register() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> entries.add(SOUL));
	}
}
