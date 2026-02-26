package net.alekrus.shphysarum.CustomGui.PlayerMenu;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.KeyBind.ModKeyBindings;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkAbility;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.GravemindResources;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class SculkAbilityRadialMenu extends Screen {

    public SculkAbilityRadialMenu() {
        super(Component.literal("Sculk Ability Selector"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }



    public static ResourceLocation getIconForAbility(SculkAbility ability) {
        if (ability == null || ability == SculkAbility.NONE) return GravemindResources.ICON_DEFAULT;

        String id = ability.getSkillId();
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
            case "adaptive_body_structuring" -> GravemindResources.ICON_TENTACLE;
            default -> GravemindResources.ICON_DEFAULT;
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long windowHandle = Minecraft.getInstance().getWindow().getWindow();
        int keyCode = ModKeyBindings.ABILITY_MENU_KEY.getKey().getValue();

        if (!InputConstants.isKeyDown(windowHandle, keyCode)) {
            this.onClose();
            return;
        }

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        double radius = 90;

        graphics.fill(0, 0, this.width, this.height, 0xDD000A10);

        List<SculkAbility> unlockedAbilities = new ArrayList<>();
        for (SculkAbility ability : SculkAbility.values()) {
            if (ability != SculkAbility.NONE && ClientSkillData.hasSkill(ability.getSkillId())) {
                unlockedAbilities.add(ability);
            }
        }

        int count = unlockedAbilities.size();

        if (count == 0) {
            graphics.drawCenteredString(this.font, "§3No active mutations unlocked yet.", centerX, centerY - 6, 0x00AAAA);
            graphics.drawCenteredString(this.font, "§8Evolve your body and mind to gain powers.", centerX, centerY + 6, 0x005555);
            return;
        }

        double angleStep = 360.0 / count;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;

        for (int i = 0; i < count; i++) {
            SculkAbility ability = unlockedAbilities.get(i);

            double itemAngle = i * angleStep;
            double rad = Math.toRadians(itemAngle - 90);
            int itemX = (int) (centerX + Math.cos(rad) * radius);
            int itemY = (int) (centerY + Math.sin(rad) * radius);

            boolean isHovered = false;

            if (distance > 20) {
                double diff = Math.abs(angle - itemAngle);
                if (diff > 180) diff = 360 - diff;
                if (diff < (angleStep / 2)) {
                    isHovered = true;
                    ClientSkillData.setSelectedAbility(ability);
                }
            }

            boolean isSelected = ClientSkillData.getSelectedAbility() == ability;

            int iconSize = 24;
            int half = iconSize / 2;

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            if (isSelected || isHovered) {
                RenderSystem.setShaderColor(0.0F, 1.0F, 1.0F, 1.0F);

                graphics.blit(GravemindResources.ICON_BORDER_LIGHT, itemX - half - 4, itemY - half - 4, 0, 0, iconSize + 8, iconSize + 8, 30, 30);
            } else {
                RenderSystem.setShaderColor(0.0F, 0.4F, 0.4F, 0.6F);
                graphics.blit(GravemindResources.ICON_BORDER, itemX - half - 2, itemY - half - 2, 0, 0, iconSize + 4, iconSize + 4, 30, 30);
            }

            ResourceLocation icon = getIconForAbility(ability);

            if (isSelected) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                RenderSystem.setShaderColor(0.6F, 0.8F, 0.8F, 0.8F);
            }

            graphics.fill(itemX - half, itemY - half, itemX + half, itemY + half, 0xFF050B10);
            graphics.blit(icon, itemX - half, itemY - half, 0, 0, iconSize, iconSize, iconSize, iconSize);

            int textColor = isSelected ? 0x00FFFF : 0x008888;
            int textYOffset = (itemY > centerY) ? half + 4 : -half - 12;

            graphics.drawCenteredString(this.font, ability.getDisplayName(), itemX, itemY + textYOffset, textColor);
        }

        SculkAbility selected = ClientSkillData.getSelectedAbility();
        String currentName = (selected == SculkAbility.NONE || selected == null) ? "No Selection" : selected.getDisplayName();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.drawCenteredString(this.font, "Selected Ability", centerX, centerY - 10, 0x005555);
        graphics.drawCenteredString(this.font, currentName, centerX, centerY, 0x00FFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
