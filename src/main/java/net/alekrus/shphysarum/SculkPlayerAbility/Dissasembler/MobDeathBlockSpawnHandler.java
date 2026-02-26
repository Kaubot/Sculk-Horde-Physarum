package net.alekrus.shphysarum.SculkPlayerAbility.Dissasembler;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;

import com.github.sculkhorde.common.block.FleshyCompostBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobDeathBlockSpawnHandler {

    private static final ResourceLocation COMPOST_RL = ResourceLocation.fromNamespaceAndPath("sculkhorde", "fleshy_compost_block");

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        if (!InfectionHandler.isInfected(player)) return;

        LivingEntity deadEntity = event.getEntity();
        Level level = deadEntity.level();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;

        Block block = ForgeRegistries.BLOCKS.getValue(COMPOST_RL);

        if (block instanceof FleshyCompostBlock compostBlock) {

            BlockPos targetPos = deadEntity.blockPosition().below();

            compostBlock.spawn(level, targetPos, deadEntity);
        }
    }
}
