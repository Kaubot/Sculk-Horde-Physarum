package net.alekrus.shphysarum.PlayerInfectedSeeNoThread;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SculkBossBarHider {

    @SubscribeEvent
    public static void onBossBarRender(CustomizeGuiOverlayEvent.BossEventProgress event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        
        if (!InfectionHandler.isClientInfected(player)) return;

        
        String bossName = event.getBossEvent().getName().getString();

        
        if (bossName.contains("Sculk Enderman")) {
            event.setCanceled(true); 
        }

        
        if (bossName.contains("Sculk Raid")) {
            event.setCanceled(true);
        }

        
        
    }
}