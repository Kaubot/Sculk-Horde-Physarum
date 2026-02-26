package net.alekrus.shphysarum.SculkPlayerAbility.SculkMoveTo;

import net.alekrus.shphysarum.KeyBind.ModKeyBindings;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum", value = Dist.CLIENT)
public class SculkMoveInputHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (ModKeyBindings.MOVE_KEY.consumeClick()) {

            if (!InfectionHandler.isClientInfected(mc.player)) {
                continue;
            }

            if (!(mc.screen instanceof SculkCommandRadialScreen)) {
                mc.setScreen(new SculkCommandRadialScreen());
            }
        }
    }
}
