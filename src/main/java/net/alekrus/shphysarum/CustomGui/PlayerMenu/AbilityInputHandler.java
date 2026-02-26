package net.alekrus.shphysarum.CustomGui.PlayerMenu;

import net.alekrus.shphysarum.KeyBind.ModKeyBindings;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.ExpirienseStore.NutrientActionPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.InfectedAroundPlayer.SporeBurstPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.PlayerBurrowInSculk.SculkBurrowPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.PlayerIntecraftSculkSummoner.RaidRequestPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.PlayerJumpCrosshair.SculkLeapPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.RaidPlayerInitiator.RaidStartPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkAbility;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkVision.SculkBlinkVision;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class AbilityInputHandler {

    private static boolean wasAbilityKeyDown = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        
        if (ModKeyBindings.ABILITY_MENU_KEY.isDown() && mc.screen == null) {
            if (InfectionHandler.isClientInfected(mc.player)) {
                mc.setScreen(new net.alekrus.shphysarum.CustomGui.PlayerMenu.SculkAbilityRadialMenu());
            }
        }

        
        boolean isDown = ModKeyBindings.ABILITY_ACTIVATE_KEY.isDown();

        if (isDown != wasAbilityKeyDown) {
            wasAbilityKeyDown = isDown;

            if (InfectionHandler.isClientInfected(mc.player) && ClientSkillData.getSelectedAbility() == SculkAbility.ESSENCE_EXTRACT) {
                if (isDown) {
                    
                    PacketHandler.CHANNEL.sendToServer(new NutrientActionPacket(1));
                } else {
                    
                    PacketHandler.CHANNEL.sendToServer(new NutrientActionPacket(2));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (ModKeyBindings.ABILITY_ACTIVATE_KEY.consumeClick()) {
            if (!InfectionHandler.isClientInfected(mc.player)) return;

            SculkAbility selected = ClientSkillData.getSelectedAbility();

            if (selected == SculkAbility.NONE) {
                mc.player.displayClientMessage(Component.literal("§7No ability selected."), true);
                return;
            }

            if (!ClientSkillData.hasSkill(selected.getSkillId())) {
                mc.player.displayClientMessage(Component.literal("§cAbility not learned yet."), true);
                return;
            }

            switch (selected) {
                case VISION:
                    SculkBlinkVision.toggleVision();
                    break;
                case SUMMONER:
                    PacketHandler.CHANNEL.sendToServer(new RaidRequestPacket());
                    break;
                case BURST:
                    PacketHandler.CHANNEL.sendToServer(new SporeBurstPacket());
                    break;
                case RAID:
                    PacketHandler.CHANNEL.sendToServer(new RaidStartPacket());
                    break;
                case LEAP:
                    if (!mc.player.onGround()) return;
                    PacketHandler.CHANNEL.sendToServer(new SculkLeapPacket());
                    break;
                case BURROW:
                    PacketHandler.CHANNEL.sendToServer(new SculkBurrowPacket());
                    break;
                case ESSENCE_EXTRACT:
                    
                    PacketHandler.CHANNEL.sendToServer(new NutrientActionPacket(0));
                    break;
                default:
                    break;
            }
        }
    }
}
