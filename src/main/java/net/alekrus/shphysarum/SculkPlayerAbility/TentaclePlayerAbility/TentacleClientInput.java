package net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility;

import net.alekrus.shphysarum.KeyBind.ModKeyBindings;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkAbility;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class TentacleClientInput {

    private static int holdTimer = 0;
    private static boolean isHolding = false;
    private static boolean isBlockingStateSent = false;

    
    private static int attackCooldown = 0;
    private static int toggleCooldown = 0;

    
    private static final int ATTACK_COOLDOWN_MAX = 60;
    private static final int TOGGLE_COOLDOWN_MAX = 20;

    private static final int BLOCK_THRESHOLD = 5;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (attackCooldown > 0) attackCooldown--;
        if (toggleCooldown > 0) toggleCooldown--;

        
        if (!ClientGravemindState.tentaclesActive) {
            isHolding = false;
            holdTimer = 0;
            isBlockingStateSent = false;
            ClientGravemindState.isBlocking = false;
            return;
        }

        if (isHolding) {
            holdTimer++;
            if (holdTimer >= BLOCK_THRESHOLD && !isBlockingStateSent) {
                PacketHandler.CHANNEL.sendToServer(new TentacleActionPacket(TentacleActionPacket.ActionType.BLOCK_START));
                isBlockingStateSent = true;
                ClientGravemindState.isBlocking = true;
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (ClientSkillData.getSelectedAbility() != SculkAbility.SHARP_TENTACLE) return;

        
        if (!ClientSkillData.hasSkill("adaptive_body_structuring")) return;

        if (event.getKey() == ModKeyBindings.ABILITY_ACTIVATE_KEY.getKey().getValue()) {

            
            if (ModKeyBindings.ABILITY_ACTIVATE_KEY.isDown()) {

                
                if (mc.player.isShiftKeyDown()) {
                    if (toggleCooldown == 0) {
                        boolean newState = !ClientGravemindState.tentaclesActive;
                        PacketHandler.CHANNEL.sendToServer(new TentacleTogglePacket(newState));
                        toggleCooldown = TOGGLE_COOLDOWN_MAX;

                        if (!newState) {
                            if (isBlockingStateSent) {
                                PacketHandler.CHANNEL.sendToServer(new TentacleActionPacket(TentacleActionPacket.ActionType.BLOCK_END));
                            }
                            isBlockingStateSent = false;
                            ClientGravemindState.isBlocking = false;
                            isHolding = false;
                        }
                    }
                    return;
                }

                
                if (ClientGravemindState.tentaclesActive) {
                    if (!isHolding) {
                        isHolding = true;
                        holdTimer = 0;
                    }
                }
            }
            
            else {
                if (isHolding) {
                    if (isBlockingStateSent) {
                        PacketHandler.CHANNEL.sendToServer(new TentacleActionPacket(TentacleActionPacket.ActionType.BLOCK_END));
                        isBlockingStateSent = false;
                        ClientGravemindState.isBlocking = false;
                    } else if (holdTimer < BLOCK_THRESHOLD) {
                        if (attackCooldown == 0) {
                            PacketHandler.CHANNEL.sendToServer(new TentacleActionPacket(TentacleActionPacket.ActionType.ATTACK));
                            ClientGravemindState.triggerAttackAnimation();
                            attackCooldown = ATTACK_COOLDOWN_MAX;
                        }
                    }
                }
                isHolding = false;
                holdTimer = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;

        if (ClientSkillData.getSelectedAbility() == SculkAbility.SHARP_TENTACLE
                && ClientGravemindState.isBlocking
                && event.getButton() == 0) {

            event.setCanceled(true);
        }
    }
}
