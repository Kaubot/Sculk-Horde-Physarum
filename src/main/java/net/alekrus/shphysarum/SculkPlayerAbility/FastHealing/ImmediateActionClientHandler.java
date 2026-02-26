package net.alekrus.shphysarum.SculkPlayerAbility.FastHealing;
import net.alekrus.shphysarum.KeyBind.ModKeyBindings;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.alekrus.shphysarum.KeyBind.ModKeyBindings;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkAbility;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class ImmediateActionClientHandler {

    private static boolean isHolding = false;
    private static boolean wasHolding = false;
    private static int localChargeTicks = 0;

    private static final int ACTIVATION_TIME = 60;


    private static float currentFovMod = 1.0f;
    private static final float TARGET_FOV_MOD = 0.85f;
    private static final float FOV_LERP_SPEED = 0.15f;

    private static final RandomSource random = RandomSource.create();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;


        if (!ClientSkillData.hasSkill("immediate_actions")) {
            if (wasHolding) forceReset();
            return;
        }



        if (ClientSkillData.getSelectedAbility() != SculkAbility.IMMEDIATE_ACTIONS) {
            if (wasHolding) {
                forceReset();
            }
            return;
        }


        boolean isKeyDown = ModKeyBindings.ABILITY_ACTIVATE_KEY.isDown();

        if (isKeyDown) {

            if (!wasHolding) {
                PacketHandler.CHANNEL.sendToServer(new ImmediateActionStatePacket(true));
                wasHolding = true;
                isHolding = true;
                localChargeTicks = 0;
            } else {

                localChargeTicks++;
            }
        } else {

            if (wasHolding) {
                forceReset();
            }
        }


        float target = isHolding ? TARGET_FOV_MOD : 1.0f;


        currentFovMod = Mth.lerp(FOV_LERP_SPEED, currentFovMod, target);


        if (Math.abs(currentFovMod - target) < 0.001f) currentFovMod = target;
    }


    private static void forceReset() {
        if (wasHolding) {
            PacketHandler.CHANNEL.sendToServer(new ImmediateActionStatePacket(false));
        }
        wasHolding = false;
        isHolding = false;
        localChargeTicks = 0;

    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {

        if (currentFovMod != 1.0f) {
            event.setFOV(event.getFOV() * currentFovMod);
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {

        if (isHolding && ClientSkillData.getSelectedAbility() == SculkAbility.IMMEDIATE_ACTIONS) {
            float shakeStrength;


            if (localChargeTicks >= ACTIVATION_TIME) {
                shakeStrength = 0.8f;
            }

            else {
                float progress = (float) localChargeTicks / ACTIVATION_TIME;
                shakeStrength = 0.05f + (progress * 0.25f);
            }


            event.setPitch(event.getPitch() + (random.nextFloat() - 0.5f) * shakeStrength);
            event.setYaw(event.getYaw() + (random.nextFloat() - 0.5f) * shakeStrength);
            event.setRoll(event.getRoll() + (random.nextFloat() - 0.5f) * shakeStrength);
        }
    }
}