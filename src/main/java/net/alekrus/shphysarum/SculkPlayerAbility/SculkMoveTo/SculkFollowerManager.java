package net.alekrus.shphysarum.SculkPlayerAbility.SculkMoveTo;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class SculkFollowerManager {

    private static final Map<UUID, Set<UUID>> followersMap = new HashMap<>();

    
    public static final List<String> TIER_0_MOBS = List.of(
            "sculkhorde:sculk_mite", "sculkhorde:sculk_mite_aggressor",
            "sculkhorde:sculk_leech", "sculkhorde:sculk_salmon", "sculkhorde:sculk_stinger"
    );
    public static final List<String> TIER_1_MOBS = List.of(
            "sculkhorde:sculk_zombie", "sculkhorde:sculk_pufferfish",
            "sculkhorde:sculk_spitter", "sculkhorde:sculk_hatcher"
    );
    public static final List<String> TIER_2_MOBS = List.of(
            "sculkhorde:sculk_creeper", "sculkhorde:sculk_sheep",
            "sculkhorde:sculk_phantom", "sculkhorde:sculk_vindicator"
    );
    public static final List<String> TIER_3_MOBS = List.of(
            "sculkhorde:sculk_squid", "sculkhorde:sculk_ravager", "sculkhorde:sculk_witch"
    );
    public static final List<String> TIER_4_MOBS = List.of(
            "sculkhorde:sculk_enderman"
    );

    public static void toggleFollow(ServerPlayer player) {
        UUID playerId = player.getUUID();

        
        if (followersMap.containsKey(playerId) && !followersMap.get(playerId).isEmpty()) {
            clearFollowers(player);
            player.sendSystemMessage(Component.literal("§cConnection severed. Units released."));
            return;
        }

        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {

            
            int maxPossible = 2; 
            if (cap.hasSkill("connection_t1")) maxPossible += 2;
            if (cap.hasSkill("connection_t2")) maxPossible += 2;
            if (cap.hasSkill("connection_t3")) maxPossible += 2;

            
            int userLimit = cap.getUserFollowerLimit();
            
            int effectiveLimit = Math.min(userLimit, maxPossible);

            if (effectiveLimit <= 0) {
                player.sendSystemMessage(Component.literal("§eConnection limit set to 0. Update settings."));
                return;
            }

            
            Set<String> allowedTypes = cap.getAllowedFollowerTypes();
            
            boolean useWhitelist = !allowedTypes.isEmpty();

            List<Mob> nearby = player.level().getEntitiesOfClass(
                    Mob.class,
                    player.getBoundingBox().inflate(30),
                    e -> e instanceof ISculkSmartEntity
            );
            nearby.sort(Comparator.comparingDouble(player::distanceToSqr));

            Set<UUID> newFollowers = new HashSet<>();
            int count = 0;

            for (Mob mob : nearby) {
                if (count >= effectiveLimit) break;

                String mobId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString();

                
                if (!isMobUnlocked(mobId, cap)) continue;

                
                
                if (useWhitelist && !allowedTypes.contains(mobId)) continue;

                newFollowers.add(mob.getUUID());
                count++;
            }

            if (!newFollowers.isEmpty()) {
                followersMap.put(playerId, newFollowers);
                player.sendSystemMessage(Component.literal("§aConnection established. Units: " + count + "/" + effectiveLimit));
            } else {
                player.sendSystemMessage(Component.literal("§eNo compatible units found in range."));
            }
        });
    }

    private static boolean isMobUnlocked(String mobId, net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.ISculkMind cap) {
        
        if (TIER_0_MOBS.contains(mobId)) return cap.hasSkill("unknown_connection_root"); 
        if (TIER_1_MOBS.contains(mobId)) return cap.hasSkill("connection_t1");
        if (TIER_2_MOBS.contains(mobId)) return cap.hasSkill("connection_t2");
        if (TIER_3_MOBS.contains(mobId)) return cap.hasSkill("connection_t3");
        if (TIER_4_MOBS.contains(mobId)) return cap.hasSkill("connection_t4");
        return false;
    }

    

    public static void removeFollower(ServerPlayer player, Mob mob) {
        if (followersMap.containsKey(player.getUUID())) {
            followersMap.get(player.getUUID()).remove(mob.getUUID());
        }
    }

    private static void clearFollowers(ServerPlayer player) {
        followersMap.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        followersMap.forEach((playerId, mobIds) -> {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(playerId);
            if (player == null || !player.isAlive() || !InfectionHandler.isInfected(player)) return;

            Iterator<UUID> it = mobIds.iterator();
            while (it.hasNext()) {
                UUID mobId = it.next();
                Entity entity = player.serverLevel().getEntity(mobId);

                if (entity instanceof Mob mob && mob.isAlive()) {
                    double distSq = player.distanceToSqr(mob);
                    if (distSq > 100) { 
                        mob.getNavigation().moveTo(player, 1.4D);
                    } else if (distSq > 9) {
                        mob.getNavigation().moveTo(player, 1.2D);
                    }
                } else {
                    it.remove();
                }
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clearFollowers(player);
        }
    }
}
