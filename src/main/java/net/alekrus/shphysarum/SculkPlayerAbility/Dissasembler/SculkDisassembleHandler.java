package net.alekrus.shphysarum.SculkPlayerAbility.Dissasembler;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "shphysarum")
public class SculkDisassembleHandler {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();

        if (!InfectionHandler.isInfected(player)) {
            return;
        }
        if (!player.isCrouching()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (!(event.getTarget() instanceof LivingEntity target)) return;

        ServerLevel level = (ServerLevel) player.level();

        ResourceLocation entRL = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (entRL == null) return;
        String entId = entRL.toString();

        boolean handled = false;

        switch (entId) {
            case "sculkhorde:sculk_spitter" -> {
                
                if (RNG.nextFloat() < 0.40f) {
                    dropItem(level, target, "sculkhorde:sculk_acidic_projectile", 1, 1);
                }
                
                if (RNG.nextFloat() < 0.30f) {
                    dropItem(level, target, "minecraft:bone", 1, 1);
                }
                dropExperience(level, target, 1, 3);
                handled = true;
            }
            case "sculkhorde:sculk_creeper" -> {
                
                if (RNG.nextFloat() < 0.35f) {
                    dropItem(level, target, "minecraft:gunpowder", 1, 2);
                }
                dropExperience(level, target, 2, 5);
                handled = true;
            }
            case "sculkhorde:sculk_sheep" -> {
                
                if (RNG.nextFloat() < 0.50f) {
                    dropItem(level, target, "minecraft:white_wool", 1, 1);
                }
                dropExperience(level, target, 2, 4);
                handled = true;
            }
            case "sculkhorde:sculk_zombie" -> {
                
                if (RNG.nextFloat() < 0.40f) {
                    dropItem(level, target, "minecraft:rotten_flesh", 1, 1);
                }
                dropExperience(level, target, 1, 3);
                handled = true;
            }
            case "sculkhorde:sculk_hatcher" -> {
                
                if (RNG.nextFloat() < 0.30f) {
                    dropItem(level, target, "minecraft:leather", 1, 1);
                }
                dropExperience(level, target, 2, 4);
                handled = true;
            }
            case "sculkhorde:sculk_vindicator" -> {
                
                if (RNG.nextFloat() < 0.10f) {
                    dropItem(level, target, "minecraft:emerald", 1, 1);
                }
                dropExperience(level, target, 4, 6);
                handled = true;
            }
        }

        if (handled) {
            spawnPoofAndSound(level, target);
            target.discard(); 
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static void dropItem(ServerLevel level, LivingEntity entity, String itemId, int min, int max) {
        int count = RNG.nextInt(max - min + 1) + min;
        if (count <= 0) return; 

        ResourceLocation rl = parseResourceLocation(itemId);
        if (rl == null) return;

        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) return;

        ItemStack stack = new ItemStack(item, count);
        ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getY() + 0.25, entity.getZ(), stack);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    private static void dropExperience(ServerLevel level, LivingEntity entity, int min, int max) {
        int amount = RNG.nextInt(max - min + 1) + min;
        if (amount > 0) {
            ExperienceOrb orb = new ExperienceOrb(level, entity.getX(), entity.getY() + 0.5, entity.getZ(), amount);
            level.addFreshEntity(orb);
        }
    }

    private static void spawnPoofAndSound(ServerLevel level, LivingEntity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_DEATH, SoundSource.PLAYERS, 1.0F, 1.0F);
        for (int i = 0; i < 20; i++) {
            double rx = (RNG.nextDouble() - 0.5) * 0.8;
            double ry = RNG.nextDouble() * 0.8;
            double rz = (RNG.nextDouble() - 0.5) * 0.8;
            level.sendParticles(ParticleTypes.POOF,
                    entity.getX() + rx,
                    entity.getY() + 0.5 + ry,
                    entity.getZ() + rz,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static ResourceLocation parseResourceLocation(String s) {
        if (s == null || s.isEmpty() || !s.contains(":")) return null;
        String[] parts = s.split(":", 2);
        return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
    }
}
