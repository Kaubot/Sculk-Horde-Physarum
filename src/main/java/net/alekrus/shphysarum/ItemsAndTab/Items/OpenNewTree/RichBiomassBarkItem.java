package net.alekrus.shphysarum.ItemsAndTab.Items.OpenNewTree;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

public class RichBiomassBarkItem extends Item {

    public static final String VISIBILITY_MARKER_SKILL = "structure_insight";
    private static final String PARENT_SKILL = "adaptive_morph";

    public RichBiomassBarkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {

                if (!cap.hasSkill(PARENT_SKILL)) {
                    
                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new GravemindMessagePacket("Your body is not yet ready for this yet."));

                    level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.SCULK_SHRIEKER_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
                    return;
                }

                if (cap.hasSkill(VISIBILITY_MARKER_SKILL)) {
                    serverPlayer.sendSystemMessage(Component.literal("§3This knowledge has already been obtained."));
                    return;
                }

                cap.unlockSkill(VISIBILITY_MARKER_SKILL);

                if (!serverPlayer.isCreative()) {
                    stack.shrink(1);
                }

                
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new GravemindMessagePacket("Your flesh is being rebuilt... new mutations are available."));

                level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 1.0f, 1.0f);
                level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);

                PacketHandler.CHANNEL.sendTo(
                        new SkillSyncPacket(
                                cap.getUnlockedSkills(), cap.getEvoPoints(), cap.getFaith(),
                                cap.getActiveTaskNBT(), cap.areTentaclesActive(),
                                cap.getUserFollowerLimit(), cap.getAllowedFollowerTypes(),
                                cap.getKnownAnchors(), cap.getActiveAbilitiesSet()
                        ),
                        serverPlayer.connection.connection, NetworkDirection.PLAY_TO_CLIENT
                );
            });
        }

        return InteractionResultHolder.success(stack);
    }
}
