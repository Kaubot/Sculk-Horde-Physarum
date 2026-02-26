package net.alekrus.shphysarum.Block;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeaconUnlockTracker {

    private static final String UNLOCK_TAG = "shphysarum_has_seen_beacon";


    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (event.player.tickCount % 20 != 0) return;

        ServerPlayer player = (ServerPlayer) event.player;


        if (player.getPersistentData().getBoolean(UNLOCK_TAG)) return;

        Item beaconItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("shphysarum", "sculk_beacon"));
        if (beaconItem == null) return;


        boolean hasItem = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(beaconItem)) { hasItem = true; break; }
        }

        if (hasItem) {

            player.getPersistentData().putBoolean(UNLOCK_TAG, true);

            sendToClient(player, true);
        }
    }


    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean unlocked = player.getPersistentData().getBoolean(UNLOCK_TAG);
            sendToClient(player, unlocked);
        }
    }

    private static void sendToClient(ServerPlayer player, boolean unlocked) {
        PacketHandler.CHANNEL.sendTo(
                new BeaconUnlockSyncPacket(unlocked),
                player.connection.connection,
                NetworkDirection.PLAY_TO_CLIENT
        );
    }
}
