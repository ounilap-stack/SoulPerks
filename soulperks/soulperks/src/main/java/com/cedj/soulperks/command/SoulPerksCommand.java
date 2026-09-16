package com.cedj.soulperks.command;

import com.cedj.soulperks.perk.Perk;
import com.cedj.soulperks.perk.PerkData;
import com.cedj.soulperks.perk.PerkManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * /soulperks list              - show your perks
 * /soulperks reopen            - reopen a pending choice screen
 * /soulperks test              - (op) simulate a soul falling into the void
 * /soulperks reset <player>    - (op) wipe a player's perks
 */
public final class SoulPerksCommand {
	private SoulPerksCommand() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				literal("soulperks")
						.then(literal("list").executes(ctx -> list(ctx.getSource(), ctx.getSource().getPlayerOrThrow())))
						.then(literal("reopen").executes(ctx -> {
							PerkManager.sendCurrentOffer(ctx.getSource().getPlayerOrThrow());
							return 1;
						}))
						.then(literal("test").requires(s -> s.hasPermissionLevel(2)).executes(ctx -> {
							PerkManager.consumeSoul(ctx.getSource().getPlayerOrThrow(), "Test Dummy");
							return 1;
						}))
						.then(literal("reset").requires(s -> s.hasPermissionLevel(2))
								.then(argument("target", EntityArgumentType.player()).executes(ctx -> {
									ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
									PerkManager.reset(target);
									ctx.getSource().sendFeedback(() -> Text.translatable("command.soulperks.reset", target.getName()), true);
									return 1;
								})))
		));
	}

	private static int list(ServerCommandSource source, ServerPlayerEntity player) {
		PerkData data = PerkManager.get(player);
		if (data.levels().isEmpty()) {
			source.sendFeedback(() -> Text.translatable("command.soulperks.none"), false);
			return 0;
		}
		source.sendFeedback(() -> Text.translatable("command.soulperks.header", data.totalStacks()), false);
		for (Perk perk : Perk.values()) {
			int lvl = data.level(perk);
			if (lvl <= 0) continue;
			source.sendFeedback(() -> Text.literal(" • ")
					.append(Text.translatable(perk.nameKey()).withColor(perk.color()))
					.append(Text.literal(" " + lvl + "/" + perk.maxStacks())), false);
		}
		return data.totalStacks();
	}
}
