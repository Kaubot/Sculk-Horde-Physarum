package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

public class SculkEvoCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sculkevo")
                .requires(source -> source.hasPermission(2)) 
                .then(Commands.literal("add")
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                    player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                                        cap.addEvoPoints(amount);

                                        
                                        PacketHandler.CHANNEL.sendTo(
                                                new SkillSyncPacket(
                                                        cap.getUnlockedSkills(),
                                                        cap.getEvoPoints(),
                                                        cap.getFaith(),
                                                        cap.getActiveTaskNBT(),
                                                        cap.areTentaclesActive(),
                                                        cap.getUserFollowerLimit(),     
                                                        cap.getAllowedFollowerTypes(),
                                                        cap.getKnownAnchors(),
                                                        cap.getActiveAbilitiesSet()
                                                ),
                                                player.connection.connection,
                                                NetworkDirection.PLAY_TO_CLIENT
                                        );

                                        context.getSource().sendSuccess(() -> Component.literal("Added " + amount + " Evo Points."), true);
                                    });
                                    return 1;
                                })
                        )
                )
        );
    }
}
