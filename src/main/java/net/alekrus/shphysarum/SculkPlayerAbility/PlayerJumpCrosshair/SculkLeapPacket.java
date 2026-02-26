package net.alekrus.shphysarum.SculkPlayerAbility.PlayerJumpCrosshair;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SculkLeapPacket {

    public SculkLeapPacket() {}
    public SculkLeapPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    public static SculkLeapPacket decode(FriendlyByteBuf buf) { return new SculkLeapPacket(buf); }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (!InfectionHandler.isInfected(player)) return;


            if (!player.onGround()) {

                return;
            }


            if (player.totalExperience < 35 && !player.isCreative()) {
                player.sendSystemMessage(Component.literal("§3Not enough XP. Need 35 points."));
                return;
            }


            if (!player.isCreative()) {
                player.giveExperiencePoints(-35);
            }


            Vec3 look = player.getLookAngle();


            double force = 1.5;


            double yBoost = 1.2;

            player.setDeltaMovement(look.x * force, yBoost, look.z * force);


            player.hurtMarked = true;
            player.resetFallDistance();

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.PLAYERS, 1.0F, 1.5F);
        });
        context.setPacketHandled(true);
    }
}