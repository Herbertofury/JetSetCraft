package com.herberto.jetsetcraft.command;

import com.herberto.jetsetcraft.crew.CrewHouseManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** Player-accessible overview; founding and relocation still require a real Charter anchor interaction. */
public final class CrewHouseCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jetsetcraft").then(Commands.literal("crew").executes(context -> {
            CrewHouseManager.open(context.getSource().getPlayerOrException());
            return 1;
        })));
    }

    private CrewHouseCommands() {}
}
