package net.alekrus.shphysarum.ItemsAndTab.Items;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class SculkTransformationClientHandler {


    private static int transformationTicks = 0;
    private static int overlayTicks = 0;

    private static final int MAX_PARTICLE_TICKS = 200;
    private static final int MAX_OVERLAY_TICKS = 160;

    private static final RandomSource random = RandomSource.create();


    public static void triggerTransformation() {
        transformationTicks = MAX_PARTICLE_TICKS;
        overlayTicks = MAX_OVERLAY_TICKS;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;


        if (transformationTicks > 0) {
            transformationTicks--;


            int particleCount = 2 + random.nextInt(3);

            for (int i = 0; i < particleCount; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 1.5;
                double offsetY = random.nextDouble() * 2.0;
                double offsetZ = (random.nextDouble() - 0.5) * 1.5;


                mc.level.addParticle(ParticleTypes.SCULK_SOUL,
                        mc.player.getX() + offsetX,
                        mc.player.getY() + offsetY,
                        mc.player.getZ() + offsetZ,
                        0, 0.05, 0);

                if (random.nextFloat() < 0.3f) {
                    mc.level.addParticle(ParticleTypes.SCULK_CHARGE_POP,
                            mc.player.getX() + offsetX,
                            mc.player.getY() + offsetY,
                            mc.player.getZ() + offsetZ,
                            (random.nextDouble() - 0.5) * 0.1,
                            (random.nextDouble() - 0.5) * 0.1,
                            (random.nextDouble() - 0.5) * 0.1);
                }
            }
        }


        if (overlayTicks > 0) {
            overlayTicks--;
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {

        if (overlayTicks > 0) {
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            GuiGraphics graphics = event.getGuiGraphics();


            float time = (MAX_OVERLAY_TICKS - overlayTicks) * 0.5f;

            float alphaPulse = (Mth.sin(time) + 1.0f) * 0.5f;


            float fadeOut = 1.0f;
            if (overlayTicks < 20) {
                fadeOut = overlayTicks / 20.0f;
            }

            int alpha = (int) (60 * alphaPulse * fadeOut);

            int color = (alpha << 24) | (0 << 16) | (50 << 8) | 100;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            graphics.fill(0, 0, width, height, color);

            RenderSystem.disableBlend();
        }
    }
}