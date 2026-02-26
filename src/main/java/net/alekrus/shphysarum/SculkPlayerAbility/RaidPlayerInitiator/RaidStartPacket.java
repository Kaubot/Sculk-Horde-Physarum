package net.alekrus.shphysarum.SculkPlayerAbility.RaidPlayerInitiator;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.events.RaidEvent.RaidEvent;
import com.github.sculkhorde.systems.gravemind_system.Gravemind.evolution_states;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import com.github.sculkhorde.systems.event_system.Event;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class RaidStartPacket {

    private static final int XP_COST_LEVELS = 70;

    public RaidStartPacket() {}
    public RaidStartPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    public static RaidStartPacket decode(FriendlyByteBuf buf) { return new RaidStartPacket(buf); }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (!InfectionHandler.isInfected(player)) {
                return;
            }

            if (player.experienceLevel < XP_COST_LEVELS && !player.isCreative()) {
                player.sendSystemMessage(Component.literal("§3Insufficient experience. Required: " + XP_COST_LEVELS));
                return;
            }

            if (!(Boolean) ModConfig.SERVER.sculk_raid_enabled.get()) {
                player.sendSystemMessage(Component.literal("§cRaids are disabled in server config."));
                return;
            }

            if (ModSavedData.getSaveData().isHordeDefeated()) {
                player.sendSystemMessage(Component.literal("§cThe Gravemind is dead. The Horde can no longer raid."));
                return;
            }

            if (!ModSavedData.getSaveData().isRaidCooldownOver()) {
                player.sendSystemMessage(Component.literal("§cThe Horde is recovering their forces. Raid is on cooldown."));
                return;
            }

            if (SculkHorde.gravemind.getEvolutionState() == evolution_states.Undeveloped) {
                player.sendSystemMessage(Component.literal("§cThe Gravemind is too weak to send forces. Further evolution required."));
                return;
            }

            ServerLevel serverLevel = (ServerLevel) player.level();
            ResourceKey<Level> dimensionKey = player.level().dimension();
            BlockPos centerPos = player.blockPosition();
            long time = player.level().getGameTime();

            RaidEvent newRaid = new RaidEvent(dimensionKey);

            ModSavedData.getSaveData().addAreaOfInterestToMemory(serverLevel, centerPos);

            ModSavedData.AreaOfInterestEntry manualEntry = new ModSavedData.AreaOfInterestEntry(
                    dimensionKey,
                    centerPos,
                    time
            );
            newRaid.setAreaOfInterestEntry(manualEntry);

            newRaid.setRaidLocation(centerPos);
            newRaid.setRaidCenter(centerPos);
            newRaid.setSpawnLocation(centerPos);
            newRaid.setCurrentRaidRadius(200);

            for (int i = 0; i < 20; i++) {
                newRaid.getHighPriorityTargets().add(centerPos);
            }

            try {
                Field eventLocField = Event.class.getDeclaredField("eventLocation");
                eventLocField.setAccessible(true);
                eventLocField.set(newRaid, centerPos);

            } catch (Exception e) {
                try {
                    Method setLocMethod = Event.class.getDeclaredMethod("setEventLocation", BlockPos.class);
                    setLocMethod.setAccessible(true);
                    setLocMethod.invoke(newRaid, centerPos);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
            }

            InfectedRaidProtector.MANUAL_RAID_ZONES.put(centerPos, System.currentTimeMillis() + 900000L);


            newRaid.setState(RaidEvent.State.SCOUTING);

            if (!player.isCreative()) {
                player.giveExperienceLevels(-XP_COST_LEVELS);
            }

            SculkHorde.eventSystem.addEvent(newRaid);

            
            GravemindMessagePacket.sendToPlayer(player, "Acknowledged the target. Sending Scout unit.");

        });
        context.setPacketHandled(true);
    }
}
