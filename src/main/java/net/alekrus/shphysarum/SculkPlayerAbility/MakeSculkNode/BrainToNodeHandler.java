package net.alekrus.shphysarum.SculkPlayerAbility.MakeSculkNode;

import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems; 
import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BrainToNodeHandler {

    private static final int LEVEL_COST = 100;

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide) return;

        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();

        
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(heldItem.getItem());
        if (itemId == null || !itemId.getNamespace().equals("sculkhorde") || !itemId.getPath().equals("chunk_o_brain")) {
            return;
        }

        
        if (!InfectionHandler.isInfected(player)) {
            return;
        }

        
        if (player.experienceLevel < LEVEL_COST && !player.isCreative()) {
            player.displayClientMessage(Component.literal("§cYou need " + LEVEL_COST + " Levels of experience to condense a Node Scaffold."), true);
            return;
        }

        
        if (!player.isCreative()) {
            player.giveExperienceLevels(-LEVEL_COST);
        }

        
        ItemStack scaffoldStack = new ItemStack(ModItems.MYCELIAL_BRAIN_SCAFFOLD.get());

        
        if (heldItem.getCount() == 1) {
            player.setItemInHand(event.getHand(), scaffoldStack);
        } else {
            heldItem.shrink(1);
            if (!player.getInventory().add(scaffoldStack)) {
                player.drop(scaffoldStack, false);
            }
        }

        
        event.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 1.0F, 1.0F);

        event.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.5F);

        player.displayClientMessage(Component.literal("§3You make a Mycelial Brain Scaffold."), true);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
