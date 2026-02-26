package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.KeyBind.ModKeyBindings;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class GravemindKeyHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (ModKeyBindings.GRAVEMIND_KEY.consumeClick()) {

            if (InfectionHandler.isClientInfected(mc.player)) {
                mc.setScreen(new GravemindMainScreen());
            }

        }
    }
}