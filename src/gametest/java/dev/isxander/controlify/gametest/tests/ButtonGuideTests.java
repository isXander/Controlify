/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.impl.contextual.GuideLocation;
import dev.isxander.controlify.impl.contextual.*;
import dev.isxander.controlify.gametest.framework.CEntityTypes;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.TestUnitWorldContext;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.impl.gui.guide.GuideInstanceImpl;
import dev.isxander.controlify.impl.utils.CUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class ButtonGuideTests implements FabricClientGameTest, ClientModInitializer {
	private static final Identifier OVERRIDE_PACK = CTestUtil.id("guide_override_test");

	@Override
	public void onInitializeClient() {
		CTestUtil.registerBuiltinResourcePack(OVERRIDE_PACK);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		var controlify = new ControlifyGameTestContext(context);

		try (var controller = controlify.virtualControllerBuilder().withXbox().attach();
			var world = new TestUnitWorldContext(context);
			var region = world.allocateRegion(5, 5, 5)) {
			var villager = region.spawnWithNoFreeWill(CEntityTypes.VILLAGER, new BlockPos(1, 1, 3));
			context.waitTicks(2);

			var villagerPosition = region.computeOnServer((_, _) -> villager.getBoundingBox().getCenter());
			context.runOnClient(minecraft -> minecraft.player.lookAt(EntityAnchorArgument.Anchor.EYES, villagerPosition));
			context.waitTicks(2);

			context.runOnClient(minecraft -> {
				if (!(minecraft.hitResult instanceof EntityHitResult)) {
					throw new AssertionError("Expected the villager to be the current entity hit result, got " + minecraft.hitResult);
				}

				var domain = (ContextualDomainImpl<InGameContext>) ContextualDomains.INSTANCE.inGame();
				var ruleEngine = RuleSetManager.GUIDE_RULES
						.getRuleEngine(minecraft.level, CUtil.rl("in_game"))
						.orElseThrow();
				assertRuleLocation(ruleEngine, "inventory", GuideLocation.LEFT);
				assertRuleLocation(ruleEngine, "radial_menu", GuideLocation.LEFT);
				var inGameContext = InGameContext.create(minecraft, controller.getControllerImpl());
				var state = domain.calculateState(inGameContext, ruleEngine.factDependencies());
				assertFact(state, "looking_at_entity");
				assertFact(state, "looking_at_merchant");

				var matchingRules = ruleEngine.evaluate(state);
				assertActionRule(matchingRules, "attack");
				assertActionRule(matchingRules, "use");

				var guide = (GuideInstanceImpl<InGameContext>) domain.createGuideInstance(minecraft.font);
				guide.update(inGameContext);
				if (guide.rightGuides().lines().stream().noneMatch(line -> line.text().getString().contains("Trade"))) {
					throw new AssertionError("Expected the rendered right guide to contain Trade: " + guide.rightGuides().lines());
				}
			});

			try (var _ = CTestUtil.applyResourcePack(context, OVERRIDE_PACK)) {
				context.runOnClient(minecraft -> {
					var ruleEngine = RuleSetManager.GUIDE_RULES
							.getRuleEngine(minecraft.level, CUtil.rl("in_game"))
							.orElseThrow();

					Set<String> expectedActions = Set.of("jump", "sneak", "drop", "inventory", "attack", "use");
					Set<String> actualActions = ruleEngine.rules().stream()
							.map(rule -> rule.action().actionId())
							.map(id -> id.getPath())
							.collect(Collectors.toSet());
					if (!actualActions.equals(expectedActions)) {
						throw new AssertionError("Expected the test guide rules to replace the defaults. Actions: " + actualActions);
					}

					assertRuleLocation(ruleEngine, "jump", GuideLocation.LEFT);
					assertRuleLocation(ruleEngine, "sneak", GuideLocation.LEFT);
					assertRuleLocation(ruleEngine, "drop", GuideLocation.LEFT);
					assertRuleLocation(ruleEngine, "inventory", GuideLocation.RIGHT);
					assertRuleLocation(ruleEngine, "attack", GuideLocation.RIGHT);
					assertRuleLocation(ruleEngine, "use", GuideLocation.RIGHT);
				});
			}
		}

		controlify.resetSettings();
	}

	private static void assertFact(ContextualState state, String path) {
		var id = CUtil.rl(path);
		if (!state.facts().getOrDefault(id, false)) {
			throw new AssertionError("Expected contextual fact '" + id + "' to be true. Facts: " + state.facts());
		}
	}

	private static void assertActionRule(
			List<GuideRule> rules,
			String actionPath
	) {
		var actionId = CUtil.rl(actionPath);
		if (rules.stream().noneMatch(rule -> rule.action().actionId().equals(actionId))) {
			throw new AssertionError("Expected a matching guide rule for '" + actionId + "': " + rules);
		}
	}

	private static void assertRuleLocation(
			RuleEngine<GuideRule.Key, GuideRule> ruleEngine,
			String actionPath,
			GuideLocation expectedLocation
	) {
		var actionId = CUtil.rl(actionPath);
		List<GuideRule> rules = ruleEngine.rules().stream()
				.filter(rule -> rule.action().actionId().equals(actionId))
				.toList();
		if (rules.isEmpty() || rules.stream().anyMatch(rule -> rule.location() != expectedLocation)) {
			throw new AssertionError(
					"Expected all guide rules for '" + actionId + "' to be on " + expectedLocation + ": " + rules
			);
		}
	}
}
