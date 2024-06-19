package dev.isxander.controlify.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.isxander.controlify.platform.network.SidedNetworkApi;
import dev.isxander.controlify.rumble.RumbleSource;
import dev.isxander.controlify.rumble.RumbleState;
import dev.isxander.controlify.server.packets.EntityVibrationPacket;
import dev.isxander.controlify.server.packets.OriginVibrationPacket;
import dev.isxander.controlify.server.packets.VibrationPacket;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class VibrateCommand {
    private static final SuggestionProvider<CommandSourceStack> SOURCES_SUGGESTION = SuggestionProviders.register(
            CUtil.rl("vibration_sources"),
            (context, builder) -> SharedSuggestionProvider.suggestResource(
                    RumbleSource.values().stream()
                            .map(RumbleSource::id)
                            .toList(),
                    builder
            )
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("vibratecontroller")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.argument("receivers", EntityArgument.players())
                                        .then(
                                                Commands.argument("low_freq_vibration", FloatArgumentType.floatArg(0, 1))
                                                        .then(
                                                                Commands.argument("high_freq_vibration", FloatArgumentType.floatArg(0, 1))
                                                                        .then(
                                                                                Commands.argument("duration", IntegerArgumentType.integer(1))
                                                                                        .then(
                                                                                                Commands.literal("static")
                                                                                                        .executes(context -> vibrateStatic(
                                                                                                                context.getSource(),
                                                                                                                EntityArgument.getPlayers(context, "receivers"),
                                                                                                                FloatArgumentType.getFloat(context, "low_freq_vibration"),
                                                                                                                FloatArgumentType.getFloat(context, "high_freq_vibration"),
                                                                                                                IntegerArgumentType.getInteger(context, "duration"),
                                                                                                                RumbleSource.MASTER
                                                                                                        ))
                                                                                        )
                                                                                        .then(
                                                                                                Commands.literal("positioned")
                                                                                                        .then(
                                                                                                                Commands.argument("range", FloatArgumentType.floatArg(0))
                                                                                                                        .then(
                                                                                                                                Commands.argument("position", Vec3Argument.vec3(true))
                                                                                                                                        .executes(context -> vibrateFromOrigin(
                                                                                                                                                context.getSource(),
                                                                                                                                                EntityArgument.getPlayers(context, "receivers"),
                                                                                                                                                Vec3Argument.getVec3(context, "position"),
                                                                                                                                                FloatArgumentType.getFloat(context, "range"),
                                                                                                                                                IntegerArgumentType.getInteger(context, "duration"),
                                                                                                                                                FloatArgumentType.getFloat(context, "low_freq_vibration"),
                                                                                                                                                FloatArgumentType.getFloat(context, "high_freq_vibration"),
                                                                                                                                                RumbleSource.MASTER
                                                                                                                                        ))
                                                                                                                        )
                                                                                                                        .then(
                                                                                                                                Commands.argument("entity", EntityArgument.entity())
                                                                                                                                        .executes(context -> vibrateFromEntity(
                                                                                                                                                context.getSource(),
                                                                                                                                                EntityArgument.getPlayers(context, "receivers"),
                                                                                                                                                EntityArgument.getEntity(context, "entity"),
                                                                                                                                                FloatArgumentType.getFloat(context, "range"),
                                                                                                                                                IntegerArgumentType.getInteger(context, "duration"),
                                                                                                                                                FloatArgumentType.getFloat(context, "low_freq_vibration"),
                                                                                                                                                FloatArgumentType.getFloat(context, "high_freq_vibration"),
                                                                                                                                                RumbleSource.MASTER
                                                                                                                                        ))
                                                                                                                        )
                                                                                                        )

                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }

    private static int vibrateStatic(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            float lowFreqMagnitude, float highFreqMagnitude,
            int durationTicks,
            RumbleSource rumbleSource
    ) {
        RumbleState[] frames = new RumbleState[durationTicks];
        Arrays.fill(frames, new RumbleState(lowFreqMagnitude, highFreqMagnitude));

        VibrationPacket packet = new VibrationPacket(rumbleSource, frames);
        for (var player : targets) {
            SidedNetworkApi.S2C().sendPacket(player, VibrationPacket.CHANNEL, packet);
        }

        source.sendSuccess(
                () -> targets.size() == 1
                        ? Component.translatable("controlify.command.vibratecontroller.static.single")
                        : Component.translatable("controlify.command.vibratecontroller.static.multiple", targets.size()),
                true
        );

        return targets.size();
    }

    private static int vibrateFromOrigin(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            Vec3 origin,
            float effectRange, int duration,
            float lowFreqMagnitude, float highFreqMagnitude,
            RumbleSource rumbleSource
    ) {
        RumbleState state = new RumbleState(lowFreqMagnitude, highFreqMagnitude);

        OriginVibrationPacket packet = new OriginVibrationPacket(origin.toVector3f(), effectRange, duration, state, rumbleSource);
        for (var player : targets) {
            SidedNetworkApi.S2C().sendPacket(player, OriginVibrationPacket.CHANNEL, packet);
        }

        source.sendSuccess(
                () -> targets.size() == 1
                        ? Component.translatable("controlify.command.vibratecontroller.pos.single", formatDouble(origin.x), formatDouble(origin.y), formatDouble(origin.z))
                        : Component.translatable("controlify.command.vibratecontroller.pos.multiple", targets.size(), formatDouble(origin.x), formatDouble(origin.y), formatDouble(origin.z)),
                true
        );

        return targets.size();
    }

    private static int vibrateFromEntity(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            Entity origin,
            float effectRange, int duration,
            float lowFreqMagnitude, float highFreqMagnitude,
            RumbleSource rumbleSource
    ) {
        RumbleState state = new RumbleState(lowFreqMagnitude, highFreqMagnitude);

        EntityVibrationPacket packet = new EntityVibrationPacket(origin.getId(), effectRange, duration, state, rumbleSource);
        for (var player : targets) {
            SidedNetworkApi.S2C().sendPacket(player, EntityVibrationPacket.CHANNEL, packet);
        }

        source.sendSuccess(
                () -> targets.size() == 1
                        ? Component.translatable("controlify.command.vibratecontroller.entity.single", origin.getDisplayName())
                        : Component.translatable("controlify.command.vibratecontroller.entity.multiple", targets.size(), origin.getDisplayName()),
                true
        );

        return targets.size();
    }

    private static String formatDouble(double d) {
        return String.format(Locale.ROOT, "%f", d);
    }
}
