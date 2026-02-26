package net.alekrus.shphysarum.SculkPlayerAbility.PlayerBurrowInSculk;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class SculkBurrowHandler {

    private static final String TAG_BURROW = "shphysarum_is_burrowing";
    private static final String TAG_GRACE_TIMER = "shphysarum_burrow_grace";

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getPersistentData().getBoolean(TAG_BURROW)) {
                disableBurrow(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.getPersistentData().getBoolean(TAG_BURROW)) {
                disableBurrow(player);
            }
        }
    }

    public static void toggle(ServerPlayer player) {
        boolean isActive = player.getPersistentData().getBoolean(TAG_BURROW);

        if (isActive) {
            disableBurrow(player);
        } else {
            if (!player.isCreative() && player.totalExperience <= 0) {
                player.displayClientMessage(Component.literal("§cNot enough XP."), true);
                return;
            }
            if (isTouchingSculkAnywhere(player)) {
                enableBurrow(player);
            } else {
                player.displayClientMessage(Component.literal("§3Must be on solid Sculk to burrow."), true);
            }
        }
    }

    private static void enableBurrow(ServerPlayer player) {
        player.getPersistentData().putBoolean(TAG_BURROW, true);
        player.getPersistentData().putInt(TAG_GRACE_TIMER, 5);
        player.setForcedPose(Pose.SWIMMING);
        sendSyncPacket(player, true);

        
        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            cap.addActiveAbility("burrow");
            syncSculkMind(player, cap);
        });

        try {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.PLAYERS, 1.0F, 1.0F);
        } catch (Exception ignored) {}
    }

    private static void disableBurrow(ServerPlayer player) {
        player.getPersistentData().putBoolean(TAG_BURROW, false);
        player.getPersistentData().putInt(TAG_GRACE_TIMER, 0);
        player.setForcedPose(null);
        player.removeEffect(MobEffects.INVISIBILITY);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        sendSyncPacket(player, false);

        
        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            cap.removeActiveAbility("burrow");
            syncSculkMind(player, cap);
        });

        if (player.connection != null) {

        }

        try {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SCULK_SHRIEKER_BREAK, SoundSource.PLAYERS, 0.5F, 0.8F);
        } catch (Exception ignored) {}
    }

    private static void sendSyncPacket(ServerPlayer player, boolean active) {
        if (player.connection == null) return;
        PacketHandler.CHANNEL.sendTo(new SculkBurrowSyncPacket(active),
                player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static void syncSculkMind(ServerPlayer player, net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.ISculkMind cap) {
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
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity().getPersistentData().getBoolean(TAG_BURROW)) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity().getPersistentData().getBoolean(TAG_BURROW)) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity().getPersistentData().getBoolean(TAG_BURROW)) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity().getPersistentData().getBoolean(TAG_BURROW)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        if (!player.getPersistentData().getBoolean(TAG_BURROW)) return;

        if (!player.isCreative()) {
            if (player.totalExperience > 0) {
                player.giveExperiencePoints(-5);
            } else {

                disableBurrow(player);
                return;
            }
        }

        boolean isOnSculk = isFootprintOnSculk(player);
        boolean isClimbing = false;
        if (player.horizontalCollision) {
            if (isFacingSculkWall(player)) isClimbing = true;
            else if (!isOnSculk) { disableBurrow(player); return; }
        }

        if (!isOnSculk && !isClimbing) {
            if (isStandingOnNonSculkSolidBlock(player)) {
                disableBurrow(player);
                return;
            }
        }

        int graceTimer = player.getPersistentData().getInt(TAG_GRACE_TIMER);
        if (isOnSculk || isClimbing) {
            player.getPersistentData().putInt(TAG_GRACE_TIMER, 3);
        } else {
            if (graceTimer > 0) player.getPersistentData().putInt(TAG_GRACE_TIMER, graceTimer - 1);
            else { disableBurrow(player); return; }
        }

        if (player.getPose() != Pose.SWIMMING) player.setForcedPose(Pose.SWIMMING);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, 9, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, 0, false, false, false));

        double dx = player.getX() - player.xo;
        double dz = player.getZ() - player.zo;
        if (dx * dx + dz * dz > 0.0001) {
            try {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SCULK_BLOCK_SPREAD,
                        SoundSource.PLAYERS,
                        0.15F,
                        1.0F + (player.getRandom().nextFloat() * 0.2F)
                );
            } catch (Exception ignored) {}
        }

        if (player.tickCount % 2 == 0) {
            BlockState state = Blocks.SCULK.defaultBlockState();
            AABB box = player.getBoundingBox();
            for (int i = 0; i < 5; i++) {
                ((ServerPlayer) player).serverLevel().sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        box.minX + (Math.random() * (box.maxX - box.minX)),
                        player.getY() + 0.1,
                        box.minZ + (Math.random() * (box.maxZ - box.minZ)),
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
    }

    public static boolean isTouchingSculkAnywhere(ServerPlayer player) {
        return isFootprintOnSculk(player) || isFacingSculkWall(player);
    }

    private static boolean isIgnoredVegetation(BlockState state) {
        if (state.isAir()) return true;
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id == null) return false;
        String path = id.toString();
        
        
        return path.equals("sculkhorde:spike") ||
                path.equals("sculkhorde:small_shroom") ||
                path.equals("sculkhorde:grass") ||
                path.equals("sculkhorde:sculk_shroom_culture") ||
                path.equals("minecraft:sculk_vein");
    }

    private static boolean isFootprintOnSculk(ServerPlayer player) {
        AABB box = player.getBoundingBox();
        int x1 = (int) Math.floor(box.minX);
        int x2 = (int) Math.floor(box.maxX);
        int z1 = (int) Math.floor(box.minZ);
        int z2 = (int) Math.floor(box.maxZ);

        int y1 = (int) Math.floor(box.minY - 1.0);
        int y2 = (int) Math.floor(box.minY + 0.5);

        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                for (int y = y1; y <= y2; y++) {
                    if (isSculkBlock(player.level().getBlockState(new BlockPos(x, y, z)))) return true;
                }
            }
        }
        return false;
    }

    private static boolean isStandingOnNonSculkSolidBlock(ServerPlayer player) {
        AABB box = player.getBoundingBox();
        int x1 = (int) Math.floor(box.minX);
        int x2 = (int) Math.floor(box.maxX);
        int z1 = (int) Math.floor(box.minZ);
        int z2 = (int) Math.floor(box.maxZ);

        int y = (int) Math.floor(box.minY - 0.2);

        boolean foundSolid = false;
        boolean foundSculk = false;

        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = player.level().getBlockState(pos);
                if (!state.isAir() && !isIgnoredVegetation(state)) {
                    foundSolid = true;
                    if (isSculkBlock(state)) foundSculk = true;
                }
            }
        }
        return foundSolid && !foundSculk;
    }

    private static boolean isFacingSculkWall(ServerPlayer player) {
        double lookX = player.getLookAngle().x;
        double lookZ = player.getLookAngle().z;
        BlockPos pos = player.blockPosition();
        if (Math.abs(lookX) > Math.abs(lookZ)) {
            BlockPos wallPos = pos.offset(lookX > 0 ? 1 : -1, 0, 0);
            return isSculkBlock(player.level().getBlockState(wallPos));
        } else {
            BlockPos wallPos = pos.offset(0, 0, lookZ > 0 ? 1 : -1);
            return isSculkBlock(player.level().getBlockState(wallPos));
        }
    }

    private static boolean isSculkBlock(BlockState state) {
        
        
        
        return state.is(Blocks.SCULK) ||
                state.is(Blocks.SCULK_CATALYST) ||
                state.is(Blocks.SCULK_SENSOR) ||
                state.is(Blocks.SCULK_SHRIEKER);
    }
}
