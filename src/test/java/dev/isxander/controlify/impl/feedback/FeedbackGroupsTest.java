/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// How `groups.json` files from several packs merge into memberships and ranks.
class FeedbackGroupsTest {
	private static final FeedbackGroup AIMING = group("aiming");
	private static final FeedbackGroup ITEMS = group("items");
	private static final FeedbackGroup WORLD = group("world");
	private static final FeedbackGroup PLAYER = group("player");
	private static final FeedbackGroup GUI = group("gui");

	private final List<String> warnings = new ArrayList<>();
	private final FeedbackGroups.Reporter reporter = new FeedbackGroups.Reporter() {
		@Override
		public void warn(String message) {
			FeedbackGroupsTest.this.warnings.add(message);
		}

		@Override
		public void debug(String message) {
		}
	};

	private static FeedbackGroup group(String path) {
		return new FeedbackGroup(Identifier.fromNamespaceAndPath("test", path));
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("test", path);
	}

	/// Packs from lowest to highest priority, each written with `'` for `"`.
	private FeedbackGroups resolve(String... packs) {
		List<FeedbackGroups.PackFile> files = new ArrayList<>();
		for (int i = 0; i < packs.length; i++) {
			files.add(new FeedbackGroups.PackFile("pack" + i, JsonParser.parseString(packs[i].replace('\'', '"'))));
		}
		return FeedbackGroups.resolve(files, this.reporter);
	}

	private static List<FeedbackGroup> groupsOf(FeedbackGroups groups, String effect) {
		return groups.membership(id(effect), FeedbackGroup.UNGROUPED).groups();
	}

	// --- membership ---

	@Test
	void packsAddToEachOther() {
		FeedbackGroups groups = resolve(
				"{'groups': {'test:items': ['test:bow']}}",
				"{'groups': {'test:aiming': ['test:bow']}}"
		);

		assertEquals(List.of(AIMING, ITEMS), groupsOf(groups, "bow"));
	}


	@Test
	void removingTakesAnEffectOutOfAGroup() {
		FeedbackGroups groups = resolve(
				"{'groups': {'test:items': ['test:bow', 'test:food']}}",
				"{'groups': {'-test:items': ['test:bow']}}"
		);

		assertEquals(List.of(FeedbackGroup.UNGROUPED), groupsOf(groups, "bow"));
		assertEquals(List.of(ITEMS), groupsOf(groups, "food"));
	}

	@Test
	void aHigherPackCanAddBackWhatALowerOneRemoved() {
		FeedbackGroups groups = resolve(
				"{'groups': {'test:items': ['test:bow']}}",
				"{'groups': {'-test:items': ['test:bow']}}",
				"{'groups': {'test:items': ['test:bow']}}"
		);

		assertEquals(List.of(ITEMS), groupsOf(groups, "bow"));
	}

	@Test
	void replacingDiscardsLowerPacks() {
		FeedbackGroups groups = resolve(
				"{'groups': {'test:world': ['test:explosion', 'test:lightning']}}",
				"{'groups': {'=test:world': ['test:lightning']}}"
		);

		assertEquals(List.of(FeedbackGroup.UNGROUPED), groupsOf(groups, "explosion"));
		assertEquals(List.of(WORLD), groupsOf(groups, "lightning"));
	}

	@Test
	void replacingWithNothingKeepsTheGroup() {
		FeedbackGroups groups = resolve(
				"{'groups': {'test:world': ['test:explosion']}}",
				"{'groups': {'=test:world': []}}"
		);

		assertTrue(groups.all().contains(WORLD));
		assertEquals(List.of(FeedbackGroup.UNGROUPED), groupsOf(groups, "explosion"));
	}


	@Test
	void removingFromAGroupNoPackDefinesDoesNotCreateIt() {
		FeedbackGroups groups = resolve("{'groups': {'-test:gui': ['test:anvil']}}");

		assertEquals(List.of(), groups.all());
	}



	// --- ordering ---

	private static final String THREE_GROUPS = "{'groups': {'test:world': [], 'test:player': [], 'test:gui': []}}";

	@Test
	void ranksCountTheGroupsStackedBelow() {
		FeedbackGroups groups = resolve(
				THREE_GROUPS,
				"{'ordering': {'test:player': {'above': ['test:world']}, 'test:gui': {'above': ['test:player']}}}"
		);

		assertEquals(0, groups.rank(WORLD));
		assertEquals(1, groups.rank(PLAYER));
		assertEquals(2, groups.rank(GUI));
	}

	@Test
	void unrelatedGroupsTie() {
		FeedbackGroups groups = resolve(THREE_GROUPS, "{'ordering': {'test:player': {'above': ['test:world']}}}");

		assertEquals(0, groups.rank(GUI));
		assertEquals(groups.rank(WORLD), groups.rank(GUI));
	}

	@Test
	void anEffectDrawsAtItsHighestGroup() {
		FeedbackGroups groups = resolve(
				"{'groups': {'test:world': ['test:bolt'], 'test:player': ['test:bolt']}}",
				"{'ordering': {'test:player': {'above': ['test:world']}}}"
		);

		assertEquals(1, groups.membership(id("bolt"), FeedbackGroup.UNGROUPED).rank());
	}

	@Test
	void removingMatchesEitherWayAnEdgeWasWritten() {
		FeedbackGroups groups = resolve(
				THREE_GROUPS,
				"{'ordering': {'test:player': {'above': ['test:world']}}}",
				"{'ordering': {'-test:world': {'below': ['test:player']}}}"
		);

		assertEquals(0, groups.rank(PLAYER));
	}

	@Test
	void replacingClearsEveryEdgeOfTheGroup() {
		FeedbackGroups groups = resolve(
				THREE_GROUPS,
				"{'ordering': {'test:player': {'above': ['test:world']}, 'test:gui': {'above': ['test:player']}}}",
				"{'ordering': {'=test:player': {'below': ['test:world']}}}"
		);

		assertEquals(1, groups.rank(WORLD));
		assertEquals(0, groups.rank(PLAYER));
		assertEquals(0, groups.rank(GUI));
	}

	@Test
	void aContradictionKeepsTheHigherPacksOrdering() {
		FeedbackGroups groups = resolve(
				THREE_GROUPS,
				"{'ordering': {'test:player': {'above': ['test:world']}}}",
				"{'ordering': {'test:world': {'above': ['test:player']}}}"
		);

		assertEquals(1, groups.rank(WORLD));
		assertEquals(0, groups.rank(PLAYER));
		assertEquals(1, this.warnings.size());
		assertTrue(this.warnings.getFirst().contains("pack1"), this.warnings.getFirst());
	}

	@Test
	void aLongerCycleIsBrokenToo() {
		FeedbackGroups groups = resolve(
				THREE_GROUPS,
				"{'ordering': {'test:player': {'above': ['test:world']}, 'test:gui': {'above': ['test:player']}}}",
				"{'ordering': {'test:world': {'above': ['test:gui']}}}"
		);

		// the highest pack's edge is kept, and one of the lower ones dropped
		assertEquals(1, this.warnings.size());
		assertTrue(groups.rank(WORLD) > groups.rank(GUI));
	}

	@Test
	void ungroupedEffectsCanBeOrdered() {
		FeedbackGroups groups = resolve(THREE_GROUPS, "{'ordering': {'controlify:ungrouped': {'above': ['test:world']}}}");

		assertEquals(1, groups.membership(id("x"), FeedbackGroup.UNGROUPED).rank());
		assertEquals(0, this.warnings.size());
	}

}
