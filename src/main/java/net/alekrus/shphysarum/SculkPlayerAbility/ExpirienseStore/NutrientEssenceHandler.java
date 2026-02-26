package net.alekrus.shphysarum.SculkPlayerAbility.ExpirienseStore;

import net.alekrus.shphysarum.ItemsAndTab.Items.ModItems;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class NutrientEssenceHandler {


    private static final String TAG_CHARGING_TICKS = "shphysarum_charging_ticks";


    private static final int CREATION_COST_POINTS = 160;

    public static void handleAction(ServerPlayer player, int actionType) {
        ItemStack heldItem = player.getMainHandItem();
        boolean holdingEssence = heldItem.getItem() == ModItems.NUTRIENT_ESSENCE.get();

        if (actionType == 0) {
            if (!holdingEssence) {

                if (player.experienceLevel >= 10) {
                    player.giveExperienceLevels(-10);

                    ItemStack essence = new ItemStack(ModItems.NUTRIENT_ESSENCE.get());
                    essence.getOrCreateTag().putInt("StoredXP", CREATION_COST_POINTS);

                    Vec3 look = player.getLookAngle();
                    ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getEyeY() - 0.2, player.getZ(), essence);
                    itemEntity.setDeltaMovement(look.scale(0.5));
                    player.level().addFreshEntity(itemEntity);

                    player.level().playSound(null, player.blockPosition(), SoundEvents.WARDEN_DIG, SoundSource.PLAYERS, 1.0F, 1.5F);
                } else {
                    player.displayClientMessage(Component.literal("§3Requires level 10 to extract essence."), true);
                }
            } else {

                chargeEssence(player, heldItem, 1);
            }
        }
        else if (actionType == 1) {
            if (holdingEssence) {
                player.getPersistentData().putInt(TAG_CHARGING_TICKS, 1);
            }
        }
        else if (actionType == 2) {
            player.getPersistentData().putInt(TAG_CHARGING_TICKS, 0);
        }
    }

    private static void chargeEssence(ServerPlayer player, ItemStack essenceStack, int targetRate) {
        if (player.totalExperience <= 0) return;

        int currentXp = essenceStack.getOrCreateTag().getInt("StoredXP");
        int spaceLeft = NutrientEssenceItem.MAX_XP_CAPACITY - currentXp;

        if (spaceLeft > 0) {

            int toTransfer = Math.min(targetRate, Math.min(player.totalExperience, spaceLeft));

            if (toTransfer > 0) {
                player.giveExperiencePoints(-toTransfer);
                essenceStack.getOrCreateTag().putInt("StoredXP", currentXp + toTransfer);


                if (player.tickCount % 4 == 0) {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F, 0.1F + (toTransfer * 0.05F));
                }
            }
        } else {

            player.getPersistentData().putInt(TAG_CHARGING_TICKS, 0);
            if (player.tickCount % 10 == 0) {
                player.displayClientMessage(Component.literal("§aEssence is full!"), true);
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            int chargeTicks = player.getPersistentData().getInt(TAG_CHARGING_TICKS);

            if (chargeTicks > 0) {
                ItemStack heldItem = player.getMainHandItem();

                if (heldItem.getItem() == ModItems.NUTRIENT_ESSENCE.get()) {


                    int rate = 1 + (chargeTicks / 40) * 2;

                    chargeEssence(player, heldItem, rate);


                    player.getPersistentData().putInt(TAG_CHARGING_TICKS, chargeTicks + 1);
                } else {

                    player.getPersistentData().putInt(TAG_CHARGING_TICKS, 0);
                }
            }
        }
    }
}