package net.alekrus.shphysarum.SculkPlayerAbility.SculkVision;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class SculkBlinkVision {

    
    
    private static final ResourceLocation PULSE_TEXTURE = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/pulse.png");

    
    private static final int ANIMATION_FRAMES = 8;

    
    private static final int ANIMATION_SPEED = 2;

    
    private static final float PULSE_SIZE = 2.5f;
    

    private static boolean active = false;
    private static boolean blinking = false;
    private static int blinkTicks = 0;
    private static final int BLINK_DURATION = 6;
    private static final int BLINK_HALF = BLINK_DURATION / 2;

    private static float fadeProgress = 0.0f;
    private static final float FADE_SPEED = 0.05f;
    private static float pulseTime = 0.0f;

    private static final List<PulseInstance> activePulses = new ArrayList<>();
    
    private static final List<Integer> cooldownList = new ArrayList<>();

    private static int loadBalanceCounter = 0;

    public static void toggleVision() {
        if (!blinking) {
            blinking = true;
            blinkTicks = 0;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (active && !InfectionHandler.isClientInfected(mc.player)) {
            toggleOff(mc);
            PacketHandler.CHANNEL.sendToServer(new SculkVisionStatePacket(false));
            return;
        }

        handleBlinkAndFade(mc);

        if (active) {
            if (mc.player.tickCount % 20 == 0) {
                mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
            }
            pulseTime += 0.05f;

            updateDetectionLogic(mc);
            updatePulses();
        } else {
            pulseTime = 0;
            activePulses.clear();
            cooldownList.clear();
        }
    }

    private static void handleBlinkAndFade(Minecraft mc) {
        if (active) {
            if (fadeProgress < 1.0f) {
                fadeProgress += FADE_SPEED;
                if (fadeProgress > 1.0f) fadeProgress = 1.0f;
            }
        } else {
            if (fadeProgress > 0.0f) {
                fadeProgress -= FADE_SPEED;
                if (fadeProgress < 0.0f) fadeProgress = 0.0f;
            }
        }

        if (blinking) {
            blinkTicks++;
            if (blinkTicks == BLINK_HALF) {
                if (active) {
                    toggleOff(mc);
                    PacketHandler.CHANNEL.sendToServer(new SculkVisionStatePacket(false));
                } else {
                    active = true;
                    PacketHandler.CHANNEL.sendToServer(new SculkVisionStatePacket(true));
                }
            }
            if (blinkTicks >= BLINK_DURATION) {
                blinking = false;
                blinkTicks = 0;
            }
        }
    }

    private static void toggleOff(Minecraft mc) {
        active = false;
        mc.player.removeEffect(MobEffects.NIGHT_VISION);
        activePulses.clear();
        cooldownList.clear();
    }

    private static void updatePulses() {
        Iterator<PulseInstance> iterator = activePulses.iterator();
        while (iterator.hasNext()) {
            PulseInstance pulse = iterator.next();
            pulse.age++;

            
            if (pulse.age >= ANIMATION_FRAMES * ANIMATION_SPEED) {
                iterator.remove();
                cooldownList.remove((Integer) pulse.entityId);
            }
        }
    }

    private static void updateDetectionLogic(Minecraft mc) {
        loadBalanceCounter++;
        int currentGroup = loadBalanceCounter % 8;

        int entityIndex = 0;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || entity instanceof ISculkSmartEntity) continue;
            if (!(entity instanceof LivingEntity)) continue;

            if (cooldownList.contains(entity.getId())) continue;

            if (entityIndex % 8 != currentGroup) {
                entityIndex++;
                continue;
            }
            entityIndex++;

            if (shouldTriggerSculkSensor((LivingEntity) entity, mc)) {
                
                Vec3 pos = entity.position().add(0, entity.getBbHeight() * 0.5 + 0.5, 0);
                activePulses.add(new PulseInstance(entity.getId(), pos));
                cooldownList.add(entity.getId());
            }
        }
    }

    private static boolean shouldTriggerSculkSensor(LivingEntity entity, Minecraft mc) {
        if (entity.isCrouching()) return false;
        double dx = entity.getX() - entity.xo;
        double dy = entity.getY() - entity.yo;
        double dz = entity.getZ() - entity.zo;
        if (dx * dx + dy * dy + dz * dz < 0.0001) return false;

        BlockPos pos = entity.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        int rangeXZ = 7;
        int rangeY = 4;

        for (int x = -rangeXZ; x <= rangeXZ; x++) {
            for (int y = -rangeY; y <= rangeY; y++) {
                for (int z = -rangeXZ; z <= rangeXZ; z++) {
                    mutablePos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    BlockState state = mc.level.getBlockState(mutablePos);
                    if (state.is(Blocks.SCULK_SENSOR) || state.is(Blocks.SCULK_SHRIEKER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (!active || activePulses.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); 
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PULSE_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        Quaternionf cameraRotation = event.getCamera().rotation();

        for (PulseInstance pulse : activePulses) {
            double relX = pulse.position.x - cameraPos.x;
            double relY = pulse.position.y - cameraPos.y;
            double relZ = pulse.position.z - cameraPos.z;

            poseStack.pushPose();
            poseStack.translate(relX, relY, relZ);
            poseStack.mulPose(cameraRotation);

            
            
            int currentFrame = (pulse.age / ANIMATION_SPEED) % ANIMATION_FRAMES;

            
            float vMin = (float) currentFrame / ANIMATION_FRAMES;
            float vMax = (float) (currentFrame + 1) / ANIMATION_FRAMES;

            float size = PULSE_SIZE;
            Matrix4f matrix = poseStack.last().pose();

            
            buffer.vertex(matrix, -size / 2, size / 2, 0).uv(0.0F, vMin).endVertex();
            buffer.vertex(matrix, -size / 2, -size / 2, 0).uv(0.0F, vMax).endVertex();
            buffer.vertex(matrix, size / 2, -size / 2, 0).uv(1.0F, vMax).endVertex();
            buffer.vertex(matrix, size / 2, size / 2, 0).uv(1.0F, vMin).endVertex();

            poseStack.popPose();
        }

        tesselator.end();

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!InfectionHandler.isClientInfected(mc.player)) return;
        if (fadeProgress <= 0.0f && !blinking) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        GuiGraphics graphics = event.getGuiGraphics();

        if (fadeProgress > 0.0f) {
            float normPulse = (float) (Math.sin(pulseTime) + 1.0f) / 2.0f;
            int minAlpha = 4; int maxAlpha = 8;
            float baseAlpha = minAlpha + (normPulse * (maxAlpha - minAlpha));
            int finalAlpha = (int) (baseAlpha * fadeProgress);
            if (finalAlpha > 255) finalAlpha = 255;
            int color = (finalAlpha << 24) | 0x001a33;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            graphics.fill(0, 0, width, height, color);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }

        if (blinking) {
            float progress;
            if (blinkTicks <= BLINK_HALF) progress = (float) blinkTicks / BLINK_HALF;
            else progress = 1.0f - ((float) (blinkTicks - BLINK_HALF) / BLINK_HALF);
            progress = progress * progress * (3.0F - 2.0F * progress);

            int lidHeight = (int) ((height / 2) * progress);
            int blackColor = 0xFF000000;
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            graphics.fill(0, 0, width, lidHeight, blackColor);
            graphics.fill(0, height - lidHeight, width, height, blackColor);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private static class PulseInstance {
        int entityId;
        Vec3 position;
        int age;
        public PulseInstance(int entityId, Vec3 position) {
            this.entityId = entityId;
            this.position = position;
            this.age = 0;
        }
    }
}
