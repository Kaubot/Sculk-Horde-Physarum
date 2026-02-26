package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.FaithSystem;

import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID)
public class FaithEvents {

    
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int savedFaith = FaithHandler.getFaith(player);
            FaithHandler.syncFaithToClient(player, savedFaith);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            int oldFaith = FaithHandler.getFaith(oldPlayer);
            newPlayer.getPersistentData().putInt("sculk_faith", oldFaith);
            FaithHandler.syncFaithToClient(newPlayer, oldFaith);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int savedFaith = FaithHandler.getFaith(player);
            FaithHandler.syncFaithToClient(player, savedFaith);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            int savedFaith = FaithHandler.getFaith(player);
            FaithHandler.syncFaithToClient(player, savedFaith);
        }
    }
}
