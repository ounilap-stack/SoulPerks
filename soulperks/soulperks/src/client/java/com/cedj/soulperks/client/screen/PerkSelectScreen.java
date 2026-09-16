package com.cedj.soulperks.client.screen;

import com.cedj.soulperks.network.ChoosePerkPayload;
import com.cedj.soulperks.network.OfferPerksPayload;
import com.cedj.soulperks.perk.Perk;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class PerkSelectScreen extends Screen {
	private static final int CARD_W = 124;
	private static final int CARD_H = 130;
	private static final int GAP = 12;

	private final List<Perk> perks = new ArrayList<>();
	private final List<Integer> levels = new ArrayList<>();
	private final int queued;
	private boolean chosen = false;

	public PerkSelectScreen(OfferPerksPayload offer) {
		super(Text.translatable("screen.soulperks.title"));
		for (int i = 0; i < offer.perks().size(); i++) {
			Perk perk = Perk.byId(offer.perks().get(i));
			if (perk == null) continue; // version mismatch safety
			perks.add(perk);
			levels.add(i < offer.levels().size() ? offer.levels().get(i) : 0);
		}
		this.queued = offer.queued();
	}

	private int cardsLeft() {
		int total = perks.size() * CARD_W + Math.max(0, perks.size() - 1) * GAP;
		return (width - total) / 2;
	}

	private int cardsTop() {
		return (height - CARD_H) / 2 + 10;
	}

	@Override
	protected void init() {
		int left = cardsLeft();
		int top = cardsTop();
		for (int i = 0; i < perks.size(); i++) {
			final int index = i;
			int x = left + i * (CARD_W + GAP);
			addDrawableChild(ButtonWidget.builder(Text.translatable("screen.soulperks.choose"), b -> choose(index))
					.dimensions(x + 12, top + CARD_H - 28, CARD_W - 24, 20)
					.build());
		}
	}

	private void choose(int index) {
		if (chosen) return;
		chosen = true;
		ClientPlayNetworking.send(new ChoosePerkPayload(index));
		close();
	}

	@Override
	public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.renderBackground(ctx, mouseX, mouseY, delta); // blur + dim (only call once per frame!)

		int left = cardsLeft();
		int top = cardsTop();

		ctx.drawCenteredTextWithShadow(textRenderer,
				title.copy().formatted(Formatting.DARK_PURPLE, Formatting.BOLD), width / 2, top - 34, 0xFFFFFF);
		if (queued > 1) {
			ctx.drawCenteredTextWithShadow(textRenderer,
					Text.translatable("screen.soulperks.queued", queued - 1), width / 2, top - 20, 0xAAAAAA);
		}

		for (int i = 0; i < perks.size(); i++) {
			Perk perk = perks.get(i);
			int level = levels.get(i);
			int x = left + i * (CARD_W + GAP);
			int cx = x + CARD_W / 2;
			boolean hovered = mouseX >= x && mouseX < x + CARD_W && mouseY >= top && mouseY < top + CARD_H;

			ctx.fill(x, top, x + CARD_W, top + CARD_H, 0xDD140A24);
			ctx.drawBorder(x, top, CARD_W, CARD_H, hovered ? (0xFF000000 | perk.color()) : 0xFF4A2F70);

			ctx.drawCenteredTextWithShadow(textRenderer,
					Text.translatable(perk.nameKey()).formatted(Formatting.BOLD), cx, top + 8, perk.color());
			ctx.drawCenteredTextWithShadow(textRenderer,
					Text.translatable("screen.soulperks.level", level, level + 1, perk.maxStacks()),
					cx, top + 22, 0x999999);

			int y = top + 38;
			for (OrderedText line : textRenderer.wrapLines(Text.translatable(perk.descKey()), CARD_W - 14)) {
				ctx.drawTextWithShadow(textRenderer, line, x + 7, y, 0xDDDDDD);
				y += 10;
			}
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false; // a choice must be made (use /soulperks reopen if the screen gets lost)
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
