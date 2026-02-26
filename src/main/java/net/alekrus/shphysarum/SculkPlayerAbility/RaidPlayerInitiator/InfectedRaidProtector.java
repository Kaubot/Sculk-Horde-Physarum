package net.alekrus.shphysarum.SculkPlayerAbility.RaidPlayerInitiator;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.core.ModSavedData;
import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfectedRaidProtector {

    private static int tickCounter = 0;

    public static final ConcurrentHashMap<BlockPos, Long> MANUAL_RAID_ZONES = new ConcurrentHashMap<>();

    private static class CleanupTask {
        ServerLevel level;
        BlockPos pos;
        int ticksLeft;

        CleanupTask(ServerLevel level, BlockPos pos, int delayTicks) {
            this.level = level;
            this.pos = pos;
            this.ticksLeft = delayTicks;
        }
    }

    private static final List<CleanupTask> PENDING_CLEANUPS = new ArrayList<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;

        long currentTime = System.currentTimeMillis();

        MANUAL_RAID_ZONES.entrySet().removeIf(entry -> currentTime > entry.getValue());

        if (!PENDING_CLEANUPS.isEmpty()) {
            Iterator<CleanupTask> iterator = PENDING_CLEANUPS.iterator();
            while (iterator.hasNext()) {
                CleanupTask task = iterator.next();
                task.ticksLeft--;

                if (task.ticksLeft <= 0) {
                    ModSavedData sculkData = ModSavedData.getSaveData();
                    if (sculkData != null) {
                        sculkData.getNoRaidZoneEntries().removeIf(entry -> entry.isBlockPosInRadius(task.level, task.pos));
                    }
                    iterator.remove();
                }
            }
        }

        if (tickCounter % 20 == 0) {
            if (event.getServer() == null) return;

            for (ServerLevel level : event.getServer().getAllLevels()) {
                List<ServerPlayer> infectedPlayers = new ArrayList<>();

                for (ServerPlayer player : level.players()) {
                    boolean isInfected = player.getCapability(SculkMindProvider.SCULK_MIND)
                            .map(cap -> cap.hasSkill("root"))
                            .orElse(false);
                    if (isInfected) {
                        infectedPlayers.add(player);
                    }
                }

                if (infectedPlayers.isEmpty()) continue;

                for (ServerPlayer player : infectedPlayers) {
                    List<SculkEndermanEntity> scouts = level.getEntitiesOfClass(
                            SculkEndermanEntity.class,
                            player.getBoundingBox().inflate(200),
                            entity -> entity.isAlive()
                                    && entity.hasEffect(MobEffects.GLOWING)
                                    && !entity.isParticipatingInRaid()
                    );

                    for (SculkEndermanEntity scout : scouts) {
                        BlockPos raidLocation = scout.blockPosition();

                        boolean isManualRaid = false;
                        for (BlockPos manualPos : MANUAL_RAID_ZONES.keySet()) {
                            if (manualPos.closerToCenterThan(raidLocation.getCenter(), 150)) {
                                isManualRaid = true;
                                break;
                            }
                        }

                        if (isManualRaid) {
                            continue;
                        }

                        
                        scout.discard();

                        
                        GravemindMessagePacket.sendToPlayer(player, "Scout intercepted. Redirecting the Horde from your territory...");

                        PENDING_CLEANUPS.add(new CleanupTask(level, raidLocation, 60));
                    }
                }
            }
        }
    }
}
