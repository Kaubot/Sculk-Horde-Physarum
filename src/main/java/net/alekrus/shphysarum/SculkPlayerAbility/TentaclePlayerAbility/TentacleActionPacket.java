package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import net.alekrus.shphysarum.AnimationAll.VisualSyncHelper;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.function.Supplier;

public class TentacleActionPacket {
    public enum ActionType { ATTACK, BLOCK_START, BLOCK_END }
    private final ActionType type;

    public TentacleActionPacket(ActionType type) { this.type = type; }
    public static TentacleActionPacket decode(FriendlyByteBuf buf) { return new TentacleActionPacket(ActionType.values()[buf.readInt()]); }
    public void encode(FriendlyByteBuf buf) { buf.writeInt(type.ordinal()); }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {

                if (!cap.areTentaclesActive()) {
                    player.getPersistentData().putBoolean("sh_isBlocking", false);
                    return;
                }

                if (!player.isCreative() && player.totalExperience <= 0) {
                    player.displayClientMessage(Component.literal("§cBiomass depleted."), true);
                    player.getPersistentData().putBoolean("sh_isBlocking", false);
                    return;
                }

                if (type == ActionType.ATTACK) {
                    player.getPersistentData().putLong("sh_lastAttackTime", System.currentTimeMillis());
                    VisualSyncHelper.syncToTracking(player);

                    
                    double range = 5.0; 

                    
                    AABB hitBox = player.getBoundingBox().inflate(range);

                    
                    List<LivingEntity> targets = player.level().getEntitiesOfClass(
                            LivingEntity.class,
                            hitBox,
                            entity -> entity != player && entity.isAlive() && entity.distanceToSqr(player) <= (range * range)
                    );

                    
                    MobEffect infectedEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse("sculkhorde:sculk_infected"));
                    MobEffect lureEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse("sculkhorde:sculk_lure"));

                    for (LivingEntity target : targets) {
                        
                        target.hurt(player.damageSources().playerAttack(player), 8.0f);

                        
                        if (infectedEffect != null) {
                            target.addEffect(new MobEffectInstance(infectedEffect, 200, 0, false, true));
                        }

                        
                        if (lureEffect != null) {
                            target.addEffect(new MobEffectInstance(lureEffect, 1200, 0, false, true));
                        }
                    }

                    
                    SoundEvent attackSound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.tryParse("sculkhorde:sculk_tendril_attack"));
                    if (attackSound == null) attackSound = SoundEvents.PLAYER_ATTACK_SWEEP; 

                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), attackSound, SoundSource.PLAYERS, 1.0f, 0.7f + (player.getRandom().nextFloat() * 0.3f));

                }
                else if (type == ActionType.BLOCK_START) {
                    player.getPersistentData().putBoolean("sh_isBlocking", true);
                    VisualSyncHelper.syncToTracking(player);
                }
                else if (type == ActionType.BLOCK_END) {
                    player.getPersistentData().putBoolean("sh_isBlocking", false);
                    VisualSyncHelper.syncToTracking(player);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
