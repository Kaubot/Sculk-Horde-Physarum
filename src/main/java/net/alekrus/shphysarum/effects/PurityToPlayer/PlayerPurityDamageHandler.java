package net.alekrus.shphysarum.effects.PurityToPlayer;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerPurityDamageHandler {

    private static final int COOLDOWN_TICKS = TickUnits.convertSecondsToTicks(2);

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;

        if (!InfectionHandler.isInfected(player)) {
            if (player.getPersistentData().contains("purity_damage_cd")) {
                player.getPersistentData().putInt("purity_damage_cd", 0);
            }
            return;
        }

        MobEffectInstance inst = player.getEffect(ModMobEffects.PURITY.get());
        if (inst != null) {

            
            float multiplier = ResistanceLogic.getDurationMultiplier(player);
            if (multiplier < 1.0f) {
                
                
                

                
                

                float rate = (1.0f / multiplier) - 1.0f;
                int extraTicks = (int) rate;
                float chance = rate - extraTicks;

                if (player.getRandom().nextFloat() < chance) {
                    extraTicks++;
                }

                
                
                
                

                
                
            }
            


            int cooldown = player.getPersistentData().getInt("purity_damage_cd");

            if (cooldown > 0) {
                player.getPersistentData().putInt("purity_damage_cd", cooldown - 1);
            } else {

                
                
                

                
                

                player.hurt(player.damageSources().magic(), 1.0F);

                
                
                
                

                int modifiedCooldown = (int) (COOLDOWN_TICKS / Math.max(0.1f, multiplier));
                
                

                player.getPersistentData().putInt("purity_damage_cd", modifiedCooldown);
            }
        } else {
            player.getPersistentData().putInt("purity_damage_cd", 0);
        }
    }
}
