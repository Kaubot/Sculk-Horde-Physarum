package net.alekrus.shphysarum.Block;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class BeaconUnlockSyncPacket {
    private final boolean isUnlocked;

    public BeaconUnlockSyncPacket(boolean isUnlocked) {
        this.isUnlocked = isUnlocked;
    }

    public static BeaconUnlockSyncPacket decode(FriendlyByteBuf buf) {
        return new BeaconUnlockSyncPacket(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isUnlocked);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {

            ClientGravemindState.hasSeenBeacon = isUnlocked;
        });
        context.setPacketHandled(true);
    }
}