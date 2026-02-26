package net.alekrus.shphysarum.SculkPlayerAbility.PlayerBurrowInSculk;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SculkBurrowSyncPacket {
    private final boolean active;

    public SculkBurrowSyncPacket(boolean active) {
        this.active = active;
    }

    public static SculkBurrowSyncPacket decode(FriendlyByteBuf buf) {
        return new SculkBurrowSyncPacket(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            
            
            ClientHandler.handleBurrowSync(active);
        });
        context.setPacketHandled(true);
    }

    
    private static class ClientHandler {
        public static void handleBurrowSync(boolean active) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                
                SculkBurrowClientHandler.setBurrowActive(active);

                if (active) {
                    mc.player.setForcedPose(Pose.SWIMMING);
                } else {
                    mc.player.setForcedPose(null);
                }
            }
        }
    }
}
