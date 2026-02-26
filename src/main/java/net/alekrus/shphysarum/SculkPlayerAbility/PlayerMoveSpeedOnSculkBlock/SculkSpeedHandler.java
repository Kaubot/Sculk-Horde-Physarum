package net.alekrus.shphysarum.SculkPlayerAbility.PlayerMoveSpeedOnSculkBlock;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class SculkSpeedHandler {

    private static final UUID SCULK_SPEED_ID = UUID.fromString("a1b2c3d4-e5f6-7890-1234-56789abcdef0");

    private static final AttributeModifier SCULK_SPEED_MODIFIER = new AttributeModifier(
            SCULK_SPEED_ID,
            "Sculk Surface Speed Bonus",
            0.30D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
    );

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;

        if (player.level().isClientSide) return;

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        boolean shouldBeFast = false;

        if (InfectionHandler.isInfected(player)) {
            if (isStandingOnSculk(player)) {
                shouldBeFast = true;
            }
        }


        boolean hasModifier = speedAttribute.hasModifier(SCULK_SPEED_MODIFIER);

        if (shouldBeFast && !hasModifier) {

            speedAttribute.addTransientModifier(SCULK_SPEED_MODIFIER);
        } else if (!shouldBeFast && hasModifier) {

            speedAttribute.removeModifier(SCULK_SPEED_MODIFIER);
        }
    }


    private static boolean isStandingOnSculk(Player player) {
        BlockPos pos = player.getOnPos();
        BlockState state = player.level().getBlockState(pos);

        if (state.isAir()) {
            pos = player.blockPosition();
            state = player.level().getBlockState(pos);
        }

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null) return false;


        if (blockId.getNamespace().equals("sculkhorde")) {
            return true;
        }

        String path = blockId.getPath();
        if (blockId.getNamespace().equals("minecraft") && path.contains("sculk")) {
            return true;
        }

        return false;
    }
}
