package dev.isxander.controlify.platform.server.events;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@FunctionalInterface
public interface CommandRegistrationCallbackEvent {
    void onRegister(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection env);
}
