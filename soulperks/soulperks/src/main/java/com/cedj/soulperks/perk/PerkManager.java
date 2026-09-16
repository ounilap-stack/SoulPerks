package com.cedj.soulperks.perk;

import com.cedj.soulperks.SoulPerks;
import com.cedj.soulperks.network.OfferPerksPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class PerkManager {
	private PerkManager() {}

	public static PerkData get(ServerPlayerEntity player) {
		return player.getAttachedOrCreate(ModAttachments.PERK_DATA);
	}

	public static void set(ServerPlayerEntity player, PerkData data) {
		player.setAttached(ModAttachments.PERK_DATA, data);
	}

	/** Called when a soul owned by {@code player} falls into the void. */
	public static void consumeSoul(ServerPlayerEntity player, String victimName) {
		PerkData data = get(player);
		List<Perk> offer = rollOffer(data);

		if (offer.isEmpty()) {
			player.sendMessage(Text.translatable("message.soulperks.all_maxed").formatted(Formatting.GRAY), false);
			return;
		}

		boolean wasIdle = data.pending().isEmpty();
		set(player, data.withOfferQueued(offer));

		player.sendMessage(Text.translatable("message.soulperks.consumed", victimName).formatted(Formatting.DARK_PURPLE), false);
		player.getServerWorld().playSound(null, player.getBlockPos(),
				SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 0.6f);

		if (wasIdle) sendCurrentOffer(player);
	}

	/** Picks up to 3 distinct perks that are not maxed yet. */
	private static List<Perk> rollOffer(PerkData data) {
		List<Perk> pool = new ArrayList<>();
		for (Perk p : Perk.values()) {
			if (data.level(p) < p.maxStacks()) pool.add(p);
		}
		Collections.shuffle(pool);
		return List.copyOf(pool.subList(0, Math.min(SoulPerks.CHOICES_PER_SOUL, pool.size())));
	}

	/** Sends the offer at the head of the queue (if any) to the client, which opens the selection screen. */
	public static void sendCurrentOffer(ServerPlayerEntity player) {
		PerkData data = get(player);
		if (data.pending().isEmpty()) return;

		List<Perk> offer = data.pending().get(0);
		List<String> ids = offer.stream().map(Perk::id).toList();
		List<Integer> levels = offer.stream().map(data::level).toList();
		ServerPlayNetworking.send(player, new OfferPerksPayload(ids, levels, data.pending().size()));
	}

	/** Client picked option {@code index} of the current offer. */
	public static void choose(ServerPlayerEntity player, int index) {
		PerkData data = get(player);
		if (data.pending().isEmpty()) return;

		List<Perk> offer = data.pending().get(0);
		if (index < 0 || index >= offer.size()) return;

		Perk perk = offer.get(index);
		int newLevel = Math.min(data.level(perk) + 1, perk.maxStacks());
		set(player, data.withLevel(perk, newLevel).withFirstOfferRemoved());
		applyAttributes(player);

		player.sendMessage(Text.translatable("message.soulperks.chosen",
				Text.translatable(perk.nameKey()).withColor(perk.color()),
				newLevel, perk.maxStacks()), false);
		player.getServerWorld().playSound(null, player.getBlockPos(),
				SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.2f);

		sendCurrentOffer(player); // next queued soul, if any
	}

	/** (Re)applies every attribute modifier from the player's perk levels. Idempotent. */
	public static void applyAttributes(ServerPlayerEntity player) {
		PerkData data = get(player);
		for (Perk perk : Perk.values()) {
			int level = data.level(perk);
			List<Perk.AttributeBonus> bonuses = perk.bonuses();
			for (int i = 0; i < bonuses.size(); i++) {
				Perk.AttributeBonus bonus = bonuses.get(i);
				EntityAttributeInstance instance = player.getAttributeInstance(bonus.attribute());
				if (instance == null) continue;

				instance.removeModifier(perk.modifierId(i));
				if (level > 0) {
					instance.addPersistentModifier(new EntityAttributeModifier(
							perk.modifierId(i), bonus.amountPerStack() * level, bonus.operation()));
				}
			}
		}
		if (player.getHealth() > player.getMaxHealth()) {
			player.setHealth(player.getMaxHealth());
		}
	}

	public static void reset(ServerPlayerEntity player) {
		set(player, PerkData.EMPTY);
		applyAttributes(player);
	}
}
