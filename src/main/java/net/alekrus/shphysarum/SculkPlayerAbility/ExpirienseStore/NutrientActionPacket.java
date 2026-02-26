package net.alekrus.shphysarum.SculkPlayerAbility.ExpirienseStore;


import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NutrientActionPacket {
    public final int actionType;

    public NutrientActionPacket(int actionType) {
        this.actionType = actionType;
    }

    public static NutrientActionPacket decode(FriendlyByteBuf buf) {
        return new NutrientActionPacket(buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(actionType);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !InfectionHandler.isInfected(player)) return;


            NutrientEssenceHandler.handleAction(player, actionType);
        });
        context.setPacketHandled(true);
    }
}