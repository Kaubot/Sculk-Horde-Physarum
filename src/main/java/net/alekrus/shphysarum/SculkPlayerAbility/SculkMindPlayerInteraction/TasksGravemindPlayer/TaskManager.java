package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMind;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class TaskManager {

    public static GravemindTask clientCurrentTask = null;
    private static List<GravemindTask> availableOptions = new ArrayList<>();
    private static final Random random = new Random();

    public static GravemindTask getCurrentTask() {
        if (net.minecraft.client.Minecraft.getInstance().level != null && net.minecraft.client.Minecraft.getInstance().level.isClientSide) {
            return clientCurrentTask;
        }
        return null;
    }

    public static GravemindTask getCurrentTask(Player player) {
        if (player.level().isClientSide) return clientCurrentTask;
        AtomicReference<GravemindTask> task = new AtomicReference<>(null);
        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            if (cap instanceof SculkMind sm) {
                task.set(GravemindTask.load(sm.getActiveTaskNBT()));
            }
        });
        return task.get();
    }

    public static List<GravemindTask> getAvailableOptions(Player player) {
        if (availableOptions.isEmpty()) generateOptions(player);
        return availableOptions;
    }

    public static void forceSetTask(Player player, GravemindTask task) {
        if (player == null) return;
        if (player.level().isClientSide) {
            clientCurrentTask = task;
        } else {
            player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                if (cap instanceof SculkMind sm) {
                    CompoundTag tag = (task == null) ? new CompoundTag() : task.save();
                    sm.setActiveTaskNBT(tag);
                    if (player instanceof ServerPlayer sp) {
                        PacketHandler.CHANNEL.sendTo(
                                new SkillSyncPacket(
                                        cap.getUnlockedSkills(),
                                        cap.getEvoPoints(),
                                        cap.getFaith(),
                                        tag,
                                        cap.areTentaclesActive(),
                                        cap.getUserFollowerLimit(),
                                        cap.getAllowedFollowerTypes(),
                                        cap.getKnownAnchors(),
                                        cap.getActiveAbilitiesSet()
                                ),
                                sp.connection.connection,
                                NetworkDirection.PLAY_TO_CLIENT
                        );
                    }
                }
            });
        }
    }

    public static void resetTask(Player player) {
        forceSetTask(player, null);
        generateOptions(player);
    }

    public static void generateOptions(Player player) {
        availableOptions.clear();
        availableOptions.add(new GravemindTask(GravemindTask.Type.KILL_VILLAGERS, 3 + random.nextInt(5), 2, 1));
        availableOptions.add(new GravemindTask(GravemindTask.Type.KILL_PILLAGERS, 3 + random.nextInt(5), 3, 2));
        availableOptions.add(new GravemindTask(GravemindTask.Type.KILL_WARDEN, 1, 10, 5));
        availableOptions.add(new GravemindTask(GravemindTask.Type.FIND_NEW_VILLAGE, 1, 6, 2));

        GravemindTask pearlTask = new GravemindTask(GravemindTask.Type.GIVE_ITEMS, 16, 5, 4);
        pearlTask.setRequiredItem(new ItemStack(Items.ENDER_PEARL));
        Item cleaver = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("sculkhorde:sculk_enderman_cleaver"));
        if (cleaver != null) pearlTask.setRewardItem(new ItemStack(cleaver));
        availableOptions.add(pearlTask);

        GravemindTask soulTask = new GravemindTask(GravemindTask.Type.GIVE_ITEMS, 32, 4, 3);
        Item cryingSoul = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("sculkhorde:crying_souls"));
        if (cryingSoul != null) soulTask.setRequiredItem(new ItemStack(cryingSoul));
        else soulTask.setRequiredItem(new ItemStack(Items.GOLD_INGOT));
        availableOptions.add(soulTask);

        GravemindTask starTask = new GravemindTask(GravemindTask.Type.OFFER_NETHER_STAR, 1, 10, 10);
        starTask.setRequiredItem(new ItemStack(Items.NETHER_STAR));
        Item sculkBeacon = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("shphysarum:sculk_beacon"));
        if (sculkBeacon != null) starTask.setRewardItem(new ItemStack(sculkBeacon));
        else starTask.setRewardItem(new ItemStack(Items.DIAMOND_BLOCK));
        availableOptions.add(starTask);

        GravemindTask xpTask = new GravemindTask(GravemindTask.Type.SACRIFICE_LEVELS, 30, 0, 8);
        availableOptions.add(xpTask);

        if (player.level() != null) {
            BlockPos cleanSpot = findCleanSpot(player.level(), player.blockPosition(), 150);
            if (cleanSpot != null) {
                GravemindTask infectTask = new GravemindTask(GravemindTask.Type.INFECT_AREA, 50, 5, 4);
                infectTask.targetLocation = cleanSpot;
                infectTask.locationName = cleanSpot.getX() + ", " + cleanSpot.getY() + ", " + cleanSpot.getZ();
                availableOptions.add(infectTask);
            }
        }
    }

    public static void handleLevelSacrifice(Player player) {
        GravemindTask current = getCurrentTask(player);
        if (current == null || current.isComplete()) return;
        if (current.type != GravemindTask.Type.SACRIFICE_LEVELS) return;

        int requiredLevels = current.requiredAmount;

        if (player.experienceLevel >= requiredLevels) {
            player.giveExperienceLevels(-requiredLevels);

            current.currentAmount = requiredLevels;

            if (player instanceof ServerPlayer serverPlayer) {
                GravemindMessagePacket.sendToPlayer(serverPlayer, "Energy absorbed. Complexity grows.");
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 1.0f, 1.0f);

            forceSetTask(player, current);
        } else {
            player.displayClientMessage(Component.literal("§cRequires " + requiredLevels + " Levels of Experience."), true);
        }
    }

    private static BlockPos findCleanSpot(Level level, BlockPos center, int range) {
        for (int i = 0; i < 20; i++) {
            int x = center.getX() + random.nextInt(range * 2) - range;
            int z = center.getZ() + random.nextInt(range * 2) - range;
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos pos = new BlockPos(x, y - 1, z);
            if (!level.getBlockState(pos).isAir() && !isSculkBlock(level.getBlockState(pos))) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isSculkBlock(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());

        
        if (id != null && id.toString().equals("sculkhorde:infestation_ward_block")) {
            return false; 
        }
        

        if (state.is(Blocks.SCULK) || state.is(Blocks.SCULK_VEIN) ||
                state.is(Blocks.SCULK_CATALYST) || state.is(Blocks.SCULK_SHRIEKER) ||
                state.is(Blocks.SCULK_SENSOR)) {
            return true;
        }

        if (id != null) {
            String ns = id.getNamespace();
            String path = id.getPath();
            if (ns.equals("sculkhorde") || path.contains("sculk")) {
                return true;
            }
        }
        return false;
    }

    public static void onMobKill(Entity victim, Player killer) {
        GravemindTask current = getCurrentTask(killer);
        if (current == null || current.isComplete()) return;
        boolean changed = false;
        if (current.type == GravemindTask.Type.KILL_VILLAGERS && victim instanceof AbstractVillager) { current.currentAmount++; changed = true; }
        else if (current.type == GravemindTask.Type.KILL_PILLAGERS && victim instanceof Pillager) { current.currentAmount++; changed = true; }
        else if (current.type == GravemindTask.Type.KILL_WARDEN && victim instanceof Warden) { current.currentAmount++; changed = true; }
        if (changed) forceSetTask(killer, current);
    }

    public static void checkInfectionProgress(Player player) {
        GravemindTask current = getCurrentTask(player);
        if (current == null || current.isComplete() || current.type != GravemindTask.Type.INFECT_AREA) return;

        BlockPos center = current.targetLocation;
        if (center == null || center.equals(BlockPos.ZERO)) return;

        double distSqr = player.distanceToSqr(center.getX(), center.getY(), center.getZ());

        if (distSqr < 3600) {
            int radiusXZ = 20;
            int radiusY = 10;
            int count = 0;
            Level level = player.level();

            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radiusXZ, -radiusY, -radiusXZ), center.offset(radiusXZ, radiusY, radiusXZ))) {
                if (isSculkBlock(level.getBlockState(pos))) {
                    count++;
                }
            }

            if (count != current.currentAmount) {
                current.currentAmount = count;
                forceSetTask(player, current);
            }
        } else {
            if (player.level().getGameTime() % 100 == 0) {
                player.displayClientMessage(Component.literal("Target too far! Go to: " + center.getX() + ", " + center.getY() + ", " + center.getZ()), true);
            }
        }
    }

    public static void handleItemTurnIn(Player player, ItemStack stack) {
        GravemindTask current = getCurrentTask(player);
        if (current == null || current.isComplete()) return;
        if (current.type != GravemindTask.Type.GIVE_ITEMS && current.type != GravemindTask.Type.OFFER_NETHER_STAR) return;

        if (stack.getItem() == current.requiredItem.getItem()) {
            int amount = stack.getCount();
            int needed = current.requiredAmount - current.currentAmount;
            int take = Math.min(amount, needed);
            if (take > 0) {
                stack.shrink(take);
                current.currentAmount += take;

                if (player instanceof ServerPlayer serverPlayer) {
                    GravemindMessagePacket.sendToPlayer(serverPlayer, "Sacrifice accepted.");
                }

                forceSetTask(player, current);
            }
        }
    }

    public static void completeVillageTask(Player player) {
        GravemindTask current = getCurrentTask(player);
        if (current == null || current.isComplete() || current.type != GravemindTask.Type.FIND_NEW_VILLAGE) return;
        if (current.currentAmount < current.requiredAmount) {
            current.currentAmount = current.requiredAmount;

            if (player instanceof ServerPlayer serverPlayer) {
                GravemindMessagePacket.sendToPlayer(serverPlayer, "New feeding grounds located.");
            }

            forceSetTask(player, current);
        }
    }

    public static void checkScouting(Player player) {}
}
