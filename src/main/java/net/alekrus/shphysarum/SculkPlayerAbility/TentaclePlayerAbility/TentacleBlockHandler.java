package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TentacleBlockHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        
        if (player.getPersistentData().getBoolean("sh_isBlocking")) {
            DamageSource source = event.getSource();

            if (isUnblockable(source)) return;

            Vec3 sourcePos = source.getSourcePosition();
            if (sourcePos != null) {
                Vec3 viewVec = player.getViewVector(1.0F);
                Vec3 distVec = sourcePos.vectorTo(player.position()).normalize();

                if (distVec.dot(viewVec) < 0.0D) {
                    event.setCanceled(true);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F + player.level().random.nextFloat() * 0.4F);

                    Entity directEntity = source.getDirectEntity();
                    if (directEntity != null && !(directEntity instanceof Projectile)) {
                        Entity attacker = source.getEntity();
                        if (attacker instanceof LivingEntity livingAttacker) {
                            livingAttacker.knockback(0.5D, player.getX() - livingAttacker.getX(), player.getZ() - livingAttacker.getZ());
                        }
                    }

                    if (directEntity instanceof Projectile projectile) {
                        if (projectile instanceof ThrownTrident) {
                            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F);
                        }
                        Vec3 motion = projectile.getDeltaMovement();
                        projectile.setDeltaMovement(motion.scale(-0.5));
                        projectile.setYRot(projectile.getYRot() + 180.0F);
                        projectile.yRotO += 180.0F;
                        projectile.hasImpulse = true;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        
        if (player.getPersistentData().getBoolean("sh_isBlocking")) {
            DamageSource source = event.getSource();

            if (isUnblockable(source)) return;

            Vec3 sourcePos = source.getSourcePosition();
            if (sourcePos != null) {
                Vec3 viewVec = player.getViewVector(1.0F);
                Vec3 distVec = sourcePos.vectorTo(player.position()).normalize();

                if (distVec.dot(viewVec) > 0.0D) {
                    float newDamage = event.getAmount() * 1.5f;
                    event.setAmount(newDamage);
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.5F);
                }
            }
        }
    }

    private static boolean isUnblockable(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) return true;
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true;
        if (source.is(DamageTypeTags.IS_FIRE)) return true;
        if (source.is(DamageTypeTags.IS_FALL)) return true;
        if (source.is(DamageTypeTags.IS_DROWNING)) return true;
        if (source.is(DamageTypeTags.IS_FREEZING)) return true;
        return false;
    }
}
