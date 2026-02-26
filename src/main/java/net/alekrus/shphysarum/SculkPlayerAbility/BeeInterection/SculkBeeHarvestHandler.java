package net.alekrus.shphysarum.SculkPlayerAbility.BeeInterection;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.common.block.SculkBeeNestCellBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkBeeHarvestHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {

        if (event.getLevel().isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!InfectionHandler.isInfected(event.getEntity())) return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof SculkBeeNestCellBlock nestBlock)) {
            return;
        }

        if (nestBlock.isMature(state)) {
            

            
            Item resinItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "sculk_resin"));

            if (resinItem != null) {
                ItemStack resinStack = new ItemStack(resinItem, 1); 

                
                if (!event.getEntity().getInventory().add(resinStack)) {
                    
                    event.getEntity().drop(resinStack, false);
                } else {
                    
                    event.getEntity().playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((event.getEntity().getRandom().nextFloat() - event.getEntity().getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                }
            }

            

            

            
            nestBlock.resetMature(event.getLevel(), state, event.getPos());

            
            event.getLevel().playSound(null, event.getPos(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 1.0F, 1.0F);

            
            event.getEntity().swing(InteractionHand.MAIN_HAND, true);

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
