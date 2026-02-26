package net.alekrus.shphysarum.SculkPlayerAbility.Spawn;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SleepEventHandler {

    @SubscribeEvent
    public static void onPlayerSleep(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();


        if (InfectionHandler.isInfected(player)) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            player.sendSystemMessage(Component.literal("§3You don't need sleep anymore."));
        }

    }
}
