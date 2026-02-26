package net.alekrus.shphysarum.SculkPlayerAbility.XP;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkHordeBlockHandler {

    private static final float SCULK_HAND_BREAK_SPEED = 55.5f;
    
    private static final String WARD_BLOCK_ID = "sculkhorde:infestation_ward_block";

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player == null || player.isCreative()) return;

        if (!InfectionHandler.isClientInfected(player)) return;

        BlockState state = event.getState();

        
        if (!isSculkHordeBlock(state)) return;

        ItemStack heldItem = player.getMainHandItem();

        
        if (heldItem.isEmpty() || heldItem.is(Items.AIR)) {
            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());

            
            if (key != null && key.toString().equals(WARD_BLOCK_ID)) {
                
                event.setNewSpeed(SCULK_HAND_BREAK_SPEED / 2.0f);
            } else {
                
                event.setNewSpeed(SCULK_HAND_BREAK_SPEED);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.isCreative()) return;

        if (!InfectionHandler.isClientInfected(player)) return;

        Level level = event.getLevel() instanceof Level l ? l : null;
        if (level == null || level.isClientSide) return;

        BlockState state = event.getState();
        if (!isSculkHordeBlock(state)) return;

        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.isEmpty() || heldItem.is(Items.AIR)) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockPos pos = event.getPos();

            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());

            
            if (key != null && key.toString().equals(WARD_BLOCK_ID)) {
                
                MobEffect purityEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "purity"));

                if (purityEffect != null) {
                    
                    player.addEffect(new MobEffectInstance(purityEffect, 240, 0));
                }
            }
            

            
            int xp = 2;
            serverLevel.addFreshEntity(new ExperienceOrb(
                    serverLevel,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    xp
            ));
        }
    }

    private static boolean isSculkHordeBlock(BlockState state) {
        var key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (key == null) return false;

        if ("sculkhorde".equals(key.getNamespace())) {
            return true;
        }

        if ("minecraft".equals(key.getNamespace())) {
            String path = key.getPath();
            return path.equals("sculk_sensor") ||
                    path.equals("sculk_catalyst") ||
                    path.equals("sculk_shrieker") ||
                    path.equals("sculk_vein");
        }

        return false;
    }
}
