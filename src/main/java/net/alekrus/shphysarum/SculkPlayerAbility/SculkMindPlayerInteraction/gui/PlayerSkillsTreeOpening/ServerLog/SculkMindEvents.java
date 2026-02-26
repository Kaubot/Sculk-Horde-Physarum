package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.SculkEvoCommand;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;

public class SculkMindEvents {

    @Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(ISculkMind.class);
        }
    }

    @Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            SculkEvoCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                if (!event.getObject().getCapability(SculkMindProvider.SCULK_MIND).isPresent()) {
                    event.addCapability(ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "sculk_player_data"), new SculkMindProvider());
                }
            }
        }

        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                event.getOriginal().reviveCaps();
                event.getOriginal().getCapability(SculkMindProvider.SCULK_MIND).ifPresent(oldStore -> {
                    event.getEntity().getCapability(SculkMindProvider.SCULK_MIND).ifPresent(newStore -> {
                        newStore.copyFrom(oldStore);
                    });
                });
                event.getOriginal().invalidateCaps();
            }
        }

        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer sp) syncData(sp);
        }

        @SubscribeEvent
        public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer sp) syncData(sp);
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer sp) syncData(sp);
        }

        private static void syncData(ServerPlayer player) {
            player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
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
            });
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {

                    if (event.getEntity() instanceof Zombie) {
                        if (!cap.hasSkill("health")) {
                            cap.incrementZombieKills();
                            if (cap.getZombieKills() >= 5) {
                                cap.unlockSkill("health");
                                player.sendSystemMessage(Component.literal("§2[Gravemind] Biomass sufficient. Mutation acquired."));
                            }
                        }
                    }

                    
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
                });
            }
        }
    }
}
