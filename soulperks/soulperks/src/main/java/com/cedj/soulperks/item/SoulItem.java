package com.cedj.soulperks.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class SoulItem extends Item {
	public SoulItem(Settings settings) {
		super(settings);
	}

	public static ItemStack create(PlayerEntity victim) {
		ItemStack stack = new ItemStack(ModItems.SOUL);
		stack.set(ModComponents.SOUL_OWNER, victim.getGameProfile().getName());
		return stack;
	}

	public static String ownerName(ItemStack stack) {
		String owner = stack.get(ModComponents.SOUL_OWNER);
		return owner != null ? owner : "???";
	}

	@Override
	public Text getName(ItemStack stack) {
		String owner = stack.get(ModComponents.SOUL_OWNER);
		return owner == null ? super.getName(stack) : Text.translatable("item.soulperks.soul.named", owner);
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		tooltip.add(Text.translatable("item.soulperks.soul.tooltip").formatted(Formatting.GRAY, Formatting.ITALIC));
	}
}
