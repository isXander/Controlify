/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/// A category of feedback that the user can set the strength of, such as `controlify:world`.
/// An effect can belong to several groups. Which effects belong to which group, and how groups
/// are ordered against each other, is data: see [FeedbackGroupManager].
public record FeedbackGroup(Identifier id) {
	public static final Codec<FeedbackGroup> CODEC = Identifier.CODEC
			.xmap(FeedbackGroup::new, FeedbackGroup::id);

	/// The group of every effect that is in no other group. Nothing can be added to it, and it
	/// has no strength of its own, but it can be ordered against other groups.
	public static final FeedbackGroup UNGROUPED = new FeedbackGroup(CUtil.rl("ungrouped"));

	/// Not a group: the id of the master strength, which every effect is multiplied by on top
	/// of its groups'. Packs cannot use it as a group.
	public static final Identifier MASTER_STRENGTH = CUtil.rl("master");

	public FeedbackGroup {
		Objects.requireNonNull(id);
	}
}
