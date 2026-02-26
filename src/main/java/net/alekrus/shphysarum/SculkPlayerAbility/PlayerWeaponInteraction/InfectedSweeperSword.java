package net.alekrus.shphysarum.SculkPlayerAbility.PlayerWeaponInteraction;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.common.item.SculkSweeperSword; 
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler; 
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfectedSweeperSword {

    
    @SubscribeEvent
    public static void onSwordUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        
        if (stack.getItem() instanceof SculkSweeperSword) {

            
            if (InfectionHandler.isInfected(player)) {

                
                if (stack.getDamageValue() == 0) {

                    
                    event.setCanceled(true);

                    
                    if (!player.level().isClientSide()) {
                        performInfectedSpikeAttack(player, stack);
                    }

                    
                    player.swing(event.getHand());
                }
            }
        }
    }

    
    private static void performInfectedSpikeAttack(Player owner, ItemStack stack) {
        
        AABB spikeHitbox = new AABB(owner.blockPosition()).inflate(20.0D);

        boolean hitSomeone = false;

        
        for (LivingEntity target : owner.level().getEntitiesOfClass(LivingEntity.class, spikeHitbox)) {
            if (target != owner) {
                
                boolean isSculk = EntityAlgorithms.isSculkLivingEntity.test(target);

                
                if (!isSculk) {
                    
                    SculkSpineSpikeAttackEntity spike = new SculkSpineSpikeAttackEntity(owner, target.getX(), target.getY(), target.getZ(), 0);
                    owner.level().addFreshEntity(spike);

                    
                    EntityAlgorithms.applyEffectToTarget(target, MobEffects.LEVITATION, TickUnits.convertSecondsToTicks(5), 1);
                    hitSomeone = true;
                }
            }
        }

        
        
        if (hitSomeone) {
            stack.setDamageValue(stack.getMaxDamage());
            
            owner.level().playSound(null, owner.blockPosition(), SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    
    @SubscribeEvent
    public static void onMobKill(LivingDeathEvent event) {
        
        if (event.getSource().getEntity() instanceof Player player) {

            
            if (InfectionHandler.isInfected(player)) {
                ItemStack stack = player.getMainHandItem();

                
                if (stack.getItem() instanceof SculkSweeperSword) {
                    LivingEntity victim = event.getEntity();

                    
                    boolean isSculk = EntityAlgorithms.isSculkLivingEntity.test(victim);

                    
                    if (!isSculk) {
                        
                        
                        
                        stack.setDamageValue(0);

                        
                        
                        
                    }
                }
            }
        }
    }
}