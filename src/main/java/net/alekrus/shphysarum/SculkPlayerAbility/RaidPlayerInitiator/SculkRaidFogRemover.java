package net.alekrus.shphysarum.SculkPlayerAbility.RaidPlayerInitiator;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class SculkRaidFogRemover {


    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (!InfectionHandler.isClientInfected(player)) return;


        if (event.getMode() == FogRenderer.FogMode.FOG_TERRAIN) {
            float farPlaneDistance = event.getRenderer().getRenderDistance();

            event.setNearPlaneDistance(farPlaneDistance * 0.75f);
            event.setFarPlaneDistance(farPlaneDistance);

            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (!InfectionHandler.isClientInfected(player)) return;

    }
}
