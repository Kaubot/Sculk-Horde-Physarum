package net.alekrus.shphysarum.Block;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfectedWebBypass {


    private static Field stuckSpeedField;
    private static boolean reflectionInitialized = false;

    private static void initReflection() {
        if (reflectionInitialized) return;
        reflectionInitialized = true;
        try {

            stuckSpeedField = Entity.class.getDeclaredField("stuckSpeedMultiplier");
        } catch (Exception e) {
            try {

                stuckSpeedField = Entity.class.getDeclaredField("f_19788_");
            } catch (Exception ex) {

            }
        }
        if (stuckSpeedField != null) {
            stuckSpeedField.setAccessible(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;


        boolean isInfected = player.level().isClientSide
                ? InfectionHandler.isClientInfected(player)
                : InfectionHandler.isInfected(player);

        if (!isInfected) return;

        initReflection();
        if (stuckSpeedField == null) return;

        try {

            Vec3 currentMultiplier = (Vec3) stuckSpeedField.get(player);


            if (currentMultiplier == null || currentMultiplier.equals(Vec3.ZERO)) {
                return;
            }

            if (currentMultiplier.x == 0.75 && currentMultiplier.z == 0.75) {

                boolean insideWeb = false;
                AABB box = player.getBoundingBox();


                BlockPos minPos = BlockPos.containing(box.minX + 0.001D, box.minY + 0.001D, box.minZ + 0.001D);
                BlockPos maxPos = BlockPos.containing(box.maxX - 0.001D, box.maxY - 0.001D, box.maxZ - 0.001D);

                for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
                    BlockState state = player.level().getBlockState(pos);

                    if (ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().equals("sculkhorde:living_web_block")) {
                        insideWeb = true;
                        break;
                    }
                }


                if (insideWeb) {
                    stuckSpeedField.set(player, Vec3.ZERO);
                }
            }

        } catch (Exception ignored) {

        }
    }
}