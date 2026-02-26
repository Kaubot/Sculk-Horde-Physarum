package net.alekrus.shphysarum.Config;


import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class RaidMusicDisplayOverlay {

    private static int displayTimer = 0;
    private static float slideOffset = 0.0f;

    public static void triggerDisplay() {
        displayTimer = 120; 
        slideOffset = 0.0f;
    }

    
    @Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            if (displayTimer > 0) {
                displayTimer--;

                
                if (displayTimer > 110) {
                    slideOffset = Math.min(1.0f, slideOffset + 0.1f);
                }
                
                else if (displayTimer < 10) {
                    slideOffset = Math.max(0.0f, slideOffset - 0.1f);
                }
                
                else {
                    slideOffset = 1.0f;
                }
            } else {
                slideOffset = 0.0f;
            }
        }
    }

    
    @Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {

            
            event.registerAboveAll("raid_music_display", (gui, graphics, partialTick, width, height) -> {
                if (displayTimer <= 0 && slideOffset <= 0.0f) return;

                Minecraft mc = Minecraft.getInstance();

                
                if (mc.options.hideGui) return;

                String text = "♫ Gunship · Pawel Perepelica";
                int textWidth = mc.font.width(text);
                int boxWidth = textWidth + 24; 

                
                float ease = (float) Math.sin(slideOffset * (Math.PI / 2));

                
                int x = width - (int) (boxWidth * ease);
                int y = 5; 

                RenderSystem.enableBlend();

                
                graphics.fill(x, y, x + boxWidth, y + 18, 0xAA000000);

                
                graphics.drawString(mc.font, text, x + 12, y + 5, 0x00FFFF, false);

                RenderSystem.disableBlend();
            });
        }
    }
}