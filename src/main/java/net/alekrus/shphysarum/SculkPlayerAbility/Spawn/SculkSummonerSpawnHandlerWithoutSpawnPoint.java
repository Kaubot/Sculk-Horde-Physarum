package net.alekrus.shphysarum.SculkPlayerAbility.Spawn;

import net.alekrus.shphysarum.Block.SoulAnchorBlock.SoulAnchorBlock;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkSummonerSpawnHandlerWithoutSpawnPoint {

    private static final ResourceLocation SCULK_SUMMONER_ID = ResourceLocation.fromNamespaceAndPath("sculkhorde", "sculk_summoner");

    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        if (isBlock(state, SCULK_SUMMONER_ID)) {
            if (!world.isClientSide && event.getEntity() instanceof ServerPlayer player) {
                if (!InfectionHandler.isInfected(player)) {
                    event.setCancellationResult(InteractionResult.FAIL);
                    event.setCanceled(true);
                    return;
                }
                BlockPos spawnPos = pos.above();
                player.setRespawnPosition(world.dimension(), spawnPos, player.getYRot(), true, false);
                player.displayClientMessage(Component.literal("§3Spawnpoint set to Sculk Summoner"), true);
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (!InfectionHandler.isInfected(player)) return;

        ServerLevel level = (ServerLevel) player.level();
        BlockPos respawnPos = player.getRespawnPosition();
        ResourceKey<Level> respawnDim = player.getRespawnDimension();

        
        
        if (respawnPos == null || !respawnDim.equals(level.dimension())) {
            return;
        }

        
        BlockPos foundAnchorPos = findAnchorOrSummoner(level, respawnPos);

        if (foundAnchorPos != null) {
            BlockState state = level.getBlockState(foundAnchorPos);

            
            if (state.getBlock() instanceof SoulAnchorBlock) {
                int charges = state.getValue(SoulAnchorBlock.CHARGES);
                if (charges > 0) {
                    
                    level.setBlock(foundAnchorPos, state.setValue(SoulAnchorBlock.CHARGES, charges - 1), 3);
                    level.playSound(null, foundAnchorPos, SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

                    
                    return;
                } else {
                    
                    player.sendSystemMessage(Component.literal("§cSoul Anchor depleted. Respawning at world spawn..."));
                    
                    resetSpawnToWorld(player, level);
                }
            }
            
            else if (isBlock(state, SCULK_SUMMONER_ID)) {
                
                return;
            }
        } else {
            
            player.sendSystemMessage(Component.literal("§cYour Spawnpoint Block is missing. Respawning at world spawn..."));
            resetSpawnToWorld(player, level);
        }
    }

    
    private static void resetSpawnToWorld(ServerPlayer player, ServerLevel level) {
        
        player.setRespawnPosition(level.dimension(), null, 0.0f, false, false);
    }

    
    private static BlockPos findAnchorOrSummoner(Level level, BlockPos center) {
        
        BlockPos below = center.below();
        BlockState stateBelow = level.getBlockState(below);
        if (stateBelow.getBlock() instanceof SoulAnchorBlock || isBlock(stateBelow, SCULK_SUMMONER_ID)) {
            return below;
        }

        
        BlockState stateAt = level.getBlockState(center);
        if (stateAt.getBlock() instanceof SoulAnchorBlock || isBlock(stateAt, SCULK_SUMMONER_ID)) {
            return center;
        }

        return null;
    }

    private static boolean isBlock(BlockState state, ResourceLocation targetId) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && id.equals(targetId);
    }
}
