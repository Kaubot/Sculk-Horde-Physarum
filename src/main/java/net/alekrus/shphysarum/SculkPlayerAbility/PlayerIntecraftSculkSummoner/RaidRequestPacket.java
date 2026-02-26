package net.alekrus.shphysarum.SculkPlayerAbility.PlayerIntecraftSculkSummoner;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.common.block.SculkSummonerBlock;
import com.github.sculkhorde.common.blockentity.SculkSummonerBlockEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.ReinforcementRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RaidRequestPacket {


    private static final int XP_COST_PER_SUMMONER = 20;

    public RaidRequestPacket() {}
    public RaidRequestPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    public static RaidRequestPacket decode(FriendlyByteBuf buf) { return new RaidRequestPacket(buf); }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!InfectionHandler.isInfected(player)) return;

            activateNearbySummoners(player);
        });
        context.setPacketHandled(true);
    }

    private void activateNearbySummoners(ServerPlayer player) {
        ServerLevel serverLevel = player.serverLevel();
        BlockPos playerPos = player.blockPosition();

        int radius = 25;
        List<SculkSummonerBlockEntity> foundSummoners = new ArrayList<>();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();


        for (int x = playerPos.getX() - radius; x <= playerPos.getX() + radius; x++) {
            for (int z = playerPos.getZ() - radius; z <= playerPos.getZ() + radius; z++) {
                for (int y = playerPos.getY() - 10; y <= playerPos.getY() + 10; y++) {
                    mutablePos.set(x, y, z);
                    BlockEntity be = serverLevel.getBlockEntity(mutablePos);
                    if (be instanceof SculkSummonerBlockEntity summoner) {
                        foundSummoners.add(summoner);
                    }
                }
            }
        }

        if (foundSummoners.isEmpty()) {
            player.sendSystemMessage(Component.literal("§3No active Summoners nearby."));
            return;
        }

        int activatedCount = 0;


        for (SculkSummonerBlockEntity summoner : foundSummoners) {
            BlockPos pos = summoner.getBlockPos();
            BlockState state = summoner.getBlockState();


            if (state.hasProperty(SculkSummonerBlock.VIBRATION_COOLDOWN) && state.getValue(SculkSummonerBlock.VIBRATION_COOLDOWN)) {
                continue;
            }


            if (!player.isCreative()) {
                if (player.totalExperience < XP_COST_PER_SUMMONER) {
                    if (activatedCount == 0) {

                        player.sendSystemMessage(Component.literal("§cNot enough experience. " + XP_COST_PER_SUMMONER + " XP points required per summoner."));
                    } else {

                        player.sendSystemMessage(Component.literal("§cexperience depleted. Could not activate all nearby summoners."));
                    }
                    break;
                }


                player.giveExperiencePoints(-XP_COST_PER_SUMMONER);
            }



            try {
                Field lastTimeField = SculkSummonerBlockEntity.class.getDeclaredField("lastGameTimeOfVibrationRecieve");
                lastTimeField.setAccessible(true);
                lastTimeField.setLong(summoner, serverLevel.getGameTime());
            } catch (Exception e) {
                e.printStackTrace();
            }

            serverLevel.setBlock(pos, state.setValue(SculkSummonerBlock.VIBRATION_COOLDOWN, true), 3);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.15, pos.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0);
            serverLevel.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 1.6F);
            serverLevel.levelEvent(3007, pos, 0);
            serverLevel.gameEvent(GameEvent.SHRIEK, pos, GameEvent.Context.of(player));

            boolean isWaterLogged = state.getValue(BlockStateProperties.WATERLOGGED);

            ArrayList<BlockPos> spawnPoints = summoner.getSpawnPositionsInCube(serverLevel, pos, 5, 4, isWaterLogged);

            if (spawnPoints.isEmpty()) {
                spawnPoints.add(pos.above());
            }

            BlockPos[] finalizedSpawnPositions = spawnPoints.toArray(new BlockPos[0]);

            ReinforcementRequest request = new ReinforcementRequest(serverLevel, finalizedSpawnPositions);
            request.sender = ReinforcementRequest.senderType.Summoner;

            if (isWaterLogged) {
                request.approvedStrategicValues.add(EntityFactoryEntry.StrategicValues.Aquatic);
            } else {
                request.approvedStrategicValues.add(EntityFactoryEntry.StrategicValues.EffectiveOnGround);
            }

            request.is_aggressor_nearby = true;

            SculkHorde.entityFactory.createReinforcementRequestFromSummoner(serverLevel, pos, false, request);

            activatedCount++;
        }


        if (activatedCount > 0) {
            player.sendSystemMessage(Component.literal("§3Unleashed " + activatedCount + " Summoners (-" + (activatedCount * XP_COST_PER_SUMMONER) + " XP)"));
        } else if (player.totalExperience >= XP_COST_PER_SUMMONER || player.isCreative()) {
            player.sendSystemMessage(Component.literal("§3All nearby Summoners are currently active."));
        }
    }
}
