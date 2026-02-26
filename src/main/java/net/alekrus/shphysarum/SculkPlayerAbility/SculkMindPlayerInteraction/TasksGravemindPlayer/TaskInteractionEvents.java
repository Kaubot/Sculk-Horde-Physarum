package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer;



import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMind;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class TaskInteractionEvents {

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) return;

        CompoundTag data = event.getEntity().getPersistentData();
        if (data.contains("GravemindCourier")) {
            int timer = data.getInt("GravemindCourier");
            timer--;

            if (timer <= 0) {
                SoundEvent teleportSound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.tryParse("sculkhorde:sculk_enderman_portal"));
                if (teleportSound == null) teleportSound = SoundEvents.ENDERMAN_TELEPORT;
                event.getEntity().level().playSound(null, event.getEntity().blockPosition(), teleportSound, SoundSource.HOSTILE, 1.0f, 1.0f);
                event.getEntity().discard();
            } else {
                data.putInt("GravemindCourier", timer);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ResourceLocation blockRL = event.getLevel().getBlockState(event.getPos()).getBlock().asItem().getDefaultInstance().getItem().getDescriptionId() != null
                ? net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()) : null;

        if (blockRL != null && blockRL.toString().equals("sculkhorde:sculk_ancient_node")) {
            Player player = event.getEntity();

            player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                if (cap instanceof SculkMind mind) {
                    if (mind.hasPendingReward()) {
                        ItemStack reward = mind.getPendingReward();
                        if (!player.getInventory().add(reward)) player.drop(reward, false);
                        mind.clearPendingReward();

                        
                        if (player instanceof ServerPlayer serverPlayer) {
                            GravemindMessagePacket.sendToPlayer(serverPlayer, "Reward received.");
                        }

                        event.setCanceled(true);
                    }
                }
            });
            if (event.isCanceled()) return;

            TaskManager.handleItemTurnIn(player, event.getItemStack());
            TaskManager.handleLevelSacrifice(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (event.player.tickCount % 60 != 0) return;

        if (TaskManager.getCurrentTask() != null && TaskManager.getCurrentTask().type == GravemindTask.Type.FIND_NEW_VILLAGE) {
            ServerPlayer player = (ServerPlayer) event.player;
            ServerLevel level = player.serverLevel();
            BlockPos pos = player.blockPosition();

            if (level.isVillage(pos)) {
                String locId = "region_" + (pos.getX() >> 8) + "_" + (pos.getZ() >> 8);
                player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                    if (cap instanceof SculkMind mind) {
                        if (!mind.hasVisitedVillage(locId)) {
                            mind.addVisitedVillage(locId);
                            TaskManager.completeVillageTask(player);
                        }
                    }
                });
            }
        }
    }
}
