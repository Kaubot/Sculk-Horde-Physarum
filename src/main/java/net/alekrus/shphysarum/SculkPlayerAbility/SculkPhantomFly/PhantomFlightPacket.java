package net.alekrus.shphysarum.SculkPlayerAbility.SculkPhantomFly;


import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PhantomFlightPacket {
    private final float forward;
    private final float strafe;
    private final boolean jump;

    public PhantomFlightPacket(float forward, float strafe, boolean jump) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
    }

    public static PhantomFlightPacket decode(FriendlyByteBuf buf) {
        return new PhantomFlightPacket(buf.readFloat(), buf.readFloat(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(forward);
        buf.writeFloat(strafe);
        buf.writeBoolean(jump);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.getVehicle() instanceof SculkPhantomEntity phantom)) return;

            
            Vec3 currentMotion = phantom.getDeltaMovement();
            Vec3 lookVec = player.getLookAngle();

            
            int flyTicks = phantom.getPersistentData().getInt("FlightTicks");

            if (forward > 0) {
                
                flyTicks = Math.min(120, flyTicks + 1);
            } else {
                
                flyTicks = Math.max(0, flyTicks - 2);
            }
            
            phantom.getPersistentData().putInt("FlightTicks", flyTicks);

            
            double progress = flyTicks / 120.0;

            
            double minSpeed = 0.3; 
            double absoluteMaxSpeed = 1.4; 

            
            double currentSpeedLimit = minSpeed + (absoluteMaxSpeed - minSpeed) * progress;

            
            double baseAcceleration = 0.02 + (0.05 * progress);
            double glideFactor = 0.96; 

            
            if (forward > 0) {
                
                double diveBoost = Math.max(0, -lookVec.y) * (0.15 * progress);
                double currentAcceleration = baseAcceleration + diveBoost;

                Vec3 addedMotion = lookVec.scale(currentAcceleration);
                currentMotion = currentMotion.add(addedMotion);
            } else {
                
                currentMotion = currentMotion.multiply(glideFactor, 0.90, glideFactor);
                currentMotion = currentMotion.add(0, -0.02, 0); 
            }

            
            if (jump) {
                
                currentMotion = currentMotion.add(0, 0.08, 0);
            }

            
            
            if (currentMotion.lengthSqr() > currentSpeedLimit * currentSpeedLimit) {
                currentMotion = currentMotion.normalize().scale(currentSpeedLimit);
            }

            
            phantom.setDeltaMovement(currentMotion);
            phantom.hurtMarked = true;
        });
        context.setPacketHandled(true);
    }
}
