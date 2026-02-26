package net.alekrus.shphysarum.PointOfNoReturn;

import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkAppleTransformationHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);

        
        String blockName = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();

        if (blockName.equals("sculkhorde:sculk_ancient_node") || blockName.equals("sculkhorde:sculk_node")) {
            ItemStack stack = event.getItemStack();

            if (stack.is(Items.APPLE)) {
                if (!world.isClientSide) {
                    stack.shrink(1);
                    ItemStack sculkApple = new ItemStack(ModItems.SCULK_APPLE.get());

                    if (!event.getEntity().getInventory().add(sculkApple)) {
                        event.getEntity().drop(sculkApple, false);
                    }

                    event.getEntity().sendSystemMessage(Component.literal("§5The fruit began to change"));
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }
}