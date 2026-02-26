package net.alekrus.shphysarum.SculkPlayerAbility.MobBehavior;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HostileNeutralHandler {

    
    private static final Predicate<Player> IS_INFECTED_TARGET = (player) -> {
        return !player.isCreative() && !player.isSpectator() && InfectionHandler.isInfected(player) && player.isAlive();
    };

    
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        boolean isWrathGolem = id != null && id.toString().equals("sculkhorde:golem_of_wrath");

        
        if (isWrathGolem || isPeacefulCombatMob(mob)) {
            
            mob.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(
                    mob,
                    Player.class,
                    true,
                    (entity) -> entity instanceof Player p && IS_INFECTED_TARGET.test(p)
            ));
        }
    }

    
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) return;

        
        if (!(event.getEntity() instanceof Mob mob)) return;

        
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (id == null || !id.toString().equals("sculkhorde:golem_of_wrath")) return;

        
        if (mob.tickCount % 20 != 0) return;

        
        if (mob.getTarget() != null && mob.getTarget().isAlive()) return;

        
        double range = 16.0D;
        AABB searchBox = mob.getBoundingBox().inflate(range);

        List<Player> nearbyPlayers = mob.level().getEntitiesOfClass(Player.class, searchBox, IS_INFECTED_TARGET);

        if (!nearbyPlayers.isEmpty()) {
            
            nearbyPlayers.sort(Comparator.comparingDouble(mob::distanceToSqr));
            Player target = nearbyPlayers.get(0);

            
            mob.setTarget(target);

            
            if (mob.getTarget() == target) {
                
            }
        }
    }

    private static boolean isPeacefulCombatMob(Mob mob) {
        if (mob instanceof IronGolem || mob instanceof SnowGolem) return true;
        if (mob instanceof Wolf) return true;
        if (mob instanceof PolarBear) return true;
        if (mob instanceof Bee) return true;
        if (mob instanceof Panda) return true;
        if (mob instanceof Llama || mob instanceof TraderLlama) return true;
        if (mob instanceof Dolphin) return true;
        if (mob instanceof Pufferfish) return true;
        if (mob instanceof Goat) return true;
        return false;
    }
}
