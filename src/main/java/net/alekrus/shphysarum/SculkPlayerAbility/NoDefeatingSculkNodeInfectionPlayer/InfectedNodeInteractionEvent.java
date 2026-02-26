package net.alekrus.shphysarum.SculkPlayerAbility.NoDefeatingSculkNodeInfectionPlayer;

import com.github.sculkhorde.common.block.SculkAncientNodeBlock;
import com.github.sculkhorde.core.ModItems;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum") 
public class InfectedNodeInteractionEvent {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

        
        
        if (!InfectionHandler.isClientInfected(player)) {
            return;
        }

        
        
        if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof SculkAncientNodeBlock) {

            
            
            if (event.getItemStack().is((Item) ModItems.HEART_OF_PURITY.get())) {

                
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);

                
                if (!event.getLevel().isClientSide && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) {
                    player.sendSystemMessage(Component.literal("§3No."));
                }
            }
        }
    }
}
