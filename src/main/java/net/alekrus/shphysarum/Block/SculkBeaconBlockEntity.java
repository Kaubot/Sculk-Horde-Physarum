package net.alekrus.shphysarum.Block;

import net.alekrus.shphysarum.Config.ModClientConfig;
import net.alekrus.shphysarum.Config.RaidMusicDisplayOverlay;
import net.alekrus.shphysarum.Entities.PureWitch.ModEntities;
import net.alekrus.shphysarum.ModSounds.RaidMusicInstance;
import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;


import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;


import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class SculkBeaconBlockEntity extends BlockEntity implements GeoBlockEntity { 

    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    
    private boolean hasPlayedPlace = false;
    private int ageTicks = 0;

    private boolean isRaidActive = false;
    private int currentWave = 0;
    private final int TOTAL_WAVES = 6;
    private UUID initiatiorUUID;
    private int outOfBoundsTimer = 0;
    private int raidTicker = 0;
    private int waveDelayTimer = 0;
    private List<UUID> raidMobs = new ArrayList<>();

    private final ServerBossEvent raidBossBar = (ServerBossEvent) new ServerBossEvent(
            Component.literal("Biomass Raid"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_6
    ).setDarkenScreen(true);

    private Object clientMusicInstance = null;

    public SculkBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(net.alekrus.shphysarum.Block.ModBlocks.SCULK_BEACON_BE.get(), pos, state);
    }

    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "beacon_controller", 3, event -> {

            
            if (this.isRaidActive) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("work"));
            }

            
            if (!this.hasPlayedPlace) {
                return event.setAndContinue(RawAnimation.begin().thenPlay("place").thenLoop("idle"));
            }

            
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public boolean isActive() { return isRaidActive; }
    public boolean isRaidActive() { return isRaidActive; }
    public UUID getInitiatorUUID() { return initiatiorUUID; }

    
    public void startRaid(Player player) {
        
        if (level.dimension() != Level.OVERWORLD) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("§cSignal failed. The Sculk Beacon connection works only in the Overworld."));
                level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
            return;
        }

        if (!level.canSeeSky(worldPosition.above())) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("§cSky obstructed! The Beacon requires a clear view of the sky."));
                level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
            return;
        }

        if (!checkPlatformIntegrity(level, worldPosition)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("§cInvalid Terrain! You need a solid 40x40 platform beneath the beacon."));
                level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
            return;
        }

        this.isRaidActive = true;
        this.currentWave = 0;
        this.initiatiorUUID = player.getUUID();
        this.outOfBoundsTimer = 0;
        this.raidMobs.clear();
        this.raidTicker = 0;
        this.waveDelayTimer = 0;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            raidBossBar.addPlayer(serverPlayer);
            raidBossBar.setName(Component.literal("§cStarting Charging..."));
            GravemindMessagePacket.sendToPlayer(serverPlayer, "Looks like they were getting ready for that");
            raidBossBar.setProgress(0f);
        }
    }

    
    private boolean checkPlatformIntegrity(Level level, BlockPos center) {
        int radius = 20;
        int yCheck = center.getY() - 1;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = new BlockPos(center.getX() + x, yCheck, center.getZ() + z);
                BlockState state = level.getBlockState(checkPos);
                if (state.isAir() || state.getCollisionShape(level, checkPos).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SculkBeaconBlockEntity entity) {
        if (level.isClientSide) {
            handleClientMusic(entity);
            if (entity.isRaidActive) {
                spawnRaidParticles(level, pos);
            }
            return;
        }

        
        
        
        
        if (!entity.hasPlayedPlace) {
            entity.ageTicks++;
            if (entity.ageTicks > 40) {
                entity.hasPlayedPlace = true;
                entity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        
        if (!entity.isRaidActive) return;

        entity.raidTicker++;
        Player player = level.getPlayerByUUID(entity.initiatiorUUID);

        if (player == null) {
            entity.failRaid("Defender lost connection to the Hive Mind.");
            return;
        }

        if (!player.isAlive()) {
            entity.failRaid("The Gravemind senses your demise.");
            return;
        }

        if (entity.raidTicker % 5 == 0 && !entity.raidMobs.isEmpty()) {
            ServerLevel sLevel = (ServerLevel) level;
            double maxDistanceSq = 400.0;

            for (UUID uuid : entity.raidMobs) {
                Entity e = sLevel.getEntity(uuid);
                if (e instanceof Mob mob) {
                    double distSq = mob.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                    if (distSq > maxDistanceSq) {
                        Vec3 toCenter = new Vec3(pos.getX() - mob.getX(), pos.getY() - mob.getY(), pos.getZ() - mob.getZ()).normalize();
                        mob.setDeltaMovement(toCenter.scale(0.5));
                        mob.getNavigation().stop();
                        if (mob.getTarget() != null && mob.getTarget().distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > maxDistanceSq) {
                            mob.setTarget(null);
                        }
                    }
                }
            }
        }

        double distSq = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
        if (distSq > 900) {
            entity.outOfBoundsTimer++;
            int secondsLeft = 10 - (entity.outOfBoundsTimer / 20);
            entity.raidBossBar.setName(Component.literal("§cRETURN TO ZONE! " + secondsLeft + "s"));

            if (entity.outOfBoundsTimer == 180) {
                if (player instanceof ServerPlayer serverPlayer) {
                    GravemindMessagePacket.sendToPlayer(serverPlayer, "Return to the beacon, it won't work without you.");
                }
            }

            entity.raidBossBar.setColor(BossEvent.BossBarColor.YELLOW);

            if (entity.outOfBoundsTimer % 20 == 0) level.playSound(null, pos, SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.MASTER, 1f, 2f);

            if (entity.outOfBoundsTimer > 200) {
                entity.failRaid("Zone abandoned. Raid cancelled.");
                return;
            }
        } else {
            if (entity.outOfBoundsTimer > 0) {
                entity.outOfBoundsTimer = 0;
                entity.raidBossBar.setColor(BossEvent.BossBarColor.RED);
                entity.raidBossBar.setName(Component.literal("§4Raid - Wave " + entity.currentWave));
            }
        }

        if (entity.currentWave > 0 && !entity.raidMobs.isEmpty()) {
            float progress = (float) entity.raidMobs.size() / (float) getMobCountForWave(entity.currentWave);
            entity.raidBossBar.setProgress(progress);
        } else if (entity.waveDelayTimer > 0) {
            entity.raidBossBar.setName(Component.literal("§eNext wave in " + (5 - entity.waveDelayTimer/20) + "s"));
            entity.raidBossBar.setProgress(0f);
        }

        if (entity.raidTicker % 20 == 0) {
            entity.raidMobs.removeIf(uuid -> { Entity e = ((ServerLevel) level).getEntity(uuid); return e == null || !e.isAlive(); });
        }

        if (entity.raidMobs.isEmpty()) {
            entity.waveDelayTimer++;
            if (entity.waveDelayTimer >= 100) {
                entity.waveDelayTimer = 0;
                entity.currentWave++;
                if (entity.currentWave > entity.TOTAL_WAVES) entity.victoryRaid();
                else entity.spawnWave(entity.currentWave, pos, (ServerLevel) level, player);
            }
        }
    }

    
    private static void handleClientMusic(SculkBeaconBlockEntity entity) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && entity.initiatiorUUID != null && localPlayer.getUUID().equals(entity.initiatiorUUID)) {

            if (entity.isRaidActive && ModClientConfig.PLAY_RAID_BEACON_MUSIC.get()) {
                if (entity.clientMusicInstance == null || !Minecraft.getInstance().getSoundManager().isActive((RaidMusicInstance)entity.clientMusicInstance)) {
                    RaidMusicInstance music = new RaidMusicInstance(entity);
                    Minecraft.getInstance().getSoundManager().play(music);
                    entity.clientMusicInstance = music;

                    RaidMusicDisplayOverlay.triggerDisplay();
                }
            } else {
                if (entity.clientMusicInstance != null) {
                    Minecraft.getInstance().getSoundManager().stop((RaidMusicInstance)entity.clientMusicInstance);
                    entity.clientMusicInstance = null;
                }
            }
        }
    }

    private static void spawnRaidParticles(Level level, BlockPos pos) {
        if (level.random.nextFloat() < 0.3f) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
            level.addParticle(ParticleTypes.SCULK_SOUL, x, pos.getY() + 1, z, 0, 0.1, 0);
        }
    }

    private static int getMobCountForWave(int wave) {
        return 12 + (wave * 6);
    }

    private void spawnWave(int wave, BlockPos center, ServerLevel sLevel, Player target) {
        raidBossBar.setName(Component.literal("§4§lRaid - Wave " + wave + " / " + TOTAL_WAVES));
        raidBossBar.setProgress(1.0f);
        sLevel.playSound(null, center, SoundEvents.RAID_HORN.get(), SoundSource.HOSTILE, 50.0f, 1.0f);

        int count = getMobCountForWave(wave);

        Scoreboard scoreboard = sLevel.getScoreboard();
        PlayerTeam raidTeam = scoreboard.getPlayerTeam("SculkRaidYellow");
        if (raidTeam == null) {
            raidTeam = scoreboard.addPlayerTeam("SculkRaidYellow");
            raidTeam.setColor(ChatFormatting.YELLOW);
        }

        for (int i = 0; i < count; i++) {
            EntityType<?> typeToSpawn = calculateEnemyType(wave, sLevel.random);

            double angle = sLevel.random.nextDouble() * Math.PI * 2;
            double dist = 18 + sLevel.random.nextDouble() * 2;
            double x = center.getX() + Math.cos(angle) * dist;
            double z = center.getZ() + Math.sin(angle) * dist;
            int y = sLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)x, (int)z);

            Entity entity = typeToSpawn.create(sLevel);
            if (entity instanceof Mob mob) {
                mob.setPos(x, y + 1, z);
                mob.setTarget(target);
                mob.setPersistenceRequired();

                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
                scoreboard.addPlayerToTeam(mob.getScoreboardName(), raidTeam);
                mob.restrictTo(center, 20);

                Entity portal = ModEntities.SCULK_PORTAL.get().create(sLevel);
                if (portal != null) {
                    portal.setPos(x, y + 1.0, z);
                    float rot = (float) (Math.atan2(target.getZ() - z, target.getX() - x) * (180F / Math.PI)) - 180.0F;
                    portal.setYRot(rot);
                    portal.setXRot(0.0F);
                    if (portal instanceof Mob mobPortal) {
                        mobPortal.yBodyRot = rot;
                        mobPortal.yBodyRotO = rot;
                        mobPortal.yHeadRot = rot;
                        mobPortal.yHeadRotO = rot;
                    }
                    mob.setYRot(rot);
                    mob.yBodyRot = rot;
                    mob.yHeadRot = rot;

                    sLevel.addFreshEntity(portal);
                }

                sLevel.sendParticles(ParticleTypes.SCULK_SOUL, x, y + 1.5, z, 30, 0.5, 1.0, 0.5, 0.05);

                mob.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 1, false, false));
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 255, false, false));
                mob.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 250, false, false));
                mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 255, false, false));
                mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 255, false, false));

                if (mob instanceof Raider raider) {
                    raider.setCanJoinRaid(true);
                    raider.setAggressive(true);
                    CompoundTag nbt = new CompoundTag();
                    raider.addAdditionalSaveData(nbt);
                    nbt.putString("DeathLootTable", BuiltInLootTables.EMPTY.toString());
                    raider.readAdditionalSaveData(nbt);
                    if (sLevel.random.nextFloat() < 0.15f) {
                        ItemStack banner = Raid.getLeaderBannerInstance();
                        raider.setItemSlot(EquipmentSlot.HEAD, banner);
                        raider.setDropChance(EquipmentSlot.HEAD, 0.0f);
                    }
                }

                if (mob instanceof Pillager pillager && pillager.getMainHandItem().isEmpty()) {
                    pillager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
                }
                if (mob instanceof Vindicator vindicator && vindicator.getMainHandItem().isEmpty()) {
                    vindicator.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                }

                sLevel.addFreshEntity(mob);
                this.raidMobs.add(mob.getUUID());
            }
        }
    }

    private EntityType<?> calculateEnemyType(int wave, net.minecraft.util.RandomSource random) {
        double r = random.nextDouble();

        if (wave == 1) return (r > 0.3) ? EntityType.VINDICATOR : EntityType.PILLAGER;
        else if (wave == 2) return (r > 0.7) ? EntityType.VINDICATOR : EntityType.PILLAGER;
        else if (wave <= 4) {
            if (r > 0.90) return EntityType.RAVAGER;
            else if (r > 0.70) return ModEntities.SCULK_WITCH.get();
            else if (r > 0.50) return EntityType.VINDICATOR;
            else return EntityType.PILLAGER;
        } else {
            if (r > 0.97) return EntityType.ILLUSIONER;
            else if (r > 0.87) return EntityType.EVOKER;
            else if (r > 0.75) return EntityType.RAVAGER;
            else if (r > 0.60) return ModEntities.SCULK_WITCH.get();
            else if (r > 0.35) return EntityType.VINDICATOR;
            else return EntityType.PILLAGER;
        }
    }

    private void failRaid(String reason) {
        this.isRaidActive = false;
        raidBossBar.removeAllPlayers();

        if (level instanceof ServerLevel sLevel) {
            for (UUID uuid : raidMobs) {
                Entity e = sLevel.getEntity(uuid);
                if (e != null && e.isAlive()) {

                    sLevel.sendParticles(ParticleTypes.SCULK_SOUL, e.getX(), e.getY() + 1, e.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
                    e.discard();
                }
            }
        }
        this.raidMobs.clear();

        level.playSound(null, worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1f, 0.5f);

        if (initiatiorUUID != null) {
            Player player = level.getPlayerByUUID(initiatiorUUID);
            if (player != null) {
                player.sendSystemMessage(Component.literal("§cRAID FAILED: " + reason));
            }
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private void victoryRaid() {
        this.isRaidActive = false;
        raidBossBar.removeAllPlayers();

        if (initiatiorUUID != null) {
            Player player = level.getPlayerByUUID(initiatiorUUID);
            if (player != null) {
                if (player instanceof ServerPlayer serverPlayer) {
                    GravemindMessagePacket.sendToPlayer(serverPlayer, "They're backing down");
                    GravemindMessagePacket.sendToPlayer(serverPlayer, "You did better than I expected.");
                }

                level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1f, 1f);
            }
        }

        ItemStack reward = new ItemStack(ModItems.RICH_BIOMASS_BARK.get(), 1);
        ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, reward);
        item.setDeltaMovement(0, 0.3, 0);
        item.setNoPickUpDelay();
        level.addFreshEntity(item);

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public void setRemoved() { super.setRemoved(); raidBossBar.removeAllPlayers(); }

    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsActive", isRaidActive);
        tag.putInt("Wave", currentWave);
        if (initiatiorUUID != null) tag.putUUID("Owner", initiatiorUUID);
        tag.putBoolean("HasPlayedPlace", hasPlayedPlace);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isRaidActive = tag.getBoolean("IsActive");
        currentWave = tag.getInt("Wave");
        if (tag.hasUUID("Owner")) initiatiorUUID = tag.getUUID("Owner");
        hasPlayedPlace = tag.getBoolean("HasPlayedPlace");
    }

    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) { load(pkt.getTag()); }
    @Override public AABB getRenderBoundingBox() { return INFINITE_EXTENT_AABB; }
}
