package com.cedj.soulperks.event;

import com.cedj.soulperks.SoulPerks;
import com.cedj.soulperks.item.ModItems;
import com.cedj.soulperks.item.SoulItem;
import com.cedj.soulperks.perk.Perk;
import com.cedj.soulperks.perk.PerkData;
import com.cedj.soulperks.perk.PerkManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SoulEvents {
	private SoulEvents() {}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DEATH.register(SoulEvents::onDeath);
		ServerTickEvents.END_WORLD_TICK.register(SoulEvents::checkVoidSouls);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % Perk.REGEN_INTERVAL_TICKS != 0) return;
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) tickRegen(p);
		});
		ServerLivingEntityEvents.AFTER_DAMAGE.register(SoulEvents::onDamage);

		// Persistence across death / login
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			PerkData data = PerkManager.get(oldPlayer);
			if (!alive && !SoulPerks.KEEP_PERKS_ON_DEATH) data = data.withoutLevels();
			PerkManager.set(newPlayer, data);
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			PerkManager.applyAttributes(newPlayer);
			if (!alive) newPlayer.setHealth(newPlayer.getMaxHealth());
			PerkManager.sendCurrentOffer(newPlayer);
		});
		ServerPlayerEvents.JOIN.register(player -> {
			PerkManager.applyAttributes(player);
			PerkManager.sendCurrentOffer(player); // un-chosen offers survive disconnects
		});
	}

	// --- Soul drop -------------------------------------------------------

	private static void onDeath(LivingEntity entity, DamageSource source) {
		if (!(entity instanceof ServerPlayerEntity victim)) return;
		ServerPlayerEntity killer = findKiller(victim, source);
		if (killer == null || killer == victim) return;

		ItemStack soul = SoulItem.create(victim);
		ServerWorld world = victim.getServerWorld();

		if (victim.getY() < world.getBottomY()) {
			// Victim was knocked into the void: the soul would be lost, hand it to the killer instead.
			killer.getInventory().offerOrDrop(soul);
		} else {
			ItemEntity drop = new ItemEntity(world, victim.getX(), victim.getY() + 0.5, victim.getZ(), soul);
			drop.setToDefaultPickupDelay();
			world.spawnEntity(drop);
		}

		killer.sendMessage(Text.translatable("message.soulperks.harvested", victim.getName())
				.formatted(Formatting.DARK_PURPLE), true);
	}

	@Nullable
	private static ServerPlayerEntity findKiller(ServerPlayerEntity victim, DamageSource source) {
		if (source.getAttacker() instanceof ServerPlayerEntity p) return p;
		// Covers void / fall deaths caused by a recent hit from another player.
		if (victim.getPrimeAdversary() instanceof ServerPlayerEntity p) return p;
		return null;
	}

	// --- Void detection --------------------------------------------------

	private static void checkVoidSouls(ServerWorld world) {
		int threshold = world.getBottomY() - 2;
		List<? extends ItemEntity> souls = world.getEntitiesByType(EntityType.ITEM,
				e -> e.isAlive() && e.getY() < threshold && e.getStack().isOf(ModItems.SOUL));

		for (ItemEntity itemEntity : souls) {
			ItemStack stack = itemEntity.getStack();
			Entity thrower = itemEntity.getOwner();

			if (thrower instanceof ServerPlayerEntity player) {
				String victim = SoulItem.ownerName(stack);
				for (int i = 0; i < stack.getCount(); i++) {
					PerkManager.consumeSoul(player, victim);
				}
			}
			itemEntity.discard();
		}
	}

	// --- Non-attribute perks ---------------------------------------------

	private static void onDamage(LivingEntity target, DamageSource source, float baseDamage, float damageTaken, boolean blocked) {
		if (blocked || damageTaken <= 0) return;
		if (!(source.getAttacker() instanceof ServerPlayerEntity attacker) || attacker == target) return;
		if (!source.isDirect()) return; // melee only

		int level = PerkManager.get(attacker).level(Perk.LIFESTEAL);
		if (level > 0) attacker.heal(damageTaken * Perk.LIFESTEAL_PER_STACK * level);
	}

	private static void tickRegen(ServerPlayerEntity player) {
		if (!player.isAlive() || player.getHealth() >= player.getMaxHealth()) return;
		int level = PerkManager.get(player).level(Perk.REGENERATION);
		if (level > 0) player.heal(Perk.REGEN_PER_STACK * level);
	}
}
