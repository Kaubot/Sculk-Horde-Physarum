package net.alekrus.shphysarum.SculkPlayerAbility.SFX;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum", value = Dist.CLIENT)
public class InfectedSoundBlocker {

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        
        String soundName = event.getSound().getLocation().toString();

        
        if (soundName.contains("sculkhorde") && soundName.contains("ambience")) {

            
            if (InfectionHandler.isClientInfected(Minecraft.getInstance().player)) {
                
                event.setSound(null);
            }
        }
    }
}