package net.alekrus.shphysarum.SculkPlayerAbility.ExpirienseStore;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NutrientEssenceItem extends Item {

    public static final int MAX_XP_CAPACITY = 8670;

    public NutrientEssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);


        boolean isInfected = level.isClientSide
                ? InfectionHandler.isClientInfected(player)
                : InfectionHandler.isInfected(player);

        if (!isInfected) {

            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("§cYour body violently rejects it."), true);
                
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 1));
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0f, 0.5f);
            }
            return InteractionResultHolder.fail(stack);
        }


        int storedXp = stack.getOrCreateTag().getInt("StoredXP");

        if (storedXp > 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksLeft) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {


            if (!InfectionHandler.isInfected(player)) {
                player.stopUsingItem();
                return;
            }

            int storedXp = stack.getOrCreateTag().getInt("StoredXP");

            int ticksHeld = 72000 - ticksLeft;
            int rate = 1 + (ticksHeld / 40) * 2;

            if (storedXp > 0) {
                int amountToGive = Math.min(rate, storedXp);

                stack.getOrCreateTag().putInt("StoredXP", storedXp - amountToGive);
                player.giveExperiencePoints(amountToGive);

                if (ticksLeft % Math.max(2, 5 - (rate / 3)) == 0) {
                    level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.4F, 1.0F + (level.random.nextFloat() * 0.4F));
                }
            } else {
                stack.shrink(1);
                player.stopUsingItem();
                level.playSound(null, player.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 0.5f, 2.0f);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int storedXp = stack.getOrCreateTag().getInt("StoredXP");
        tooltip.add(Component.literal("Pure mass enriched with experience.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Stored XP: §a" + storedXp + " §8/§2 " + MAX_XP_CAPACITY));
        tooltip.add(Component.literal("Hold Right-Click to consume.").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Lethal to non-infected organics.").withStyle(ChatFormatting.DARK_RED));
    }
}
