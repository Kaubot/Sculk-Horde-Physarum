package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.FaithSystem.FaithHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class TaskActionPacket {

    private final int action;
    private final int taskTypeOrdinal;
    private final int required;
    private final int reward;
    private final int evoReward;
    private final BlockPos target;

    public TaskActionPacket(int action, int rewardAmount) {
        this(action, 0, 0, rewardAmount, 0, BlockPos.ZERO);
    }

    public TaskActionPacket(int action, int type, int req, int rew, int evoRew, BlockPos pos) {
        this.action = action;
        this.taskTypeOrdinal = type;
        this.required = req;
        this.reward = rew;
        this.evoReward = evoRew;
        this.target = pos;
    }

    public static TaskActionPacket decode(FriendlyByteBuf buf) {
        return new TaskActionPacket(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBlockPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(action);
        buf.writeInt(taskTypeOrdinal);
        buf.writeInt(required);
        buf.writeInt(reward);
        buf.writeInt(evoReward);
        buf.writeBlockPos(target);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                if (action == 0) {
                    if (reward > 0) FaithHandler.addFaith(player, reward);

                    if (evoReward > 0) {
                        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                            cap.addEvoPoints(evoReward);
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
                            player.sendSystemMessage(Component.literal("§a+" + evoReward + " Biomass (Total: " + cap.getEvoPoints() + ")"));
                        });
                    }

                    boolean isEnderQuest = (taskTypeOrdinal == 4 && required == 16);
                    if (isEnderQuest) {
                        Item cleaver = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("sculkhorde:sculk_enderman_cleaver"));
                        if (cleaver == null) cleaver = Items.NETHERITE_SWORD;
                        spawnCourierNoAI(player.serverLevel(), player, new ItemStack(cleaver));
                    }
                    else if (taskTypeOrdinal == GravemindTask.Type.OFFER_NETHER_STAR.ordinal()) {
                        Item beacon = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse("shphysarum:sculk_beacon"));
                        if (beacon == null) beacon = Items.BEACON;
                        spawnCourierNoAI(player.serverLevel(), player, new ItemStack(beacon));
                    }
                    else {
                        
                        GravemindMessagePacket.sendToPlayer(player, "Reward assimilated.");
                    }

                    TaskManager.resetTask(player);
                }
                else if (action == 2) {
                    GravemindTask.Type t = GravemindTask.Type.values()[taskTypeOrdinal];
                    GravemindTask task = new GravemindTask(t, required, reward, evoReward);
                    if (!target.equals(BlockPos.ZERO)) task.targetLocation = target;
                    TaskManager.generateOptions(player);
                    for(GravemindTask opt : TaskManager.getAvailableOptions(player)) {
                        if(opt.type == t && opt.requiredAmount == required) {
                            task.requiredItem = opt.requiredItem;
                            task.rewardItem = opt.rewardItem;
                            break;
                        }
                    }
                    TaskManager.forceSetTask(player, task);
                }
                else if (action == 3) {
                    player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                        int currentEvo = cap.getEvoPoints();
                        cap.setEvoPoints(Math.max(0, currentEvo - 3));
                        int currentFaith = cap.getFaith();
                        cap.setFaith(Math.max(0, currentFaith - 4));
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
                    TaskManager.resetTask(player);

                    
                    GravemindMessagePacket.sendToPlayer(player, "Directive abandoned. Was it really that difficult?");
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    private void spawnCourierNoAI(ServerLevel level, ServerPlayer player, ItemStack reward) {
        try {
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse("sculkhorde:sculk_enderman"));
            if (entityType == null) entityType = EntityType.ENDERMAN;

            double x = player.getX() + (player.getLookAngle().x * 3.0);
            double z = player.getZ() + (player.getLookAngle().z * 3.0);
            double y = player.getY();

            Entity entity = entityType.create(level);
            if (entity != null) {
                entity.setPos(x, y, z);
                if (entity instanceof Mob mob) {
                    mob.setNoAi(true);
                    mob.setPersistenceRequired();
                }
                entity.setInvulnerable(true);
                entity.setSilent(true);
                entity.getPersistentData().putInt("GravemindCourier", 60);
                level.addFreshEntity(entity);

                SoundEvent teleportSound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.tryParse("sculkhorde:sculk_enderman_portal"));
                if (teleportSound == null) teleportSound = SoundEvents.ENDERMAN_TELEPORT;

                level.playSound(null, x, y, z, teleportSound, SoundSource.HOSTILE, 1.0f, 1.0f);
            }

            ItemEntity itemEntity = new ItemEntity(level, x, y + 1.0, z, reward);
            itemEntity.setDeltaMovement(0, 0, 0);
            itemEntity.setPickUpDelay(0);
            itemEntity.setUnlimitedLifetime();
            level.addFreshEntity(itemEntity);

            
            GravemindMessagePacket.sendToPlayer(player, "I send reward to your location");

        } catch (Exception e) {
            e.printStackTrace();
            if (!player.getInventory().add(reward)) player.drop(reward, false);
        }
    }
}
