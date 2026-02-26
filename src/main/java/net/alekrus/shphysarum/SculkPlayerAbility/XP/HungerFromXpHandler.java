package net.alekrus.shphysarum.SculkPlayerAbility.XP;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HungerFromXpHandler {

    private static final int XP_COST_PER_HALF_HEART = 44;
    private static final int ACTIVATION_DELAY = 60; 

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;
        if (!InfectionHandler.isInfected(player)) return;

        ServerLevel level = player.serverLevel();

        
        if (level.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
            level.getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, player.server);
        }
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5f);

        
        boolean isHoldingButton = player.getPersistentData().getBoolean("ImmediateHealingActive");

        
        boolean isFullyCharged = false;

        
        if (isHoldingButton) {
            
            
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2, 2, false, false, false));

            
            int currentCharge = player.getPersistentData().getInt("ImmediateChargeTicks");
            currentCharge++;
            player.getPersistentData().putInt("ImmediateChargeTicks", currentCharge);

            
            if (currentCharge >= ACTIVATION_DELAY) {
                isFullyCharged = true;
            }

        } else {
            
            player.getPersistentData().putInt("ImmediateChargeTicks", 0);
        }

        
        if (isFullyCharged) {
            
            ParticleType<?> particle = ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "burrowed_burst_particle"));
            if (particle != null && player.tickCount % 2 == 0) {
                double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.8;
                double y = player.getY() + 1.2;
                double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.8;
                level.sendParticles((ParticleOptions) particle, x, y, z, 1, 0, -0.1, 0, 0.05);
            }
        }

        
        if (player.getHealth() < player.getMaxHealth()) {
            AtomicInteger regenSpeed = new AtomicInteger(85);

            if (isFullyCharged) {
                regenSpeed.set(3); 
            } else {
                
                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                    if (cap.hasSkill("xp_proc_5")) regenSpeed.set(22);
                    else if (cap.hasSkill("xp_proc_4")) regenSpeed.set(31);
                    else if (cap.hasSkill("xp_proc_3")) regenSpeed.set(55);
                    else if (cap.hasSkill("xp_proc_2")) regenSpeed.set(60);
                    else if (cap.hasSkill("xp_proc_1")) regenSpeed.set(70);
                });
            }

            int tick = player.getPersistentData().getInt("xp_regen_tick");
            tick++;

            if (tick >= regenSpeed.get()) {
                if (player.totalExperience >= XP_COST_PER_HALF_HEART) {
                    player.giveExperiencePoints(-XP_COST_PER_HALF_HEART);
                    player.heal(1.0F);
                }
                tick = 0;
            }
            player.getPersistentData().putInt("xp_regen_tick", tick);
        } else {
            player.getPersistentData().putInt("xp_regen_tick", 0);
        }
    }
}
