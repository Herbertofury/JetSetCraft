package com.herberto.jetsetcraft.rush;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;

/** Explicit entry; the normal Boombox and vanilla interactions retain their existing meanings. */
public final class RivalRushCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("jetsetcraft").then(Commands.literal("rush")
                .executes(context -> {
                    RivalRushManager.handleAction(context.getSource().getPlayerOrException(), "menu", "", 0, null, 0); return 1;
                })
                .then(Commands.literal("leave").executes(context -> {
                    RivalRushManager.handleAction(context.getSource().getPlayerOrException(), "leave", "", 0, null, 0); return 1;
                }))
                .then(Commands.literal("album").executes(context -> {
                    RivalRushManager.handleAction(context.getSource().getPlayerOrException(), "album", "", 0, null, 0); return 1;
                }))
                .then(Commands.literal("start")
                        .then(Commands.argument("crew", StringArgumentType.word())
                                .then(Commands.argument("mob", StringArgumentType.word())
                                        .executes(context -> RivalRushManager.start(context.getSource().getPlayerOrException(),
                                                ResourceLocation.tryParse(StringArgumentType.getString(context, "crew")),
                                                ResourceLocation.tryParse(StringArgumentType.getString(context, "mob")), false) ? 1 : 0)
                                        .then(Commands.argument("endless", BoolArgumentType.bool())
                                                .executes(context -> RivalRushManager.start(context.getSource().getPlayerOrException(),
                                                        ResourceLocation.tryParse(StringArgumentType.getString(context, "crew")),
                                                        ResourceLocation.tryParse(StringArgumentType.getString(context, "mob")),
                                                        BoolArgumentType.getBool(context, "endless")) ? 1 : 0)))))));
    }
    private RivalRushCommands() {}
}
