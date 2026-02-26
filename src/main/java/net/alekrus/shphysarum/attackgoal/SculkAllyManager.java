package net.alekrus.shphysarum.attackgoal;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.common.entity.SculkWitchEntity;
import com.github.sculkhorde.common.entity.SculkHatcherEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkAllyManager {

    public static final Map<Mob, LivingEntity> COMMANDED_TARGETS = new WeakHashMap<>();
    private static final double ASSISTANCE_RADIUS = 48.0;


    private static boolean hasSpecialAttackRequirement(Mob mob) {
        return mob instanceof SculkSpitterEntity
                || mob instanceof SculkWitchEntity
                || mob instanceof SculkHatcherEntity;
    }

    private static void alertNearbyAllies(Player leader, LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        if (target instanceof ISculkSmartEntity || target instanceof Player) return;

        Level level = leader.level();
        if (level.isClientSide) return;

        AABB searchArea = new AABB(leader.blockPosition()).inflate(ASSISTANCE_RADIUS);
        List<Mob> nearbyMobs = level.getEntitiesOfClass(Mob.class, searchArea);

        for (Mob ally : nearbyMobs) {
            if (ally instanceof ISculkSmartEntity) {
                COMMANDED_TARGETS.put(ally, target);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)) return;
        if (!(mob instanceof ISculkSmartEntity)) return;


        mob.targetSelector.addGoal(0, new SculkAllyGoals.PlayerCommandedTargetGoal(mob));


        if (hasSpecialAttackRequirement(mob)) {

        } else {

            List<Goal> toRemove = new ArrayList<>();
            for (WrappedGoal wg : mob.goalSelector.getAvailableGoals()) {
                String className = wg.getGoal().getClass().getName();
                if (className.contains("AttackGoal")) {
                    toRemove.add(wg.getGoal());
                }
            }
            toRemove.forEach(mob.goalSelector::removeGoal);

            mob.goalSelector.addGoal(2, new SculkAllyGoals.InjectedCustomMeleeAttackGoal(mob));
        }
    }

    @SubscribeEvent
    public static void onPlayerAttacks(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player && InfectionHandler.isInfected(player)) {
            LivingEntity victim = event.getEntity();
            alertNearbyAllies(player, victim);

            if (!player.isCreative() && !player.isSpectator()) {
                if (victim instanceof Mob mobVictim) {
                    mobVictim.setTarget(player);
                    mobVictim.setLastHurtByMob(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && InfectionHandler.isInfected(player)) {
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                alertNearbyAllies(player, attacker);
            }
        }
    }

    @SubscribeEvent
    public static void onMobTargetsPlayer(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player player && InfectionHandler.isInfected(player)) {
            alertNearbyAllies(player, event.getEntity());
        }
    }
}
