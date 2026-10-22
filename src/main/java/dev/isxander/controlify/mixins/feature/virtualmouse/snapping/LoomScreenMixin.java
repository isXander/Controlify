/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.api.screenop.SnapPoint;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Consumer;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin extends AbstractContainerScreen<LoomMenu> implements ScreenBehaviourFactory<LoomScreen> {

	@Unique
	private static final int COLUMNS = 4, ROWS = 4, SLOTS = COLUMNS * ROWS;
	@Unique
	private static final int BTN_SIZE = 14;

	@Shadow
	private boolean displayPatterns;
	@Shadow
	private int startRow;

	// This math is based on the renderBg method in LoomScreen
	protected LoomScreenMixin(LoomMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public ScreenBehaviour create(@NonNull ScreenContext<? extends LoomScreen> context) {
		return new ScreenBehaviour() {
			@Override
			public void collectSnapPoints(Consumer<SnapPoint> consumer, Runnable next) {
				next.run();

				if (!LoomScreenMixin.this.displayPatterns) {
					return;
				}
				// Get list of available patterns
				List<Holder<BannerPattern>> patterns = LoomScreenMixin.this.menu.getSelectablePatterns();

				// Early exit if no patterns are available
				if (patterns.isEmpty()) {
					return;
				}

				int numPatterns = patterns.size();

				int gridLeft = LoomScreenMixin.this.leftPos + 60;
				int gridTop = LoomScreenMixin.this.topPos + 14;

				// Determine the range of *absolute* pattern indices to consider.
				// The first index corresponds to the top-left corner of the grid.
				int firstIndex = LoomScreenMixin.this.startRow * COLUMNS;

				// The last potential index is SLOTS slots after the first one,
				// but cannot exceed the number of available patterns
				int endPatternIndexExclusive = Math.min(
						numPatterns,       // cannot exceed the number of available patterns
						firstIndex + SLOTS // cannot exceed the number of slots in the grid
				);

				for (int patternIndex = firstIndex; patternIndex < endPatternIndexExclusive; patternIndex++) {
					// Calculate the grid row and column based on the absolute index
					// These calculations effectively reverse the patternIndex = absoluteRow * COLS + col formula
					int absoluteRow = patternIndex / COLUMNS;
					int gridColumn = patternIndex % COLUMNS;

					// Calculate the row relative to the visible grid
					int gridRow = absoluteRow - LoomScreenMixin.this.startRow;

					// Calculate the screen position in the centre of the button
					int buttonX = gridLeft + gridColumn * BTN_SIZE + BTN_SIZE / 2;
					int buttonY = gridTop + gridRow * BTN_SIZE + BTN_SIZE / 2;

					// Add the snap point for this slot
					consumer.accept(new SnapPoint(buttonX, buttonY, BTN_SIZE / 2));
				}

			}
		};
	}

}
