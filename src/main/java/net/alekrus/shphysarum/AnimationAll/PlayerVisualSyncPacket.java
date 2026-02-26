package net.alekrus.shphysarum.AnimationAll;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerVisualSyncPacket {
    private final int entityId;
    private final boolean isInfected;
    private final boolean hasTentacleSkill;
    private final boolean tentaclesActive;
    private final boolean isBlocking;
    private final long lastAttackTime;

    public PlayerVisualSyncPacket(int entityId, boolean isInfected, boolean hasTentacleSkill, boolean tentaclesActive, boolean isBlocking, long lastAttackTime) {
        this.entityId = entityId;
        this.isInfected = isInfected;
        this.hasTentacleSkill = hasTentacleSkill;
        this.tentaclesActive = tentaclesActive;
        this.isBlocking = isBlocking;
        this.lastAttackTime = lastAttackTime;
    }

    public static PlayerVisualSyncPacket decode(FriendlyByteBuf buf) {
        return new PlayerVisualSyncPacket(buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readLong());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(isInfected);
        buf.writeBoolean(hasTentacleSkill);
        buf.writeBoolean(tentaclesActive);
        buf.writeBoolean(isBlocking);
        buf.writeLong(lastAttackTime);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(entityId);
                if (entity instanceof Player player) {

                    player.getPersistentData().putBoolean("sh_isInfected", isInfected);
                    player.getPersistentData().putBoolean("sh_hasTentacleSkill", hasTentacleSkill);
                    player.getPersistentData().putBoolean("sh_tentaclesActive", tentaclesActive);
                    player.getPersistentData().putBoolean("sh_isBlocking", isBlocking);
                    player.getPersistentData().putLong("sh_lastAttackTime", lastAttackTime);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}