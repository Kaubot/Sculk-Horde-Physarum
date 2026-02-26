package net.alekrus.shphysarum.SculkPlayerAbility.SculkPhantomFly;

import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class PhantomFlightClient {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        
        if (mc.player.getVehicle() instanceof SculkPhantomEntity) {

            
            float forward = mc.player.input.up ? 1.0f : (mc.player.input.down ? -1.0f : 0.0f);
            float strafe = mc.player.input.left ? 1.0f : (mc.player.input.right ? -1.0f : 0.0f);

            boolean jump = mc.player.input.jumping; 

            
            PacketHandler.CHANNEL.sendToServer(new PhantomFlightPacket(forward, strafe, jump));
        }
    }
}