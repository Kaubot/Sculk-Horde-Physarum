package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.SupportTabInteraction;

import net.alekrus.shphysarum.Block.SoulAnchorBlock.SoulAnchorBlock;
import net.alekrus.shphysarum.PacketHandler;


import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.FaithSystem.FaithHandler;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.core.ModEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class SupportHandler {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide) return;
        UUID playerId = event.getEntity().getUUID();

        if (activeTeleports.containsKey(playerId)) {
            TeleportData data = activeTeleports.get(playerId);
            if (data.enderman != null && data.enderman.isAlive()) {
                data.enderman.discard();
            }
            activeTeleports.remove(playerId);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) return;
        event.getEntity().removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        event.getEntity().removeEffect(MobEffects.JUMP);
        event.getEntity().removeEffect(MobEffects.BLINDNESS);
    }

    public static void executeSporeDelivery(ServerPlayer player) {
        int cost = 11;
        if (FaithHandler.getFaith(player) < cost) {
            GravemindMessagePacket.sendToPlayer(player, "Not enough Faith.");
            return;
        }
        FaithHandler.addFaith(player, -cost);

        EntityType<?> sporeMob = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse("sculkhorde:sculk_spore_spewer"));

        if (sporeMob == null) {
            return;
        }

        spawnReinforcementGeneric(player, sporeMob, 1, "Spore Spewer deployed.");
    }

    public static void executeSkeletonReinforcement(ServerPlayer player) {
        int cost = 4;
        if (FaithHandler.getFaith(player) < cost) {
            GravemindMessagePacket.sendToPlayer(player, "Not enough Faith.");
            return;
        }
        FaithHandler.addFaith(player, -cost);

        spawnReinforcementGeneric(player, ModEntities.SCULK_SPITTER.get(), 6, "Skeleton Reinforcements");
    }

    public static void executeWitchReinforcement(ServerPlayer player) {
        int cost = 3;
        if (FaithHandler.getFaith(player) < cost) {
            GravemindMessagePacket.sendToPlayer(player, "Not enough Faith.");
            return;
        }
        FaithHandler.addFaith(player, -cost);

        spawnReinforcementGeneric(player, ModEntities.SCULK_WITCH.get(), 1, "Support Reinforcement");
    }

    public static void executeCreeperSupport(ServerPlayer player) {
        int cost = 8;
        if (FaithHandler.getFaith(player) < cost) {
            GravemindMessagePacket.sendToPlayer(player, "Not enough Faith.");
            return;
        }
        FaithHandler.addFaith(player, -cost);

        EntityType<?> creeperType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse("sculkhorde:sculk_creeper"));
        if (creeperType == null) creeperType = EntityType.CREEPER;

        spawnReinforcementGeneric(player, creeperType, 3, "Boom squad arrived");
    }

    private static final Map<UUID, TeleportData> activeTeleports = new HashMap<>();

    public static void startTeleportSequence(ServerPlayer player, BlockPos target) {
        if (target.equals(BlockPos.ZERO)) {
            GravemindMessagePacket.sendToPlayer(player, "Invalid coordinate.");
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(target);

        if (!(state.getBlock() instanceof SoulAnchorBlock)) {
            player.sendSystemMessage(Component.literal("§cTarget Anchor is destroyed or missing. Link severed."));

            player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                cap.removeKnownAnchor(target);
                PacketHandler.CHANNEL.sendTo(
                        new SkillSyncPacket(
                                cap.getUnlockedSkills(),
                                cap.getEvoPoints(),
                                cap.getFaith(),
                                cap.getActiveTaskNBT(),
                                cap.areTentaclesActive(),
                                cap.getUserFollowerLimit(),
                                cap.getAllowedFollowerTypes(),
                                cap.getKnownAnchors(),
                                cap.getActiveAbilitiesSet()
                        ),
                        player.connection.connection,
                        NetworkDirection.PLAY_TO_CLIENT
                );
            });
            return;
        }

        int cost = 5;
        if (FaithHandler.getFaith(player) < cost) {
            GravemindMessagePacket.sendToPlayer(player, "Not enough Faith.");
            return;
        }
        FaithHandler.addFaith(player, -cost);

        SculkEndermanEntity enderman = createCourier(level, player.blockPosition(), player);
        if (enderman != null) {
            enderman.setPos(player.getX() + 1.5, player.getY(), player.getZ() + 1.5);
            level.addFreshEntity(enderman);
        }

        playTeleportSound(level, player.blockPosition());

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 255, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 100, 250, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));

        GravemindMessagePacket.sendToPlayer(player, "Hold still. Folding space...");

        activeTeleports.put(player.getUUID(), new TeleportData(player, enderman, target, 60, 60));
    }

    private static final List<ReinforcementData> activeReinforcements = new ArrayList<>();

    private static void spawnReinforcementGeneric(ServerPlayer player, EntityType<?> mobType, int count, String name) {
        ServerLevel level = player.serverLevel();
        BlockPos spawnPos = findSafeSurface(level, player.blockPosition().relative(player.getDirection(), 2));

        SculkEndermanEntity enderman = createCourier(level, spawnPos, player);
        if (enderman == null) return;

        level.addFreshEntity(enderman);
        playTeleportSound(level, spawnPos);

        for (int i = 0; i < count; i++) {
            Entity reinforcement = mobType.create(level);
            if (reinforcement != null) {
                double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
                reinforcement.setPos(spawnPos.getX() + 0.5 + offsetX, spawnPos.getY(), spawnPos.getZ() + 0.5 + offsetZ);
                level.addFreshEntity(reinforcement);
            }
        }
        GravemindMessagePacket.sendToPlayer(player, name);
        activeReinforcements.add(new ReinforcementData(enderman, 60));
    }

    private static void spawnCourierDropItem(ServerPlayer player, ItemStack stack) {
        ServerLevel level = player.serverLevel();
        BlockPos spawnPos = findSafeSurface(level, player.blockPosition().relative(player.getDirection(), 2));

        SculkEndermanEntity enderman = createCourier(level, spawnPos, player);
        if (enderman == null) return;

        enderman.setItemInHand(InteractionHand.MAIN_HAND, stack);
        level.addFreshEntity(enderman);
        playTeleportSound(level, spawnPos);

        ItemEntity itemEntity = new ItemEntity(level, spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5, stack);
        itemEntity.setDeltaMovement(0, 0, 0);
        itemEntity.setPickUpDelay(10);
        level.addFreshEntity(itemEntity);

        activeReinforcements.add(new ReinforcementData(enderman, 40));
    }

    private static SculkEndermanEntity createCourier(ServerLevel level, BlockPos pos, ServerPlayer target) {
        try {
            SculkEndermanEntity enderman = new SculkEndermanEntity(ModEntities.SCULK_ENDERMAN.get(), level);
            enderman.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            enderman.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());

            enderman.canTeleport = false;

            enderman.setNoAi(false);
            enderman.setSilent(true);
            enderman.setInvulnerable(true);

            if (enderman.getAttribute(Attributes.MOVEMENT_SPEED) != null)
                enderman.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);

            if (enderman.getAttribute(Attributes.ATTACK_DAMAGE) != null)
                enderman.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.0);

            if (enderman.getAttribute(Attributes.FOLLOW_RANGE) != null)
                enderman.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(0.0);

            enderman.setTarget(null);

            enderman.getPersistentData().putInt("GravemindCourier", 100);
            return enderman;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (!activeTeleports.isEmpty()) {
            Iterator<Map.Entry<UUID, TeleportData>> iterator = activeTeleports.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, TeleportData> entry = iterator.next();
                TeleportData data = entry.getValue();

                if (data.player == null || data.player.hasDisconnected() || data.player.isRemoved()) {
                    if (data.enderman != null && data.enderman.isAlive()) data.enderman.discard();
                    iterator.remove();
                    continue;
                }

                if (data.enderman != null && data.enderman.isAlive()) {
                    data.enderman.lookAt(EntityAnchorArgument.Anchor.EYES, data.player.getEyePosition());
                }

                if (data.warmupTicks > 0) {
                    data.warmupTicks--;
                    if (data.warmupTicks == 0) {
                        ServerLevel level = data.player.serverLevel();

                        if (!(level.getBlockState(data.target).getBlock() instanceof SoulAnchorBlock)) {
                            data.player.sendSystemMessage(Component.literal("§cAnchor broken during transit!"));
                            data.player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                            data.player.removeEffect(MobEffects.JUMP);
                            data.player.removeEffect(MobEffects.BLINDNESS);
                            if (data.enderman != null) data.enderman.discard();
                            iterator.remove();
                            continue;
                        }

                        BlockPos safePos = data.target.above();

                        data.player.teleportTo(safePos.getX() + 0.5, safePos.getY(), safePos.getZ() + 0.5);
                        data.player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                        data.player.removeEffect(MobEffects.JUMP);
                        data.player.removeEffect(MobEffects.BLINDNESS);

                        if (data.enderman != null && data.enderman.isAlive()) {
                            data.enderman.discard();
                        }
                        playTeleportSound(level, safePos);

                        GravemindMessagePacket.sendToPlayer(data.player, "Arrival confirmed");
                    }
                } else if (data.postTeleportTicks > 0) {
                    data.postTeleportTicks--;
                    if (data.postTeleportTicks == 0) {
                        iterator.remove();
                    }
                }
            }
        }

        if (!activeReinforcements.isEmpty()) {
            Iterator<ReinforcementData> iter = activeReinforcements.iterator();
            while (iter.hasNext()) {
                ReinforcementData data = iter.next();
                data.ticksLeft--;

                if (data.ticksLeft <= 0) {
                    if (data.courier != null && data.courier.isAlive()) {
                        playTeleportSound((ServerLevel) data.courier.level(), data.courier.blockPosition());
                        data.courier.discard();
                    }
                    iter.remove();
                }
            }
        }
    }

    private static BlockPos findSafeSurface(ServerLevel level, BlockPos target) {
        LevelChunk chunk = level.getChunkSource().getChunk(target.getX() >> 4, target.getZ() >> 4, true);
        if (chunk == null) return target;
        BlockPos.MutableBlockPos cursor = target.mutable();
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        if (cursor.getY() < minY + 2) cursor.setY(minY + 2);
        if (cursor.getY() > maxY - 2) cursor.setY(maxY - 2);

        if (!level.isEmptyBlock(cursor)) {
            for(int i=0; i<20; i++) {
                cursor.move(0, 1, 0);
                if (level.isEmptyBlock(cursor) && level.isEmptyBlock(cursor.above())) return cursor.immutable();
            }
        } else {
            while (cursor.getY() > minY) {
                BlockPos below = cursor.below();
                if (!level.isEmptyBlock(below)) return cursor.immutable();
                cursor.move(0, -1, 0);
            }
        }
        return target.above();
    }

    private static void playTeleportSound(ServerLevel level, BlockPos pos) {
        ResourceLocation soundLoc = ResourceLocation.fromNamespaceAndPath("sculkhorde", "sculk_enderman_portal");
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(soundLoc);
        if (sound != null) {
            level.playSound(null, pos, sound, SoundSource.HOSTILE, 1.0f, 1.0f);
        } else {
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 1.0f);
        }
    }

    private static class TeleportData {
        ServerPlayer player;
        SculkEndermanEntity enderman;
        BlockPos target;
        int warmupTicks;
        int postTeleportTicks;

        public TeleportData(ServerPlayer player, SculkEndermanEntity enderman, BlockPos target, int warmup, int post) {
            this.player = player;
            this.enderman = enderman;
            this.target = target;
            this.warmupTicks = warmup;
            this.postTeleportTicks = post;
        }
    }

    private static class ReinforcementData {
        LivingEntity courier;
        int ticksLeft;

        public ReinforcementData(LivingEntity courier, int ticksLeft) {
            this.courier = courier;
            this.ticksLeft = ticksLeft;
        }
    }
}
