/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/// Which groups each effect belongs to, and the rank each group draws at, merged from every
/// pack's `groups.json`. See [FeedbackGroupManager] for the file format.
///
/// Resolving never throws: anything malformed or contradictory is reported and skipped.
public final class FeedbackGroups {
	public static final FeedbackGroups EMPTY = new FeedbackGroups(Map.of(), Set.of(), Map.of());

	/// One pack's `groups.json`.
	///
	/// @param packId used in messages
	public record PackFile(String packId, JsonElement json) {
	}

	/// Where problems found while resolving are reported.
	public interface Reporter {
		/// Something a pack author should fix.
		void warn(String message);

		/// Something that is probably intended, such as removing what is not there.
		void debug(String message);
	}

	/// The groups a playback belongs to, and the rank it draws at.
	///
	/// @param groups never empty
	/// @param rank   the highest rank of `groups`
	public record Membership(List<FeedbackGroup> groups, int rank) {
	}

	/// The groups packs put each effect in.
	private final Map<Identifier, Set<FeedbackGroup>> membership;
	/// Every group packs defined. Never contains [FeedbackGroup#UNGROUPED].
	private final List<FeedbackGroup> all;
	private final Set<FeedbackGroup> allSet;
	private final Map<FeedbackGroup, Integer> ranks;
	private final Map<Key, Membership> cache = new ConcurrentHashMap<>();

	private FeedbackGroups(
			Map<Identifier, Set<FeedbackGroup>> membership,
			Set<FeedbackGroup> all,
			Map<FeedbackGroup, Integer> ranks
	) {
		this.membership = membership;
		this.all = all.stream().sorted(Comparator.comparing(group -> group.id().toString())).toList();
		this.allSet = Set.copyOf(all);
		this.ranks = ranks;
	}

	/// Every group any pack defined, sorted by id. Each one has a strength slider.
	/// [FeedbackGroup#UNGROUPED] is not included, since it has none.
	public List<FeedbackGroup> all() {
		return this.all;
	}

	/// The rank a group draws at: 0 if no group is ordered below it, else one more than the
	/// highest rank below it.
	public int rank(FeedbackGroup group) {
		return this.ranks.getOrDefault(group, 0);
	}

	/// The groups of an effect: the ones packs list it in. They depend only on the effect, not
	/// on the event playing it. An effect in no group belongs to `fallback` if a pack defines
	/// that group, else to [FeedbackGroup#UNGROUPED].
	public Membership membership(Identifier effectId, FeedbackGroup fallback) {
		return this.cache.computeIfAbsent(new Key(effectId, fallback), this::computeMembership);
	}

	private Membership computeMembership(Key key) {
		Set<FeedbackGroup> listed = this.membership.getOrDefault(key.effectId, Set.of());

		List<FeedbackGroup> groups = new ArrayList<>();
		for (FeedbackGroup group : this.all) {
			if (listed.contains(group)) {
				groups.add(group);
			}
		}
		if (groups.isEmpty()) {
			groups.add(this.allSet.contains(key.fallback) ? key.fallback : FeedbackGroup.UNGROUPED);
		}

		int rank = 0;
		for (FeedbackGroup group : groups) {
			rank = Math.max(rank, this.rank(group));
		}
		return new Membership(List.copyOf(groups), rank);
	}

	private record Key(Identifier effectId, FeedbackGroup fallback) {
	}

	// ---- resolving ----

	private enum Op {
		/// `"group"`: adds
		ADD,
		/// `"=group"`: replaces what lower packs said
		REPLACE,
		/// `"-group"`: removes
		REMOVE
	}

	/// `lower` draws below `upper`.
	private record Edge(FeedbackGroup lower, FeedbackGroup upper) {
		@Override
		public @NonNull String toString() {
			return "'" + this.upper.id() + "' above '" + this.lower.id() + "'";
		}
	}

	/// The pack that last asserted an edge. Higher packs have higher indices.
	private record Origin(int packIndex, String packId) {
	}

	private record Entry<V>(Op op, FeedbackGroup group, V value) {
	}

	/// Merges pack files, lowest-priority pack first.
	public static FeedbackGroups resolve(List<PackFile> files, Reporter reporter) {
		Map<FeedbackGroup, Set<Identifier>> byGroup = new HashMap<>();
		Set<FeedbackGroup> defined = new LinkedHashSet<>();
		Set<FeedbackGroup> removedFrom = new LinkedHashSet<>();
		Map<Edge, Origin> edges = new LinkedHashMap<>();

		for (int packIndex = 0; packIndex < files.size(); packIndex++) {
			PackFile file = files.get(packIndex);
			String pack = file.packId();
			if (!(file.json() instanceof JsonObject root)) {
				reporter.warn("Feedback groups from pack '" + pack + "' must be a JSON object; ignoring the file");
				continue;
			}
			for (String key : root.keySet()) {
				if (!key.equals("groups") && !key.equals("ordering")) {
					reporter.warn("Unknown field '" + key + "' in feedback groups from pack '" + pack + "'");
				}
			}

			List<Entry<List<Identifier>>> groupEntries = parseEntries(root, "groups", pack, reporter, FeedbackGroups::parseEffects);
			groupEntries.removeIf(entry -> {
				if (entry.group().equals(FeedbackGroup.UNGROUPED)) {
					reporter.warn("Pack '" + pack + "' changes the group '" + entry.group().id()
							+ "', which cannot be changed: it holds every effect in no other group; ignoring it");
					return true;
				}
				if (entry.group().id().equals(FeedbackGroup.MASTER_STRENGTH)) {
					reporter.warn("Pack '" + pack + "' uses '" + entry.group().id()
							+ "' as a group, but it is the master strength, which applies to every effect; ignoring it");
					return true;
				}
				return false;
			});
			applyGroups(groupEntries, pack, byGroup, defined, removedFrom, reporter);

			List<Entry<List<Edge>>> orderEntries = parseEntries(root, "ordering", pack, reporter, FeedbackGroups::parseOrdering);
			applyOrdering(orderEntries, new Origin(packIndex, pack), edges, reporter);
		}

		for (FeedbackGroup group : removedFrom) {
			if (!defined.contains(group)) {
				reporter.debug("Effects are removed from feedback group '" + group.id() + "', which no pack defines");
			}
		}

		Map<Identifier, Set<FeedbackGroup>> membership = new HashMap<>();
		byGroup.forEach((group, effects) -> effects.forEach(effect ->
				membership.computeIfAbsent(effect, _ -> new HashSet<>()).add(group)));
		membership.replaceAll((_, groups) -> Set.copyOf(groups));

		Set<FeedbackGroup> orderable = new LinkedHashSet<>(defined);
		orderable.add(FeedbackGroup.UNGROUPED);
		Map<FeedbackGroup, Integer> ranks = rank(edges, orderable, reporter);
		return new FeedbackGroups(Map.copyOf(membership), defined, Map.copyOf(ranks));
	}

	private static void applyGroups(
			List<Entry<List<Identifier>>> entries,
			String pack,
			Map<FeedbackGroup, Set<Identifier>> byGroup,
			Set<FeedbackGroup> defined,
			Set<FeedbackGroup> removedFrom,
			Reporter reporter
	) {
		// within one file: replace, then remove, then add
		for (Entry<List<Identifier>> entry : entries) {
			if (entry.op() == Op.REPLACE) {
				byGroup.put(entry.group(), new HashSet<>(entry.value()));
				defined.add(entry.group());
			}
		}
		Map<FeedbackGroup, Set<Identifier>> removedHere = new HashMap<>();
		for (Entry<List<Identifier>> entry : entries) {
			if (entry.op() == Op.REMOVE) {
				Set<Identifier> effects = byGroup.computeIfAbsent(entry.group(), _ -> new HashSet<>());
				for (Identifier effect : entry.value()) {
					if (!effects.remove(effect)) {
						reporter.debug("Pack '" + pack + "' removes '" + effect + "' from feedback group '"
								+ entry.group().id() + "', which no lower pack put it in");
					}
				}
				removedHere.computeIfAbsent(entry.group(), _ -> new HashSet<>()).addAll(entry.value());
				removedFrom.add(entry.group());
			}
		}
		for (Entry<List<Identifier>> entry : entries) {
			if (entry.op() == Op.ADD) {
				Set<Identifier> effects = byGroup.computeIfAbsent(entry.group(), _ -> new HashSet<>());
				Set<Identifier> removed = removedHere.getOrDefault(entry.group(), Set.of());
				for (Identifier effect : entry.value()) {
					if (removed.contains(effect)) {
						reporter.warn("Pack '" + pack + "' both adds '" + effect + "' to feedback group '"
								+ entry.group().id() + "' and removes it; it is added");
					}
					effects.add(effect);
				}
				defined.add(entry.group());
			}
		}
	}

	private static void applyOrdering(
			List<Entry<List<Edge>>> entries,
			Origin origin,
			Map<Edge, Origin> edges,
			Reporter reporter
	) {
		// clear every replaced group first, so two replacements in one file do not undo each other
		for (Entry<List<Edge>> entry : entries) {
			if (entry.op() == Op.REPLACE) {
				FeedbackGroup group = entry.group();
				edges.keySet().removeIf(edge -> edge.lower().equals(group) || edge.upper().equals(group));
			}
		}
		for (Entry<List<Edge>> entry : entries) {
			if (entry.op() == Op.REPLACE) {
				entry.value().forEach(edge -> edges.put(edge, origin));
			}
		}
		for (Entry<List<Edge>> entry : entries) {
			if (entry.op() == Op.REMOVE) {
				for (Edge edge : entry.value()) {
					if (edges.remove(edge) == null) {
						reporter.debug("Pack '" + origin.packId() + "' removes the feedback ordering " + edge
								+ ", which no lower pack defines");
					}
				}
			}
		}
		for (Entry<List<Edge>> entry : entries) {
			if (entry.op() == Op.ADD) {
				// re-asserting an edge makes this pack its origin, so it wins cycles as this pack
				entry.value().forEach(edge -> {
					edges.remove(edge);
					edges.put(edge, origin);
				});
			}
		}
	}

	/// Drops edges to undefined groups and edges that would close a cycle, then ranks every
	/// group by how many groups are stacked below it.
	private static Map<FeedbackGroup, Integer> rank(Map<Edge, Origin> edges, Set<FeedbackGroup> defined, Reporter reporter) {
		List<Map.Entry<Edge, Origin>> candidates = new ArrayList<>();
		for (Map.Entry<Edge, Origin> entry : edges.entrySet()) {
			Edge edge = entry.getKey();
			FeedbackGroup missing = !defined.contains(edge.lower()) ? edge.lower()
					: !defined.contains(edge.upper()) ? edge.upper()
					: null;
			if (missing != null) {
				reporter.warn("Ignoring the feedback ordering " + edge + " from pack '" + entry.getValue().packId()
						+ "': no pack defines the group '" + missing.id() + "'");
				continue;
			}
			candidates.add(entry);
		}

		// highest pack first, so when edges contradict each other the higher pack's are kept
		candidates.sort(Comparator
				.comparing((Map.Entry<Edge, Origin> entry) -> entry.getValue().packIndex()).reversed()
				.thenComparing(entry -> entry.getKey().lower().id().toString())
				.thenComparing(entry -> entry.getKey().upper().id().toString()));

		Map<FeedbackGroup, Map<FeedbackGroup, Origin>> up = new HashMap<>();
		for (Map.Entry<Edge, Origin> entry : candidates) {
			Edge edge = entry.getKey();
			List<Edge> path = path(up, edge.upper(), edge.lower());
			if (path != null) {
				Set<String> packs = new TreeSet<>();
				for (Edge step : path) {
					packs.add("'" + up.get(step.lower()).get(step.upper()).packId() + "'");
				}
				reporter.warn("Ignoring the feedback ordering " + edge + " from pack '" + entry.getValue().packId()
						+ "': it contradicts " + path + " from " + String.join(", ", packs));
				continue;
			}
			up.computeIfAbsent(edge.lower(), _ -> new LinkedHashMap<>()).put(edge.upper(), entry.getValue());
		}

		Map<FeedbackGroup, List<FeedbackGroup>> down = new HashMap<>();
		up.forEach((lower, uppers) -> uppers.keySet().forEach(upper ->
				down.computeIfAbsent(upper, _ -> new ArrayList<>()).add(lower)));

		Map<FeedbackGroup, Integer> ranks = new HashMap<>();
		for (FeedbackGroup group : defined) {
			depth(group, down, ranks);
		}
		return ranks;
	}

	/// The edges of a path going up from `from` to `to`, or null if there is none.
	private static @Nullable List<Edge> path(Map<FeedbackGroup, Map<FeedbackGroup, Origin>> up, FeedbackGroup from, FeedbackGroup to) {
		Map<FeedbackGroup, FeedbackGroup> cameFrom = new HashMap<>();
		Deque<FeedbackGroup> queue = new ArrayDeque<>();
		queue.add(from);
		cameFrom.put(from, from);
		while (!queue.isEmpty()) {
			FeedbackGroup current = queue.poll();
			if (current.equals(to)) {
				List<Edge> path = new ArrayList<>();
				for (FeedbackGroup node = to; !node.equals(from); node = cameFrom.get(node)) {
					path.addFirst(new Edge(cameFrom.get(node), node));
				}
				return path;
			}
			for (FeedbackGroup next : up.getOrDefault(current, Map.of()).keySet()) {
				if (cameFrom.putIfAbsent(next, current) == null) {
					queue.add(next);
				}
			}
		}
		return null;
	}

	/// Cannot loop: cycles were removed before ranking.
	private static int depth(FeedbackGroup group, Map<FeedbackGroup, List<FeedbackGroup>> down, Map<FeedbackGroup, Integer> ranks) {
		Integer known = ranks.get(group);
		if (known != null) {
			return known;
		}
		int rank = 0;
		for (FeedbackGroup lower : down.getOrDefault(group, List.of())) {
			rank = Math.max(rank, depth(lower, down, ranks) + 1);
		}
		ranks.put(group, rank);
		return rank;
	}

	// ---- parsing ----

	@FunctionalInterface
	private interface ValueParser<V> {
		@Nullable V parse(FeedbackGroup group, JsonElement json, String where, Reporter reporter);
	}

	/// @return a mutable list
	private static <V> List<Entry<V>> parseEntries(
			JsonObject root,
			String field,
			String pack,
			Reporter reporter,
			ValueParser<V> parser
	) {
		JsonElement element = root.get(field);
		if (element == null) {
			return new ArrayList<>();
		}
		if (!(element instanceof JsonObject object)) {
			reporter.warn("'" + field + "' in feedback groups from pack '" + pack + "' must be an object; ignoring it");
			return new ArrayList<>();
		}

		List<Entry<V>> entries = new ArrayList<>();
		for (Map.Entry<String, JsonElement> member : object.entrySet()) {
			String key = member.getKey();
			String where = "'" + field + "." + key + "' in feedback groups from pack '" + pack + "'";
			Op op = key.startsWith("=") ? Op.REPLACE : key.startsWith("-") ? Op.REMOVE : Op.ADD;
			Identifier id = Identifier.tryParse(op == Op.ADD ? key : key.substring(1));
			if (id == null) {
				reporter.warn(where + ": '" + key + "' is not a valid group id; ignoring it");
				continue;
			}
			FeedbackGroup group = new FeedbackGroup(id);
			V value = parser.parse(group, member.getValue(), where, reporter);
			if (value != null) {
				entries.add(new Entry<>(op, group, value));
			}
		}
		return entries;
	}

	private static @Nullable List<Identifier> parseEffects(FeedbackGroup group, JsonElement json, String where, Reporter reporter) {
		if (!(json instanceof JsonArray array)) {
			reporter.warn(where + " must be a list of effect ids; ignoring it");
			return null;
		}
		return parseIds(array, where, reporter);
	}

	private static @Nullable List<Edge> parseOrdering(FeedbackGroup group, JsonElement json, String where, Reporter reporter) {
		if (!(json instanceof JsonObject object)) {
			reporter.warn(where + " must be an object with 'above' and/or 'below'; ignoring it");
			return null;
		}
		List<Edge> edges = new ArrayList<>();
		for (Map.Entry<String, JsonElement> member : object.entrySet()) {
			boolean above = member.getKey().equals("above");
			if (!above && !member.getKey().equals("below")) {
				reporter.warn(where + ": unknown field '" + member.getKey() + "'");
				continue;
			}
			if (!(member.getValue() instanceof JsonArray array)) {
				reporter.warn(where + ": '" + member.getKey() + "' must be a list of group ids; ignoring it");
				continue;
			}
			for (Identifier other : parseIds(array, where, reporter)) {
				FeedbackGroup otherGroup = new FeedbackGroup(other);
				if (otherGroup.equals(group)) {
					reporter.warn(where + ": a group cannot be ordered against itself; ignoring it");
					continue;
				}
				edges.add(above ? new Edge(otherGroup, group) : new Edge(group, otherGroup));
			}
		}
		return edges;
	}

	private static List<Identifier> parseIds(JsonArray array, String where, Reporter reporter) {
		List<Identifier> ids = new ArrayList<>(array.size());
		for (JsonElement element : array) {
			Identifier id = element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
					? Identifier.tryParse(element.getAsString())
					: null;
			if (id == null) {
				reporter.warn(where + ": '" + element + "' is not a valid id; ignoring it");
				continue;
			}
			ids.add(id);
		}
		return ids;
	}
}
