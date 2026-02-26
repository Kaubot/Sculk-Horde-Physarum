package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction;

import net.alekrus.shphysarum.shPhysarum;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkAbility;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(modid = shPhysarum.MODID, value = Dist.CLIENT)
public class GravemindHudOverlay {

    private static final int MAIN_ICON_SIZE = 32;
    private static final int ABILITY_ICON_SIZE = 24; 
    private static final int ABILITY_SPACING = 36; 

    @SubscribeEvent
    public static void renderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!InfectionHandler.isClientInfected(mc.player)) return;

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        GuiGraphics graphics = event.getGuiGraphics();

        renderMainIcon(mc, width, height, graphics);

        
        renderActiveAbilities(mc, width, height, graphics);
    }

    private static void renderMainIcon(Minecraft mc, int width, int height, GuiGraphics graphics) {
        ResourceLocation textureToRender;
        if (ClientGravemindState.hasUnreadDialogue()) {
            textureToRender = GravemindResources.ICON_TALK;
        } else {
            textureToRender = GravemindResources.ICON_DEFAULT;
        }

        int x = (width / 2) - (MAIN_ICON_SIZE / 2);
        int y = height - 55;
        if (mc.player.isCreative()) y = height - 55;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        
        graphics.blit(textureToRender, x, y, 0, 0, MAIN_ICON_SIZE, MAIN_ICON_SIZE, MAIN_ICON_SIZE, MAIN_ICON_SIZE);

        RenderSystem.disableBlend();
    }

    
    private static ResourceLocation getIconForActiveId(String id) {
        if (id == null) return GravemindResources.ICON_DEFAULT;

        
        return switch (id) {
            case "essence_extract" -> GravemindResources.ICON_EXPERIENCE;
            case "vision" -> GravemindResources.ICON_VISIONS;
            case "summoner" -> GravemindResources.ICON_SUMMON;
            case "burst" -> GravemindResources.ICON_BURST;
            case "raid" -> GravemindResources.ICON_SCOUT;
            case "leap" -> GravemindResources.ICON_LEAP;
            case "burrow" -> GravemindResources.ICON_BURROW;
            case "immediate_actions" -> GravemindResources.ICON_MET4;
            
            case "sharp_tentacle", "adaptive_body_structuring" -> GravemindResources.ICON_TENTACLE;
            default -> {
                ResourceLocation fromData = ClientSkillData.getIcon(id);
                yield fromData != null ? fromData : GravemindResources.ICON_DEFAULT;
            }
        };
    }

    private static void renderActiveAbilities(Minecraft mc, int width, int height, GuiGraphics graphics) {
        Set<String> activeSet = ClientSkillData.getActiveAbilities();
        if (activeSet.isEmpty()) return;

        List<String> abilitiesToRender = new ArrayList<>(activeSet);

        int totalHeight = abilitiesToRender.size() * ABILITY_SPACING;
        int startY = (height / 2) - (totalHeight / 2);
        int x = width - ABILITY_ICON_SIZE - 20; 

        SculkAbility selected = ClientSkillData.getSelectedAbility();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int i = 0; i < abilitiesToRender.size(); i++) {
            String activeSkillId = abilitiesToRender.get(i); 
            int y = startY + (i * ABILITY_SPACING);

            
            ResourceLocation icon = getIconForActiveId(activeSkillId);

            
            boolean isSelected = false;
            if (selected != null && selected != SculkAbility.NONE) {
                String selectedSkillId = selected.getSkillId();
                if (activeSkillId.equals(selectedSkillId) ||
                        (activeSkillId.equals("sharp_tentacle") && selectedSkillId.equals("adaptive_body_structuring"))) {
                    isSelected = true;
                }
            }

            int borderSize = 32;
            int offset = (borderSize - ABILITY_ICON_SIZE) / 2; 

            
            if (isSelected) {
                RenderSystem.setShaderColor(0.0F, 1.0F, 1.0F, 1.0F); 
                graphics.blit(GravemindResources.ICON_BORDER_LIGHT, x - offset, y - offset, 0, 0, borderSize, borderSize, 30, 30);
            } else {
                RenderSystem.setShaderColor(0.0F, 0.4F, 0.4F, 0.7F); 
                graphics.blit(GravemindResources.ICON_BORDER, x - offset, y - offset, 0, 0, borderSize, borderSize, 30, 30);
            }

            
            if (isSelected) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); 
            } else {
                RenderSystem.setShaderColor(0.6F, 0.8F, 0.8F, 0.8F); 
            }

            graphics.fill(x, y, x + ABILITY_ICON_SIZE, y + ABILITY_ICON_SIZE, 0xFF050B10); 
            graphics.blit(icon, x, y, 0, 0, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE, ABILITY_ICON_SIZE);

            
            
            String name = activeSkillId.substring(0, 1).toUpperCase() + activeSkillId.substring(1).replace("_", " ");

            
            if (activeSkillId.equals("sharp_tentacle")) name = "Tentacles Active";

            activeString(graphics, mc, name, x, y, isSelected);
        }

        RenderSystem.disableBlend();
    }

    private static void activeString(GuiGraphics graphics, Minecraft mc, String text, int x, int y, boolean selected) {
        
        int color = selected ? 0x00FFFF : 0x00AAAA;
        graphics.drawString(mc.font, text, x - mc.font.width(text) - 12, y + 8, color);
    }
}
