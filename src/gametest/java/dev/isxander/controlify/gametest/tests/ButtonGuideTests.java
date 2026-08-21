/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.tests;

import dev.isxander.controlify.api.contextual.InGameContext;
import dev.isxander.controlify.api.contextual.GuideLocation;
import dev.isxander.controlify.contextual.*;
import dev.isxander.controlify.gametest.framework.CEntityTypes;
import dev.isxander.controlify.gametest.framework.CTestUtil;
import dev.isxander.controlify.gametest.framework.TestUnitWorldContext;
import dev.isxander.controlify.gametest.framework.controller.ControlifyGameTestContext;
import dev.isxander.controlify.gui.guide.GuideInstanceImpl;
import dev.isxander.controlify.utils.CUtil;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class ButtonGuideTests implements FabricClientGameTest {
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
				var inGameContext = InGameContext.create(minecraft, controller.getControllerEntity());
				var state = domain.calculateState(inGameContext, ruleEngine.factDependencies());
				assertFact(state, "looking_at_entity");
				assertFact(state, "looking_at_merchant");

				var matchingRules = ruleEngine.evaluate(state);
				assertBindingRule(matchingRules, "attack");
				assertBindingRule(matchingRules, "use");

				var guide = (GuideInstanceImpl<InGameContext>) domain.createGuideInstance(minecraft.font);
				guide.update(inGameContext);
				if (guide.rightGuides().lines().stream().noneMatch(line -> line.text().getString().contains("Trade"))) {
					throw new AssertionError("Expected the rendered right guide to contain Trade: " + guide.rightGuides().lines());
				}
			});

			try (var _ = CTestUtil.applyResourcePack(context, CUtil.rl("legacy_console"))) {
				context.runOnClient(minecraft -> {
					var ruleEngine = RuleSetManager.GUIDE_RULES
							.getRuleEngine(minecraft.level, CUtil.rl("in_game"))
							.orElseThrow();

					Set<String> expectedBindings = Set.of("jump", "sneak", "drop", "inventory", "attack", "use");
					Set<String> actualBindings = ruleEngine.rules().stream()
							.map(rule -> rule.binding().bindId())
							.map(id -> id.getPath())
							.collect(Collectors.toSet());
					if (!actualBindings.equals(expectedBindings)) {
						throw new AssertionError("Expected the Legacy Console guide rules to replace the defaults. Bindings: " + actualBindings);
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

	private static void assertBindingRule(
			List<GuideRule> rules,
			String bindingPath
	) {
		var bindingId = CUtil.rl(bindingPath);
		if (rules.stream().noneMatch(rule -> rule.binding().bindId().equals(bindingId))) {
			throw new AssertionError("Expected a matching guide rule for '" + bindingId + "': " + rules);
		}
	}

	private static void assertRuleLocation(
			RuleEngine<GuideRule.Key, GuideRule> ruleEngine,
			String bindingPath,
			GuideLocation expectedLocation
	) {
		var bindingId = CUtil.rl(bindingPath);
		List<GuideRule> rules = ruleEngine.rules().stream()
				.filter(rule -> rule.binding().bindId().equals(bindingId))
				.toList();
		if (rules.isEmpty() || rules.stream().anyMatch(rule -> rule.location() != expectedLocation)) {
			throw new AssertionError(
					"Expected all guide rules for '" + bindingId + "' to be on " + expectedLocation + ": " + rules
			);
		}
	}
}
