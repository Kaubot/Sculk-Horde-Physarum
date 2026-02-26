package net.alekrus.shphysarum.attackgoal.NoDamageFromBoss.EndermanSculk;
import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.common.entity.projectile.SculkAcidicProjectileEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.*;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkFriendlyFireHandler {


    private static boolean isInfected(Player player) {
        return player.getCapability(SculkMindProvider.SCULK_MIND)
                .map(cap -> cap.hasSkill("root"))
                .orElse(false);
    }


    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {

        if (event.getRayTraceResult() instanceof EntityHitResult hitResult) {


            if (hitResult.getEntity() instanceof Player player && isInfected(player)) {

                Projectile projectile = event.getProjectile();
                Entity shooter = projectile.getOwner();

                boolean shouldCancel = false;


                ResourceLocation regName = ForgeRegistries.ENTITY_TYPES.getKey(projectile.getType());
                if (regName != null && regName.toString().equals("sculkhorde:sculk_acidic_projectile")) {
                    shouldCancel = true;
                }


                if (projectile instanceof SculkAcidicProjectileEntity || projectile instanceof DragonFireball) {

                    if (projectile instanceof DragonFireball) {
                        if (shooter instanceof SculkEndermanEntity) shouldCancel = true;
                    } else {
                        shouldCancel = true;
                    }
                }


                if (shooter instanceof SculkSpitterEntity || shooter instanceof SculkEndermanEntity) {
                    shouldCancel = true;
                }


                if (shouldCancel) {
                    event.setCanceled(true);
                }
            }
        }
    }


    private static boolean isSculkFriendlyFire(DamageSource source) {
        Entity trueSource = source.getEntity();
        Entity directSource = source.getDirectEntity();

        if (trueSource instanceof SculkEndermanEntity || trueSource instanceof SculkSpitterEntity) return true;
        if (directSource instanceof SculkSpineSpikeAttackEntity || directSource instanceof EnderBubbleAttackEntity || directSource instanceof ChaosTeleporationRiftEntity) return true;


        if (directSource instanceof AreaEffectCloud cloud) {
            if (cloud.getOwner() instanceof SculkSpitterEntity || cloud.getOwner() instanceof SculkEndermanEntity) return true;
        }

        return false;
    }


    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && isInfected(player)) {
            if (isSculkFriendlyFire(event.getSource())) {
                event.setCanceled(true);
            }
        }
    }


    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && isInfected(player)) {
            if (isSculkFriendlyFire(event.getSource())) {
                event.setCanceled(true);
                event.setAmount(0);
            }
        }
    }


    @SubscribeEvent
    public static void onPotionApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player && isInfected(player)) {
            var effect = event.getEffectInstance().getEffect();
            if (effect == MobEffects.LEVITATION || effect == MobEffects.WEAKNESS || effect == MobEffects.DARKNESS || effect == MobEffects.POISON) {

                boolean nearThreat = !player.level().getEntitiesOfClass(SculkSpineSpikeAttackEntity.class, player.getBoundingBox().inflate(4.0)).isEmpty() ||
                        !player.level().getEntitiesOfClass(ChaosTeleporationRiftEntity.class, player.getBoundingBox().inflate(4.0)).isEmpty() ||
                        !player.level().getEntitiesOfClass(SculkAcidicProjectileEntity.class, player.getBoundingBox().inflate(4.0)).isEmpty() ||
                        !player.level().getEntitiesOfClass(SculkSpitterEntity.class, player.getBoundingBox().inflate(8.0)).isEmpty() ||
                        !player.level().getEntitiesOfClass(SculkEndermanEntity.class, player.getBoundingBox().inflate(15.0)).isEmpty();

                if (nearThreat) {
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }


    @SubscribeEvent
    public static void onTeleportRift(EntityTeleportEvent.ChorusFruit event) {
        if (event.getEntity() instanceof Player player && isInfected(player)) {
            boolean isNearRift = !player.level().getEntitiesOfClass(ChaosTeleporationRiftEntity.class, player.getBoundingBox().inflate(4.0)).isEmpty();
            if (isNearRift) {
                event.setCanceled(true);
            }
        }
    }
}