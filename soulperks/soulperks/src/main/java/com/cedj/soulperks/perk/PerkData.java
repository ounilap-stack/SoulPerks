package com.cedj.soulperks.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable per-player perk state.
 *
 * @param levels  current stack count of every owned perk
 * @param pending queue of offers (each a list of up to 3 perks) not yet chosen
 */
public record PerkData(Map<Perk, Integer> levels, List<List<Perk>> pending) {
	public static final PerkData EMPTY = new PerkData(Map.of(), List.of());

	public static final Codec<PerkData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(Perk.CODEC, Codec.INT).optionalFieldOf("levels", Map.of()).forGetter(PerkData::levels),
			Perk.CODEC.listOf().listOf().optionalFieldOf("pending", List.of()).forGetter(PerkData::pending)
	).apply(i, PerkData::new));

	public int level(Perk perk) {
		return levels.getOrDefault(perk, 0);
	}

	public int totalStacks() {
		return levels.values().stream().mapToInt(Integer::intValue).sum();
	}

	public PerkData withLevel(Perk perk, int level) {
		Map<Perk, Integer> copy = new EnumMap<>(Perk.class);
		copy.putAll(levels);
		if (level <= 0) copy.remove(perk); else copy.put(perk, level);
		return new PerkData(Map.copyOf(copy), pending);
	}

	public PerkData withOfferQueued(List<Perk> offer) {
		List<List<Perk>> copy = new ArrayList<>(pending);
		copy.add(List.copyOf(offer));
		return new PerkData(levels, List.copyOf(copy));
	}

	public PerkData withFirstOfferRemoved() {
		if (pending.isEmpty()) return this;
		return new PerkData(levels, List.copyOf(pending.subList(1, pending.size())));
	}

	public PerkData withoutLevels() {
		return new PerkData(Map.of(), pending);
	}
}
