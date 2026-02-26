package net.alekrus.shphysarum.SculkPlayerAbility.MakeSculkNode;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.common.structures.procedural.SculkNodeProceduralStructure;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.ModSounds;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.Random;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkNodePlacementHandler {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getLevel().isClientSide()) return;

        BlockState placedState = event.getPlacedBlock();
        ServerLevel serverLevel = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();

        
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(placedState.getBlock());
        if (blockId == null || !blockId.getNamespace().equals("sculkhorde") || !blockId.getPath().equals("sculk_node")) {
            return;
        }

        
        if (InfectionHandler.isInfected(player)) {

            
            try {
                
                ModSavedData.getSaveData().resetNoNodeSpawningTicksElapsed();

                
                EntityType.LIGHTNING_BOLT.spawn(serverLevel, pos, MobSpawnType.SPAWNER);

                
                serverLevel.players().forEach(p -> p.displayClientMessage(Component.literal("A Sculk Node has spawned!"), true));

                
                serverLevel.players().forEach(p -> serverLevel.playSound(null, p.blockPosition(), ModSounds.NODE_SPAWN_SOUND.get(), SoundSource.HOSTILE, 1.0F, 1.0F));

                
                if (ModConfig.SERVER.should_sculk_nodes_and_raids_spawn_phantoms.get()) {
                    spawnPhantomsAtTopOfWorld(serverLevel, pos, 10);
                }
            } catch (Exception e) {
                

            }


            
            if (serverLevel.getBlockEntity(pos) instanceof SculkNodeBlockEntity nodeBE) {

                nodeBE.setActive(true);
                triggerBuildAnimation(serverLevel, nodeBE);

                
                player.displayClientMessage(Component.literal("§3The Node takes root and begins to construct itself..."), true);
            }
        }
    }

    
    private static void spawnPhantomsAtTopOfWorld(ServerLevel level, BlockPos origin, int amount) {
        int spawnRange = 100;
        int minimumSpawnRange = 50;
        Random rng = new Random();

        for (int i = 0; i < amount; ++i) {
            int x = minimumSpawnRange + rng.nextInt(spawnRange) - spawnRange / 2;
            int z = minimumSpawnRange + rng.nextInt(spawnRange) - spawnRange / 2;
            int y = level.getMaxBuildHeight(); 
            BlockPos spawnPosition = new BlockPos(origin.getX() + x, y, origin.getZ() + z);

            SculkPhantomEntity.spawnPhantom(level, spawnPosition, true);
        }
    }

    private static void triggerBuildAnimation(ServerLevel level, SculkNodeBlockEntity nodeBE) {
        try {
            Field structureField = SculkNodeBlockEntity.class.getDeclaredField("nodeProceduralStructure");
            structureField.setAccessible(true);

            SculkNodeProceduralStructure structure = (SculkNodeProceduralStructure) structureField.get(nodeBE);

            if (structure == null) {
                structure = new SculkNodeProceduralStructure(level, nodeBE.getBlockPos());
                structure.generatePlan();
                structureField.set(nodeBE, structure);
            }

            if (structure.canStartToBuild()) {
                structure.startBuildProcedure();
            }

            Field timeField = SculkNodeBlockEntity.class.getDeclaredField("timeOfLastRepair");
            timeField.setAccessible(true);
            timeField.setLong(nodeBE, level.getGameTime());

        } catch (Exception e) {

        }
    }
}
