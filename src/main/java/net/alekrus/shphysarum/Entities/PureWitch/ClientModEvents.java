package net.alekrus.shphysarum.Entities.PureWitch;

import net.alekrus.shphysarum.shPhysarum;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;



@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        
        event.registerEntityRenderer(ModEntities.SCULK_WITCH.get(), SculkWitchRenderer::new);


    }
}