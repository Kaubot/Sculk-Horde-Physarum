package net.alekrus.shphysarum.SculkPlayerAbility.Spawn;

import net.alekrus.shphysarum.Block.SoulAnchorBlock.SoulAnchorBlock;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
public class SculkSummonerSpawnHandler {

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

        
        if (respawnPos == null) return;
        
        if (!player.getRespawnDimension().equals(level.dimension())) return;

        
        BlockPos blockPos = respawnPos.below();
        BlockState state = level.getBlockState(blockPos);

        
        if (state.getBlock() instanceof SoulAnchorBlock) {
            int charges = state.getValue(SoulAnchorBlock.CHARGES);

            if (charges > 0) {
                
                level.setBlock(blockPos, state.setValue(SoulAnchorBlock.CHARGES, charges - 1), 3);
                level.playSound(null, blockPos, SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                
                player.sendSystemMessage(Component.literal("§cSoul Anchor depleted. Spawnpoint reset."));

                
                
                
                player.setRespawnPosition(level.dimension(), null, 0.0f, false, false);
            }
        }
    }

    
    private static boolean isBlock(BlockState state, ResourceLocation targetId) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && id.equals(targetId);
    }
}
