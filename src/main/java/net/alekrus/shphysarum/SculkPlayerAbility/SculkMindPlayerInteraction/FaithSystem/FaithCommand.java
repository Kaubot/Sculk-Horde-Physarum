package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.FaithSystem;

import net.alekrus.shphysarum.shPhysarum;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class FaithCommand {


    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sculkfaith")
                .requires(source -> source.hasPermission(2)) 


                .then(Commands.literal("add")
                        .then(Commands.argument("amount", IntegerArgumentType.integer())

                                .executes(context -> addFaith(context, context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "amount")))


                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> addFaith(context, EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "amount")))
                                )
                        )
                )


                .then(Commands.literal("set")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0)) 
                                .executes(context -> setFaith(context, context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "amount")))
                        )
                )


                .then(Commands.literal("get")
                        .executes(context -> getFaith(context, context.getSource().getPlayerOrException()))
                )
        );
    }

    private static int addFaith(CommandContext<CommandSourceStack> context, ServerPlayer player, int amount) {

        FaithHandler.addFaith(player, amount);

        context.getSource().sendSuccess(() -> Component.literal("§aAdded " + amount + " Faith to " + player.getName().getString()), true);
        return 1;
    }

    private static int setFaith(CommandContext<CommandSourceStack> context, ServerPlayer player, int amount) {

        int current = FaithHandler.getFaith(player);
        int difference = amount - current;

        FaithHandler.addFaith(player, difference);

        context.getSource().sendSuccess(() -> Component.literal("§aSet Faith of " + player.getName().getString() + " to " + amount), true);
        return 1;
    }

    private static int getFaith(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        int current = FaithHandler.getFaith(player);
        context.getSource().sendSuccess(() -> Component.literal("§b" + player.getName().getString() + " has " + current + " Faith."), false);
        return 1;
    }
}