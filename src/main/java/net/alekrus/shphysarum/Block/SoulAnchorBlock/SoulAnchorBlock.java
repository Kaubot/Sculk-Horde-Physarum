package net.alekrus.shphysarum.Block.SoulAnchorBlock;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMind;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

public class SoulAnchorBlock extends Block {
    public static final IntegerProperty CHARGES = IntegerProperty.create("charges", 0, 12);

    public SoulAnchorBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(CHARGES, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGES);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {


        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {

            if (!InfectionHandler.isInfected(serverPlayer)) {
                serverPlayer.displayClientMessage(Component.literal("§cThe Anchor remains dormant to your touch."), true);
                return InteractionResult.FAIL;
            }
        }


        ItemStack stack = player.getItemInHand(hand);


        ResourceLocation cryingSoulID = ResourceLocation.tryParse("sculkhorde:crying_souls");
        boolean isChargingItem = ForgeRegistries.ITEMS.getKey(stack.getItem()).equals(cryingSoulID);

        if (isChargingItem) {
            int current = state.getValue(CHARGES);
            if (current < 12) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.setValue(CHARGES, current + 1), 3);

                    level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1, 1);
                    if (!player.isCreative()) stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }


        if (hand == InteractionHand.MAIN_HAND) {
            if (!level.isClientSide && player instanceof ServerPlayer sp) {

                if (state.getValue(CHARGES) == 0) {
                    sp.sendSystemMessage(Component.literal("§3The Anchor is dormant. It requires Crying Souls."));
                    return InteractionResult.FAIL;
                }


                sp.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
                    if (cap instanceof SculkMind mind) {
                        boolean added = mind.addKnownAnchor(pos);


                        syncAnchorData(sp, mind);

                        if (added) {
                            sp.sendSystemMessage(Component.literal("§3Soul tethered to Anchor at " + pos.toShortString()));
                            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1, 1);
                        } else {
                            sp.sendSystemMessage(Component.literal("§7Anchor already tethered. Respawn point updated."));
                        }


                        sp.setRespawnPosition(level.dimension(), pos.above(), 0.0F, true, true);
                    }
                });
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }


    private void syncAnchorData(ServerPlayer player, SculkMind mind) {
        PacketHandler.CHANNEL.sendTo(
                new SkillSyncPacket(
                        mind.getUnlockedSkills(),
                        mind.getEvoPoints(),
                        mind.getFaith(),
                        mind.getActiveTaskNBT(),
                        mind.areTentaclesActive(),
                        mind.getUserFollowerLimit(),
                        mind.getAllowedFollowerTypes(),
                        mind.getKnownAnchors(),
                        mind.getActiveAbilitiesSet()
                ),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }
}
