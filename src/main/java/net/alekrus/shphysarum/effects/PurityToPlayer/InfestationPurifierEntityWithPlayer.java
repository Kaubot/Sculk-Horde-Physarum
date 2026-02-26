package net.alekrus.shphysarum.effects.PurityToPlayer;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "shphysarum")
public class InfestationPurifierEntityWithPlayer {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onPurifierTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof InfestationPurifierEntity purifier)) {
            return;
        }

        if (purifier.level().isClientSide()) {
            return;
        }

        
        if (RANDOM.nextInt(100) == 0) {
            if (purifier.level() instanceof ServerLevel serverLevel) {

                
                List<LivingEntity> entities = EntityAlgorithms.getLivingEntitiesInBoundingBox(
                        serverLevel,
                        purifier.getBoundingBox().inflate(10.0F)
                );

                for (LivingEntity entity : entities) {
                    if (entity == null || entity == purifier) continue;

                    
                    if (EntityAlgorithms.isSculkLivingEntity.test(entity) || EntityAlgorithms.isLivingEntityAllyToSculkHorde(entity)) {
                        attackTarget(entity, purifier);
                    }

                    
                    if (entity instanceof Player player) {
                        if (InfectionHandler.isInfected(player)) {
                            attackTarget(player, purifier);
                        }
                    }
                }
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        
        if (!(event.getTarget() instanceof InfestationPurifierEntity)) {
            return;
        }

        Player player = event.getEntity();

        
        if (InfectionHandler.isInfected(player)) {
            
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);

            if (!player.level().isClientSide) {
                
                player.level().playSound(null, player.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 2.0f);

                
                player.sendSystemMessage(Component.literal("§3The purity burns your infected skin, you cannot touch it."));

                
                player.hurt(player.damageSources().hotFloor(), 2.0f);
                player.setSecondsOnFire(3);
            }
        }
    }


    private static void attackTarget(LivingEntity target, InfestationPurifierEntity source) {
        
        int fireSeconds = 60;
        int effectDuration = 60;
        int amplifier = 3;
        float damage = 2.0F;

        
        if (target instanceof Player player) {
            float multiplier = ResistanceLogic.getDurationMultiplier(player);

            
            fireSeconds = Math.max(1, (int) (fireSeconds * multiplier));
            effectDuration = Math.max(20, (int) (effectDuration * multiplier));
        }

        
        target.setSecondsOnFire(fireSeconds);
        EntityAlgorithms.applyEffectToTarget(target, MobEffects.MOVEMENT_SLOWDOWN, effectDuration, amplifier);
        EntityAlgorithms.applyEffectToTarget(target, MobEffects.WEAKNESS, effectDuration, amplifier);
        EntityAlgorithms.applyEffectToTarget(target, MobEffects.POISON, effectDuration, amplifier);

        
        target.hurt(source.damageSources().magic(), damage);
    }
}
