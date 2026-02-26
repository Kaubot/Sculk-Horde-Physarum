package net.alekrus.shphysarum.SculkPlayerAbility.FastHealing;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImmediateDamageHandler {

    private static final int ACTIVATION_DELAY = 60; 

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {

            
            int chargeTicks = player.getPersistentData().getInt("ImmediateChargeTicks");

            
            if (chargeTicks >= ACTIVATION_DELAY) {
                
                event.setAmount(event.getAmount() * 3.0f);
            }
        }
    }
}