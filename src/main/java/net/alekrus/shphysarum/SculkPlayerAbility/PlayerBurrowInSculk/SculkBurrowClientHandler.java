package net.alekrus.shphysarum.SculkPlayerAbility.PlayerBurrowInSculk;

import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class SculkBurrowClientHandler {

    private static boolean isBurrowActive = false;
    private static boolean wasActiveLastTick = false;
    private static boolean originalBobbing = true;
    private static final UUID STEP_UUID = UUID.fromString("e0f669a4-226e-4122-ad00-069016140076");
    private static final AttributeModifier STEP_MODIFIER = new AttributeModifier(
            STEP_UUID, "Burrow Step Height", 1.0, AttributeModifier.Operation.ADDITION
    );

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        isBurrowActive = false;
        wasActiveLastTick = false;
    }

    public static boolean isBurrowActive() { return isBurrowActive; }
    public static void setBurrowActive(boolean active) { isBurrowActive = active; }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (isBurrowActive) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (isBurrowActive && event.getEntity().equals(Minecraft.getInstance().player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        AttributeInstance stepAttribute = mc.player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());

        if (isBurrowActive) {
            if (!wasActiveLastTick) {
                originalBobbing = mc.options.bobView().get();
                mc.options.bobView().set(false);
                wasActiveLastTick = true;
            }
            if (mc.options.bobView().get()) mc.options.bobView().set(false);

            if (stepAttribute != null && !stepAttribute.hasModifier(STEP_MODIFIER)) {
                stepAttribute.addTransientModifier(STEP_MODIFIER);
            }
            if (mc.player.getPose() != Pose.SWIMMING) mc.player.setForcedPose(Pose.SWIMMING);

            if (mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0) {
                Vec3 motion = mc.player.getDeltaMovement();
                double speedBoost = 1.25;
                mc.player.setDeltaMovement(motion.x * speedBoost, motion.y, motion.z * speedBoost);
            }

            if (!mc.player.onGround() && !mc.player.horizontalCollision && !mc.player.input.jumping) {
                Vec3 motion = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(motion.x, -2.0, motion.z);
            }

            if (mc.player.horizontalCollision) {
                Vec3 motion = mc.player.getDeltaMovement();
                double climbSpeed = mc.player.isShiftKeyDown() ? 0.0 : 0.3;
                mc.player.setDeltaMovement(motion.x, climbSpeed, motion.z);
                mc.player.fallDistance = 0;
            }
        } else {
            if (wasActiveLastTick) {
                mc.options.bobView().set(originalBobbing);
                if (stepAttribute != null && stepAttribute.hasModifier(STEP_MODIFIER)) {
                    stepAttribute.removeModifier(STEP_MODIFIER);
                }
                mc.player.setForcedPose(null);
                wasActiveLastTick = false;
            }
        }
    }
}
