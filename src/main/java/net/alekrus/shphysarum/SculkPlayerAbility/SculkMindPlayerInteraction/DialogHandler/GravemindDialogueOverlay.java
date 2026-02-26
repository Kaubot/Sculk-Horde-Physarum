package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class GravemindDialogueOverlay {

    private static final ResourceLocation BG_TEXTURE = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/hudtalk.png");

    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!ClientGravemindState.isOverlayActive) return;
        if (!InfectionHandler.isClientInfected(mc.player)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int ticks = ClientGravemindState.displayTicks;

        
        double currentScale = mc.getWindow().getGuiScale();
        double scaleFactor = 2.0 / currentScale;

        int finalWidth = (int) (180 * scaleFactor);
        int finalHeight = (int) (80 * scaleFactor);

        
        
        
        
        

        float slideProgress = 0f;

        if (ticks < 12) {
            
            float t = ticks / 12f;
            slideProgress = 1f - (1f - t) * (1f - t);
        } else if (ticks > 58) {
            
            float t = (70 - ticks) / 12f;
            if (t < 0) t = 0;
            slideProgress = t * t;
        } else {
            
            slideProgress = 1f;
        }

        int startX = -finalWidth;
        int endX = (int) (10 * scaleFactor);
        int currentX = (int) Mth.lerp(slideProgress, startX, endX);
        int currentY = mc.getWindow().getGuiScaledHeight() / 3;

        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BG_TEXTURE);
        graphics.blit(BG_TEXTURE, currentX, currentY, 0, 0, finalWidth, finalHeight, finalWidth, finalHeight);

        
        String fullText = ClientGravemindState.currentMessage;
        int textLength = fullText.length();

        
        int charPerTick = 2;
        int charsToShow = Math.min(textLength, (ticks * charPerTick));
        String textToRender = fullText.substring(0, charsToShow);

        int textPadX = (int) (15 * scaleFactor);
        int textPadY = (int) (35 * scaleFactor);
        int wrapWidth = finalWidth - (int)(20 * scaleFactor);

        graphics.drawWordWrap(mc.font, net.minecraft.network.chat.Component.literal(textToRender), currentX + textPadX, currentY + textPadY, wrapWidth, 0x00AAAA);

        RenderSystem.disableBlend();
    }
}
