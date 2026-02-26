package net.alekrus.shphysarum.PointOfNoReturn;

import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AppleEatHandler {

    
    @SubscribeEvent
    public static void onPlayerSpawn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {

            
            if (!player.getPersistentData().getBoolean("sh_ate_sculk_apple")) {

                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                    
                    if (cap.hasSkill("root")) {
                        cap.getUnlockedSkills().remove("root"); 
                    }

                    
                    PacketHandler.CHANNEL.sendTo(
                            new SkillSyncPacket(
                                    cap.getUnlockedSkills(), cap.getEvoPoints(), cap.getFaith(),
                                    cap.getActiveTaskNBT(), cap.areTentaclesActive(),
                                    cap.getUserFollowerLimit(), cap.getAllowedFollowerTypes(),
                                    cap.getKnownAnchors(), cap.getActiveAbilitiesSet()
                            ),
                            player.connection.connection, NetworkDirection.PLAY_TO_CLIENT
                    );
                });

                
                player.getPersistentData().putBoolean("sh_isInfected", false);
                net.alekrus.shphysarum.AnimationAll.VisualSyncHelper.syncToTracking(player);
            }
        }
    }

    
    @SubscribeEvent
    public static void onEatApple(LivingEntityUseItemEvent.Finish event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {

            if (event.getItem().is(ModItems.SCULK_APPLE.get())) {

                
                player.getPersistentData().putBoolean("sh_ate_sculk_apple", true);

                
                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                    if (!cap.hasSkill("root")) {
                        cap.unlockSkill("root");

                        PacketHandler.CHANNEL.sendTo(
                                new SkillSyncPacket(
                                        cap.getUnlockedSkills(), cap.getEvoPoints(), cap.getFaith(),
                                        cap.getActiveTaskNBT(), cap.areTentaclesActive(),
                                        cap.getUserFollowerLimit(), cap.getAllowedFollowerTypes(),
                                        cap.getKnownAnchors(), cap.getActiveAbilitiesSet()
                                ),
                                player.connection.connection, NetworkDirection.PLAY_TO_CLIENT
                        );
                    }
                });

                
                player.getPersistentData().putBoolean("sh_isInfected", true);
                net.alekrus.shphysarum.AnimationAll.VisualSyncHelper.syncToTracking(player);
            }
        }
    }
}
