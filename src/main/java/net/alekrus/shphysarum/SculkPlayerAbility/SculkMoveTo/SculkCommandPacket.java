package net.alekrus.shphysarum.SculkPlayerAbility.SculkMoveTo;

import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class SculkCommandPacket {

    public enum Action {
        MOVE_TO_POS,
        TOGGLE_FOLLOW
    }

    private final Action action;
    private final BlockPos targetPos;

    public SculkCommandPacket(Action action, BlockPos pos) {
        this.action = action;
        this.targetPos = pos != null ? pos : BlockPos.ZERO;
    }

    public static void encode(SculkCommandPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeBlockPos(msg.targetPos);
    }

    public static SculkCommandPacket decode(FriendlyByteBuf buf) {
        return new SculkCommandPacket(buf.readEnum(Action.class), buf.readBlockPos());
    }

    public static void handle(SculkCommandPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            if (!InfectionHandler.isInfected(player)) return;

            ServerLevel level = player.serverLevel();

            if (msg.action == Action.MOVE_TO_POS) {
                handleMoveCommand(player, level, msg.targetPos);
            } else if (msg.action == Action.TOGGLE_FOLLOW) {
                SculkFollowerManager.toggleFollow(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleMoveCommand(ServerPlayer player, ServerLevel level, BlockPos target) {

        level.playSound(null, target, SoundEvents.SCULK_CLICKING, SoundSource.PLAYERS, 1.0F, 1.0F);

        double startX = player.getX();
        double startY = player.getEyeY();
        double startZ = player.getZ();
        double dx = target.getX() + 0.5 - startX;
        double dy = target.getY() + 0.5 - startY;
        double dz = target.getZ() + 0.5 - startZ;
        int steps = 20;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            level.sendParticles(ParticleTypes.SCULK_SOUL, startX + dx * t, startY + dy * t, startZ + dz * t, 1, 0, 0, 0, 0);
        }

        List<Mob> sculkMobs = level.getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(30),
                mob -> mob instanceof ISculkSmartEntity
        );

        int count = 0;
        for (Mob mob : sculkMobs) {
            SculkFollowerManager.removeFollower(player, mob);

            mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.2D);
            count++;
        }
    }
}
