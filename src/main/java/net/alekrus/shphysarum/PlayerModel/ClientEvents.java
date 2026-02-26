package net.alekrus.shphysarum.PlayerModel;

import net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility.TentaclesLayerRenderer;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinType : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skinType);
            if (renderer != null) {
                
                renderer.addLayer(new SporeInfectionLayerRenderer(renderer));

                
                renderer.addLayer(new TentaclesLayerRenderer(renderer));
            }
        }
    }
}
