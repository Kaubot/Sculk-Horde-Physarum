package net.alekrus.shphysarum.Block;


import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;




public class SculkBeaconBlock extends BaseEntityBlock {

    public SculkBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        
        if (heldItem.getItem() == ModItems.MYCELIUM_SPROUT.get()) {
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof net.alekrus.shphysarum.Block.SculkBeaconBlockEntity beacon) {
                if (!beacon.isRaidActive()) {
                    if (!level.isClientSide) {

                        
                        if (!checkAreaClear(level, pos)) {
                            player.sendSystemMessage(Component.literal("§cArea obstructed! Remove solid blocks (stones, wood, leaves) in 40 blocks radius."));
                            return InteractionResult.FAIL;
                        }

                        
                        beacon.startRaid(player);
                        if (!player.isCreative()) {
                            heldItem.shrink(1);
                        }

                        
                        level.playSound(null, pos, SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(2).get(), SoundSource.BLOCKS, 3.0f, 1.0f);
                    }
                    return InteractionResult.SUCCESS;
                } else {
                    if (!level.isClientSide) player.sendSystemMessage(Component.literal("§7The raid is already active."));
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    
    private boolean checkAreaClear(Level level, BlockPos center) {
        int radius = 30;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) continue; 

                BlockPos groundPos = center.offset(x, 1, z); 
                BlockPos headPos = center.offset(x, 2, z);   

                
                
                

                if (isSolidObstruction(level, groundPos) || isSolidObstruction(level, headPos)) {
                    return false; 
                }
            }
        }
        return true;
    }

    
    private boolean isSolidObstruction(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        
        if (state.isAir()) return false;

        
        
        
        return !state.getCollisionShape(level, pos).isEmpty();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SculkBeaconBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.SCULK_BEACON_BE.get(), SculkBeaconBlockEntity::tick);
    }
}