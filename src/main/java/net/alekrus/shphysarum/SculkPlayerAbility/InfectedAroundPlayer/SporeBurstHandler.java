package net.alekrus.shphysarum.SculkPlayerAbility.InfectedAroundPlayer;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.cursor_system.VirtualSurfaceInfestorCursor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.Random;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SporeBurstHandler {

    private static final String TAG_ACTIVE = "sculk_spore_burst_active";
    private static final String TAG_TIMER = "sculk_spore_burst_timer";

    public static void toggleAbility(ServerPlayer player) {
        boolean isActive = player.getPersistentData().getBoolean(TAG_ACTIVE);

        if (isActive) {
            deactivate(player);
        } else {
            activate(player);
        }
    }

    private static void activate(ServerPlayer player) {
        player.getPersistentData().putBoolean(TAG_ACTIVE, true);
        player.getPersistentData().putInt(TAG_TIMER, 0);

        player.hurt(player.damageSources().magic(), 4.0F);

        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "burrowed_burst"));
        if (sound != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        spawnExplosionParticles(player);


        
        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            cap.addActiveAbility("burst");
            syncSculkMind(player, cap);
        });
    }

    private static void deactivate(ServerPlayer player) {
        player.getPersistentData().putBoolean(TAG_ACTIVE, false);


        
        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            cap.removeActiveAbility("burst");
            syncSculkMind(player, cap);
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        if (!player.getPersistentData().getBoolean(TAG_ACTIVE)) return;

        if (!InfectionHandler.isInfected(player)) {
            deactivate(player); 
            return;
        }

        int timer = player.getPersistentData().getInt(TAG_TIMER);
        timer++;

        if (timer >= 1) {
            timer = 0;
            if (player.totalExperience > 0) {
                player.giveExperiencePoints(-1);
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§3Not enough XP."), true);
                deactivate(player);
                return;
            }
        }

        player.getPersistentData().putInt(TAG_TIMER, timer);

        if (player.tickCount % 20 == 0) {
            spawnInfectionCursor(player);
            spawnRunningParticles(player);
        }
    }

    
    private static void syncSculkMind(ServerPlayer player, net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.ISculkMind cap) {
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

    
    private static void spawnInfectionCursor(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Optional<VirtualSurfaceInfestorCursor> possibleCursor = CursorSystem.createSurfaceInfestorVirtualCursor(level, player.blockPosition());
        if (possibleCursor.isPresent()) {
            VirtualSurfaceInfestorCursor cursor = possibleCursor.get();
            cursor.setMaxTransformations(20);
            cursor.setMaxRange(10);
            cursor.setTickIntervalTicks(5L);
            cursor.setSearchIterationsPerTick(5);
        }
    }

    private static void spawnExplosionParticles(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Random rng = new Random();
        for (int i = 0; i < 30; i++) {
            double x = player.getX() + (rng.nextDouble() - 0.5) * 1.5;
            double y = player.getY() + rng.nextDouble() * 2.0;
            double z = player.getZ() + (rng.nextDouble() - 0.5) * 1.5;
            level.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 1, 0, 0, 0, 0.1);
        }
    }

    private static void spawnRunningParticles(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, player.getX(), player.getY() + 1, player.getZ(), 2, 0.3, 0.5, 0.3, 0.05);
    }
}
