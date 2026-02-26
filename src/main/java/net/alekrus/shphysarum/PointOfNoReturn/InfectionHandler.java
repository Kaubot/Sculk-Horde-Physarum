package net.alekrus.shphysarum.PointOfNoReturn;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shphysarum", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfectionHandler {

    public static boolean isInfected(Player player) {
        if (player == null) return false;

        if (player.level().isClientSide) {
            return ClientInfectionHelper.checkClient(player);
        } else {
            return player.getCapability(SculkMindProvider.SCULK_MIND)
                    .map(cap -> cap.hasSkill("root"))
                    .orElse(false);
        }
    }

    public static boolean isClientInfected(Player player) {
        return isInfected(player);
    }

    private static class ClientInfectionHelper {
        public static boolean checkClient(Player player) {
            if (player == net.minecraft.client.Minecraft.getInstance().player) {
                
                return ClientSkillData.hasSkill("root");
            } else {
                return player.getPersistentData().getBoolean("sh_isInfected");
            }
        }
    }

    public static void setInfected(Player player, boolean value) {
        
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();

            boolean wasVisual = oldPlayer.getPersistentData().getBoolean("sh_isInfected");
            newPlayer.getPersistentData().putBoolean("sh_isInfected", wasVisual);
        }
    }
}
