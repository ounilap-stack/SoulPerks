package com.cedj.soulperks.perk;

import com.cedj.soulperks.SoulPerks;
import com.mojang.serialization.Codec;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The 12 perks. Every perk stacks up to {@link #maxStacks()}.
 * Attribute perks are applied as persistent attribute modifiers (value = amount * stacks).
 * LIFESTEAL and REGENERATION are handled in {@link com.cedj.soulperks.event.SoulEvents}.
 */
public enum Perk implements StringIdentifiable {
	VITALITY("vitality", 10, 0xFF5555,
			bonus(EntityAttributes.GENERIC_MAX_HEALTH, 2.0, Operation.ADD_VALUE)),
	SWIFTNESS("swiftness", 5, 0x55FFFF,
			bonus(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.05, Operation.ADD_MULTIPLIED_BASE)),
	MIGHT("might", 10, 0xFF8844,
			bonus(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0, Operation.ADD_VALUE)),
	FEROCITY("ferocity", 5, 0xFFAA00,
			bonus(EntityAttributes.GENERIC_ATTACK_SPEED, 0.10, Operation.ADD_MULTIPLIED_BASE)),
	IRON_SKIN("iron_skin", 10, 0xBBBBBB,
			bonus(EntityAttributes.GENERIC_ARMOR, 2.0, Operation.ADD_VALUE)),
	FORTITUDE("fortitude", 5, 0x5599FF,
			bonus(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 1.0, Operation.ADD_VALUE)),
	STEADFAST("steadfast", 5, 0x886644,
			bonus(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.15, Operation.ADD_VALUE),
			bonus(EntityAttributes.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE, 0.15, Operation.ADD_VALUE)),
	REACH("reach", 4, 0xAA88FF,
			bonus(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE, 0.5, Operation.ADD_VALUE),
			bonus(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE, 0.5, Operation.ADD_VALUE)),
	EXCAVATOR("excavator", 5, 0xFFFF55,
			bonus(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED, 0.20, Operation.ADD_MULTIPLIED_BASE)),
	FEATHERWEIGHT("featherweight", 5, 0xEEEEFF,
			bonus(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 2.0, Operation.ADD_VALUE),
			bonus(EntityAttributes.GENERIC_JUMP_STRENGTH, 0.05, Operation.ADD_MULTIPLIED_BASE)),
	LIFESTEAL("lifesteal", 5, 0xAA0000),
	REGENERATION("regeneration", 5, 0xFF55FF);

	public static final Codec<Perk> CODEC = StringIdentifiable.createCodec(Perk::values);

	/** Lifesteal: fraction of melee damage dealt that is healed, per stack. */
	public static final float LIFESTEAL_PER_STACK = 0.04f;
	/** Regeneration: health healed every {@link #REGEN_INTERVAL_TICKS}, per stack. */
	public static final float REGEN_PER_STACK = 1.0f;
	public static final int REGEN_INTERVAL_TICKS = 100;

	private final String id;
	private final int maxStacks;
	private final int color;
	private final List<AttributeBonus> bonuses;

	Perk(String id, int maxStacks, int color, AttributeBonus... bonuses) {
		this.id = id;
		this.maxStacks = maxStacks;
		this.color = color;
		this.bonuses = List.of(bonuses);
	}

	public String id() { return id; }
	public int maxStacks() { return maxStacks; }
	public int color() { return color; }
	public List<AttributeBonus> bonuses() { return bonuses; }

	public String nameKey() { return "perk.soulperks." + id; }
	public String descKey() { return "perk.soulperks." + id + ".desc"; }

	public Identifier modifierId(int bonusIndex) {
		return SoulPerks.id("perk/" + id + "/" + bonusIndex);
	}

	@Override
	public String asString() { return id; }

	@Nullable
	public static Perk byId(String id) {
		for (Perk p : values()) if (p.id.equals(id)) return p;
		return null;
	}

	private static AttributeBonus bonus(RegistryEntry<EntityAttribute> attribute, double amount, Operation op) {
		return new AttributeBonus(attribute, amount, op);
	}

	public record AttributeBonus(RegistryEntry<EntityAttribute> attribute, double amountPerStack, Operation operation) {}
}
