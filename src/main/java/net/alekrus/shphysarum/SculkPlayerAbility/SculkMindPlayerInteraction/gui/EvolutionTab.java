package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.PointOfNoReturn.InfectionHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.GravemindMainScreen;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.GravemindResources;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.MiniGame.SynapticInvasionScreen;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillBuyPacket;
import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class EvolutionTab extends Screen {

    private final Screen parent;

    private int windowWidth;
    private int windowHeight;
    private int guiLeft;
    private int guiTop;

    private static final ResourceLocation INFESTED_STONE_TEXTURE = ResourceLocation.fromNamespaceAndPath("sculkhorde", "textures/block/infested_stone.png");
    private static final ResourceLocation BORDER_ICON_TEXTURE =  ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/border_icon.png");
    private static final ResourceLocation SKULK_INFECTION_TEXTURE = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, "textures/gui/skulkinfection.png");

    private double scrollX = 0;
    private double scrollY = 0;
    private double zoom = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.0;
    private double minScrollX, maxScrollX, minScrollY, maxScrollY;

    private final List<SkillNode> skills = new ArrayList<>();
    private SkillNode selectedSkill = null;

    public EvolutionTab(Screen parent) {
        super(Component.literal("Evolution Chamber"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.minecraft != null && this.minecraft.player != null) {
            
            
            if (!InfectionHandler.isInfected(this.minecraft.player)) {
                this.onClose();
                return;
            }
        }
        initSkillData();
        calculateScrollLimits();

        this.windowWidth = Math.min(512, this.width - 40);
        this.windowHeight = Math.min(440, this.height - 40);
        this.guiLeft = (this.width - this.windowWidth) / 2;
        this.guiTop = (this.height - this.windowHeight) / 2;
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void calculateScrollLimits() {
        if (skills.isEmpty()) {
            minScrollX = -100; maxScrollX = 100; minScrollY = -100; maxScrollY = 100; return;
        }
        int minX = Integer.MAX_VALUE; int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE; int maxY = Integer.MIN_VALUE;
        for (SkillNode node : skills) {
            if (node.x < minX) minX = node.x; if (node.x > maxX) maxX = node.x;
            if (node.y < minY) minY = node.y; if (node.y > maxY) maxY = node.y;
        }
        int padding = 200;
        this.minScrollX = -(maxX + padding); this.maxScrollX = -(minX - padding);
        this.minScrollY = -(maxY + padding); this.maxScrollY = -(minY - padding);
    }

    private boolean isNodeVisible(SkillNode node) {
        if (ClientSkillData.hasSkill(node.id)) return true;
        if (node.isUltimate) {
            boolean p1 = node.parentId == null || ClientSkillData.hasSkill(node.parentId);
            boolean p2 = node.secondaryParentId == null || ClientSkillData.hasSkill(node.secondaryParentId);
            return p1 && p2;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.minecraft != null && this.minecraft.player != null) {
            
            if (!InfectionHandler.isInfected(this.minecraft.player)) {
                this.onClose();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        graphics.enableScissor(this.guiLeft, this.guiTop, this.guiLeft + this.windowWidth, this.guiTop + this.windowHeight);
        graphics.pose().pushPose();
        graphics.pose().translate(this.guiLeft + (this.windowWidth / 2.0), this.guiTop + (this.windowHeight / 2.0), 0);
        graphics.pose().scale((float) zoom, (float) zoom, 1.0f);
        graphics.pose().translate(scrollX, scrollY, 0);

        renderZoomedBackground(graphics);
        renderConnections(graphics);

        double centerX = this.guiLeft + (this.windowWidth / 2.0);
        double centerY = this.guiTop + (this.windowHeight / 2.0);
        double worldMouseX = (mouseX - centerX) / zoom - scrollX;
        double worldMouseY = (mouseY - centerY) / zoom - scrollY;

        for (SkillNode node : skills) {
            if (!isNodeVisible(node)) continue;
            node.render(graphics, node.x, node.y, worldMouseX, worldMouseY);
        }

        graphics.pose().popPose();
        graphics.disableScissor();

        GravemindMainScreen.renderBorder(graphics, this.guiLeft - 1, this.guiTop - 1, this.windowWidth + 2, this.windowHeight + 2, 0xFF00AAAA);
        renderTabs(graphics, mouseX, mouseY, this.guiLeft, this.guiTop, 1);

        graphics.drawString(this.font, "Evolution Point: " + ClientSkillData.getPoints(), this.guiLeft + 20, this.guiTop + this.windowHeight - 30, 0x55FF55);

        if (selectedSkill == null) {
            graphics.drawCenteredString(this.font, "Scroll to Zoom | Drag to Move", this.guiLeft + (this.windowWidth / 2), this.guiTop + this.windowHeight + 5, 0xAAAAAA);
        }

        if (selectedSkill != null) {
            graphics.fill(0, 0, this.width, this.height, 0x88000000);
            renderSkillPopup(graphics, mouseX, mouseY);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderZoomedBackground(GuiGraphics graphics) {
        int range = 1500;
        int tileSize = 64;
        for (int x = -range; x < range; x += tileSize) {
            for (int y = -range; y < range; y += tileSize) {
                int tileCenterX = x + tileSize / 2;
                int tileCenterY = y + tileSize / 2;

                boolean isInfested = isTileOnInfestedPath(tileCenterX, tileCenterY);
                ResourceLocation texture = isInfested ? INFESTED_STONE_TEXTURE : GravemindResources.STONE_TEXTURE;

                if (isInfested) {
                    RenderSystem.setShaderColor(0.8F, 0.8F, 0.8F, 1.0F);
                } else {
                    RenderSystem.setShaderColor(0.4F, 0.4F, 0.4F, 1.0F);
                }

                RenderSystem.setShaderTexture(0, texture);
                graphics.blit(texture, x, y, 0, 0, tileSize, tileSize, 16, 16);
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private boolean isTileOnInfestedPath(double tileX, double tileY) {
        int skillRadius = 45;
        int pathWidth = 35;

        for (SkillNode node : skills) {
            if (ClientSkillData.hasSkill(node.id)) {
                double dist = Math.sqrt(Math.pow(tileX - (node.x + 13), 2) + Math.pow(tileY - (node.y + 13), 2));
                if (dist < skillRadius) {
                    return true;
                }
            }
        }

        for (SkillNode node : skills) {
            if (!ClientSkillData.hasSkill(node.id)) continue;
            if (node.parentId != null && ClientSkillData.hasSkill(node.parentId)) {
                if (checkLineConnection(node, node.parentId, tileX, tileY, pathWidth)) return true;
            }
            if (node.secondaryParentId != null && ClientSkillData.hasSkill(node.secondaryParentId)) {
                if (checkLineConnection(node, node.secondaryParentId, tileX, tileY, pathWidth)) return true;
            }
        }
        return false;
    }

    private boolean checkLineConnection(SkillNode node, String parentId, double tileX, double tileY, int width) {
        SkillNode parent = skills.stream().filter(n -> n.id.equals(parentId)).findFirst().orElse(null);
        if (parent != null) {
            double x1 = parent.x + 13;
            double y1 = parent.y + 13;
            double x2 = node.x + 13;
            double y2 = node.y + 13;
            double distToLine = getDistToSegment(tileX, tileY, x1, y1, x2, y2);
            return distToLine < width;
        }
        return false;
    }

    private double getDistToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double l2 = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
        if (l2 == 0) return Math.sqrt(Math.pow(px - x1, 2) + Math.pow(py - y1, 2));
        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / l2;
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);
        return Math.sqrt(Math.pow(px - projX, 2) + Math.pow(py - projY, 2));
    }

    private void renderConnections(GuiGraphics graphics) {
        int lineWidth = 4;
        for (SkillNode node : skills) {
            if (!isNodeVisible(node)) continue;
            drawConnection(graphics, node, node.parentId, lineWidth);
            if (node.secondaryParentId != null) {
                drawConnection(graphics, node, node.secondaryParentId, lineWidth);
            }
        }
    }

    private void drawConnection(GuiGraphics graphics, SkillNode node, String parentId, int lineWidth) {
        if (parentId == null) return;
        SkillNode parent = skills.stream().filter(n -> n.id.equals(parentId)).findFirst().orElse(null);
        if (parent != null) {
            float x1 = parent.x + 13;
            float y1 = parent.y + 13;
            float x2 = node.x + 13;
            float y2 = node.y + 13;

            boolean unlocked = ClientSkillData.hasSkill(node.id);
            int color;
            if (node.isUltimate) {
                long time = System.currentTimeMillis();
                float progress = (float) Math.sin(time / 500.0) * 0.5f + 0.5f;
                int r1 = 160, g1 = 32, b1 = 240;
                int r2 = 0, g2 = 0, b2 = 255;
                int r = (int) (r1 + (r2 - r1) * progress);
                int g = (int) (g1 + (g2 - g1) * progress);
                int b = (int) (b1 + (b2 - b1) * progress);
                if (!unlocked) { r /= 3; g /= 3; b /= 3; }
                color = (0xFF << 24) | (r << 16) | (g << 8) | b;
                drawLine(graphics, x1, y1, x2, y2, lineWidth + 2, color);
            } else {
                color = unlocked ? 0xFF00AAAA : 0xFF444444;
                drawLine(graphics, x1, y1, x2, y2, lineWidth, color);
            }
        }
    }

    private void drawLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, int width, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.atan2(dy, dx);
        graphics.pose().pushPose();
        graphics.pose().translate(x1, y1, 0);
        graphics.pose().mulPose(new org.joml.Quaternionf().rotateZ(angle));
        graphics.fill(0, -width / 2, (int) length, width / 2, color);
        graphics.pose().popPose();
    }

    private void renderSkillPopup(GuiGraphics graphics, int mouseX, int mouseY) {
        int popupWidth = Math.min(360, this.width - 20);
        int popupHeight = Math.min(260, this.height - 20);
        int popupX = (this.width - popupWidth) / 2;
        int popupY = (this.height - popupHeight) / 2;

        graphics.fill(popupX, popupY, popupX + popupWidth, popupY + popupHeight, 0xFF101010);
        GravemindMainScreen.renderBorder(graphics, popupX - 1, popupY - 1, popupWidth + 2, popupHeight + 2, 0xFF00AAAA);

        boolean unlocked = ClientSkillData.hasSkill(selectedSkill.id);
        String status = unlocked ? "§a[UNLOCKED]" : "§c[LOCKED]";

        graphics.drawCenteredString(this.font, "§3" + selectedSkill.title + " " + status, this.width / 2, popupY + 10, 0xFFFFFF);
        graphics.drawWordWrap(this.font, Component.literal("§7" + selectedSkill.description), popupX + 10, popupY + 30, popupWidth - 20, 0xCCCCCC);

        int reqY = popupY + popupHeight - 40;
        graphics.drawString(this.font, "§6Requirement:", popupX + 10, reqY, 0xFFAA00);
        String reqText = unlocked ? "Requirement Met." : selectedSkill.obtainMethod;
        int reqColor = unlocked ? 0x55FF55 : 0xFFFFFF;
        graphics.drawWordWrap(this.font, Component.literal(reqText), popupX + 10, reqY + 12, popupWidth - 20, reqColor);

        int btnSize = 14;
        int btnX = popupX + popupWidth - btnSize - 5;
        int btnY = popupY + 5;
        boolean hoverClose = mouseX >= btnX && mouseX <= btnX + btnSize && mouseY >= btnY && mouseY <= btnY + btnSize;
        graphics.fill(btnX, btnY, btnX + btnSize, btnY + btnSize, hoverClose ? 0xFFFF0000 : 0xFF555555);
        graphics.drawCenteredString(this.font, "x", btnX + btnSize / 2, btnY + 3, 0xFFFFFF);

        boolean isAdaptiveMorph = selectedSkill.id.equals("adaptive_morph");

        if (!unlocked || isAdaptiveMorph) {
            boolean parentUnlocked = (selectedSkill.parentId == null || ClientSkillData.hasSkill(selectedSkill.parentId))
                    && (selectedSkill.secondaryParentId == null || ClientSkillData.hasSkill(selectedSkill.secondaryParentId));

            if (parentUnlocked) {
                int unlockBtnW = Math.min(120, popupWidth - 20);
                int unlockBtnH = 20;
                int unlockBtnX = popupX + (popupWidth - unlockBtnW) / 2;
                int unlockBtnY = popupY + popupHeight - 30;
                boolean isHovered = mouseX >= unlockBtnX && mouseX <= unlockBtnX + unlockBtnW && mouseY >= unlockBtnY && mouseY <= unlockBtnY + unlockBtnH;

                int color;
                String btnText;

                if (isAdaptiveMorph) {
                    btnText = unlocked ? "Replay Mini-Game" : "Start Invasion";
                    color = isHovered ? 0xFF00FF00 : 0xFF00AA00;
                } else {
                    if (unlocked) return;
                    int cost = selectedSkill.cost;
                    boolean canAfford = ClientSkillData.getPoints() >= cost;
                    btnText = "Unlock (" + cost + " Pts)";
                    color = canAfford ? (isHovered ? 0xFF00FF00 : 0xFF00AA00) : (isHovered ? 0xFFFF5555 : 0xFFAA0000);
                }

                graphics.fill(unlockBtnX, unlockBtnY, unlockBtnX + unlockBtnW, unlockBtnY + unlockBtnH, color);
                graphics.renderOutline(unlockBtnX, unlockBtnY, unlockBtnW, unlockBtnH, 0xFFFFFFFF);
                graphics.drawCenteredString(this.font, btnText, unlockBtnX + unlockBtnW / 2, unlockBtnY + 6, 0xFFFFFF);
            } else if (!unlocked) {
                graphics.drawCenteredString(this.font, "§cPrerequisite not met", this.width / 2, popupY + popupHeight - 25, 0xFF5555);
            }
        } else {
            graphics.drawCenteredString(this.font, "§aMutation Acquired", this.width / 2, popupY + popupHeight - 25, 0x55FF55);
        }
    }

    protected void renderTabs(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int activeTabID) {
        int startX = x + 20;
        int startY = y + 10;
        int gap = 40;
        drawSingleTab(graphics, GravemindResources.TAB_ICON_DIALOGUE, startX, startY, activeTabID == 0, mouseX, mouseY);
        drawSingleTab(graphics, GravemindResources.TAB_ICON_SKILLTREE,  startX + gap, startY, activeTabID == 1, mouseX, mouseY);
        drawSingleTab(graphics, GravemindResources.TAB_ICON_TASK, startX + gap * 2, startY, activeTabID == 2, mouseX, mouseY);
        drawSingleTab(graphics, GravemindResources.TAB_ICON_SUPPORT,  startX + gap * 3, startY, activeTabID == 3, mouseX, mouseY);
    }

    private void drawSingleTab(GuiGraphics graphics, ResourceLocation icon, int x, int y, boolean isActive, int mouseX, int mouseY) {
        float scale = isActive ? 1.5f : 1.0f;
        boolean hovered = mouseX >= x && mouseX <= x + 20 && mouseY >= y && mouseY <= y + 20;
        if (hovered && !isActive) scale = 1.2f;

        if (isActive) {
            graphics.fill(x - 2, y + 18, x + 18, y + 20, 0xFF00AAAA);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(x + 8, y + 8, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-8, -8, 0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (selectedSkill != null) {
                int popupWidth = Math.min(360, this.width - 20);
                int popupHeight = Math.min(260, this.height - 20);
                int popupX = (this.width - popupWidth) / 2;
                int popupY = (this.height - popupHeight) / 2;

                int closeBtnSize = 14;
                int closeBtnX = popupX + popupWidth - closeBtnSize - 5;
                int closeBtnY = popupY + 5;

                if (mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnSize && mouseY >= closeBtnY && mouseY <= closeBtnY + closeBtnSize) {
                    Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    selectedSkill = null;
                    return true;
                }

                boolean isUnlocked = ClientSkillData.hasSkill(selectedSkill.id);
                boolean parentUnlocked = (selectedSkill.parentId == null || ClientSkillData.hasSkill(selectedSkill.parentId))
                        && (selectedSkill.secondaryParentId == null || ClientSkillData.hasSkill(selectedSkill.secondaryParentId));
                boolean isAdaptiveMorph = selectedSkill.id.equals("adaptive_morph");

                if ((!isUnlocked || isAdaptiveMorph) && parentUnlocked) {
                    int btnW = Math.min(120, popupWidth - 20);
                    int btnH = 20;
                    int btnX = popupX + (popupWidth - btnW) / 2;
                    int btnY = popupY + popupHeight - 30;

                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                        if (isAdaptiveMorph) {
                            Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            this.minecraft.setScreen(new SynapticInvasionScreen(this));
                            return true;
                        }

                        if (!isUnlocked && ClientSkillData.getPoints() >= selectedSkill.cost) {
                            Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
                            PacketHandler.CHANNEL.sendToServer(new SkillBuyPacket(selectedSkill.id, selectedSkill.cost));
                            selectedSkill = null;
                        } else {
                            Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 0.5F));
                        }
                        return true;
                    }
                }

                if (mouseX < popupX || mouseX > popupX + popupWidth || mouseY < popupY || mouseY > popupY + popupHeight) {
                    selectedSkill = null;
                }
                return true;
            }

            int startX = this.guiLeft + 20;
            int startY = this.guiTop + 10;
            int gap = 40;
            int size = 20;

            if (isHovering(startX + (gap * 0), startY, size, size, mouseX, mouseY)) {
                this.minecraft.setScreen(new GravemindMainScreen()); return true;
            }
            if (isHovering(startX + (gap * 2), startY, size, size, mouseX, mouseY)) {
                this.minecraft.setScreen(new TasksTab(this)); return true;
            }
            if (isHovering(startX + (gap * 3), startY, size, size, mouseX, mouseY)) {
                this.minecraft.setScreen(new SupportTab(this)); return true;
            }

            if (mouseX >= this.guiLeft && mouseX <= this.guiLeft + this.windowWidth && mouseY >= this.guiTop && mouseY <= this.guiTop + this.windowHeight) {
                double centerX = this.guiLeft + (this.windowWidth / 2.0);
                double centerY = this.guiTop + (this.windowHeight / 2.0);
                double worldMouseX = (mouseX - centerX) / zoom - scrollX;
                double worldMouseY = (mouseY - centerY) / zoom - scrollY;

                for (SkillNode node : skills) {
                    if (!isNodeVisible(node)) continue;
                    if (worldMouseX >= node.x && worldMouseX <= node.x + 26 &&
                            worldMouseY >= node.y && worldMouseY <= node.y + 26) {
                        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        selectedSkill = node;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (selectedSkill != null) return false;
        double zoomSpeed = 0.1;
        if (delta > 0) zoom += zoomSpeed; else if (delta < 0) zoom -= zoomSpeed;
        zoom = Mth.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (selectedSkill != null) return false;
        if (button == 0) {
            double newScrollX = scrollX + (dragX / zoom);
            double newScrollY = scrollY + (dragY / zoom);
            scrollX = Mth.clamp(newScrollX, minScrollX, maxScrollX);
            scrollY = Mth.clamp(newScrollY, minScrollY, maxScrollY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedSkill != null && (keyCode == GLFW.GLFW_KEY_ESCAPE)) {
            selectedSkill = null; return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose(); return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void initSkillData() {
        if (!skills.isEmpty()) return;

        addSkill("root", null, 0, 0, 0, "Sculk Symbiosis", "Your body is new. You have the benefits and improvements of the horde.", "Already Obtained", GravemindResources.ICON_ROOT);
        addSkill("vision", "root", 100, 0, 5, "Sculk Vision", "See vibrations through walls. Use Ability Menu to toggle.", "Cost: 5 Evolution", GravemindResources.ICON_VISIONS);

        addSkill("essence_extract", "root", 100, 100, 8,
                "Nutrient Extraction",
                "Condense your XP to store.\n" +
                        "§eTap Ability:§7 Create Essence (costs 10 Lvls).\n" +
                        "§eHold Ability:§7 Fill held Essence rapidly from your XP, Hold Essence in your hands.\n" +
                        "§eHold Use (RMB):§7 Consume Essence to regain XP.\n",
                "Cost: 8 Evolution", GravemindResources.ICON_EXPERIENCE);

        addSkill("summoner", "vision", 200, 0, 9, "Horde Caller", "Activate nearby Sculk Summoners to aid you.", "Cost: 9 Evolution", GravemindResources.ICON_SUMMON);
        addSkill("body_fall_1", "root", 80, -80, 6, "Softened Impact", "Your body acts as a cushion. No fall damage up to 10 blocks.", "Cost: 6 Evolution", GravemindResources.ICON_FALLING);

        SkillNode burstNode = new SkillNode("burst", "summoner", 300, 0, 17, "Spore Burst", "Release a violent burst of infective spores.", "Cost: 17 Evolution", GravemindResources.ICON_BURST);
        burstNode.setSecondaryParent("body_fall_1");
        addNode(burstNode);

        addSkill("leap", "burst", 300, -100, 14, "Sculk Leap", "Propel yourself forward using your new legs.", "Cost: 14 Evolution", GravemindResources.ICON_LEAP);
        addSkill("burrow", "leap", 300, -200, 20, "Sculk Burrow", "Merge with the Sculk to move swiftly and unseen. Requires Sculk ground.", "Cost: 20 Evolution", GravemindResources.ICON_BURROW);

        SkillNode ultNode = new SkillNode("adaptive_morph", "burrow", 150, -150, 0, "Brain Damage", "Improve your compatibility with the Horde, allowing you to enhance your body further.\n" + "Complete the Synaptic Invasion to unlock. WARNING: Failure drains XP.", "Complete Mini-Game", GravemindResources.ICON_BRAIN_MORPH);
        ultNode.setSecondaryParent("body_fall_1");
        ultNode.setUltimate();
        addNode(ultNode);

        if (ClientSkillData.hasSkill("structure_insight") || ClientSkillData.hasSkill("adaptive_body_structuring")) {
            addSkill("adaptive_body_structuring", "adaptive_morph", 150, -250, 38, "Adaptive Body Restructuring", "Adapts your body to lethal fauna, allowing you to wield deadly Sculk tentacles.\n"+"§eSHIFT + [Ability Button] §7— Extends Sculk tentacles from your body. Press again to retract them. (Consumes XP while active).\n"+"§e[Ability Button] (Hold)§7 — Deploys tentacles into a defensive stance. Blocks all physical damage without slowing you down.\n" + "§e[Ability Button] (Press)§7 — Performs an area-of-effect attack within a 4-block radius, applying the Sculk Infected effect." , "Cost: 38 Evolution", GravemindResources.ICON_TENTACLE);
        }

        addSkill("raid", "burst", 400, 0, 34, "Scout Initiator", "Send a signal to the Gravemind to scout the territory.", "Cost: 34 Evolution", GravemindResources.ICON_SCOUT);

        addSkill("xp_proc_1", "root", -100, -50, 2, "Metabolism I", "Slightly faster conversion of XP into health.", "Cost: 2 Evolution", GravemindResources.ICON_MET1);
        addSkill("xp_proc_2", "xp_proc_1", -200, -50, 4, "Metabolism II", "Faster healing process.", "Cost: 4 Evolution", GravemindResources.ICON_MET1);
        addSkill("xp_proc_3", "xp_proc_2", -300, -50, 6, "Metabolism III", "Efficient biomass reconstruction.", "Cost: 6 Evolution", GravemindResources.ICON_MET2);
        addSkill("xp_proc_4", "xp_proc_3", -400, -50, 8, "Metabolism IV", "Rapid regeneration.", "Cost: 8 Evolution", GravemindResources.ICON_MET2);
        addSkill("xp_proc_5", "xp_proc_4", -500, -50, 10, "Perfect Metabolism", "Near-instant conversion of XP to health.", "Cost: 10 Evolution", GravemindResources.ICON_MET3);

        addSkill("resistance_1", "root", -100, 50, 3, "Sculk Resistance I", "Purity effect duration -15%.", "Cost: 3 Evolution", GravemindResources.ICON_PURE);
        addSkill("resistance_2", "resistance_1", -200, 50, 5, "Sculk Resistance II", "Purity effect duration -30%.", "Cost: 5 Evolution", GravemindResources.ICON_PURE);
        addSkill("resistance_3", "resistance_2", -300, 50, 7, "Sculk Resistance III", "Purity effect duration -45%.", "Cost: 7 Evolution", GravemindResources.ICON_PURE);
        addSkill("resistance_4", "resistance_3", -400, 50, 9, "Sculk Resistance IV", "Purity effect duration -60%.", "Cost: 9 Evolution", GravemindResources.ICON_PURE);
        addSkill("resistance_5", "resistance_4", -500, 50, 12, "Adaptive Immunity", "Purity effect duration -80%.", "Cost: 12 Evolution", GravemindResources.ICON_PURE);

        addSkill("immediate_actions", "xp_proc_5", -500, -150, 15, "Flesh Shedding", "Discard dead flesh and replace it instantly to gain fast regeneration. \n§eHold Ability Key to activate.\n§cWARNING: You create a shell vulnerability (Incoming Damage x3).", "Cost: 15 Evolution", GravemindResources.ICON_MET4);

        addSkill("unknown_connection_root", "root", 0, 100, 0, "Unknown Connection", "Allows establishing a mental link with basic Sculk units.\n§7Control Limit: 2\n§7Unlocks: Mites, Leech, Salmon, Stinger", "Always Available", GravemindResources.ICON_ARMY1);
        addSkill("connection_t1", "unknown_connection_root", 0, 200, 6, "Cortex Control I", "Allows establishing a mental link with basic Sculk units.\n§a+2 Control Limit\n§7Unlocks: Zombie, Pufferfish, Spitter, Hatcher", "Cost: 6 Evolution", GravemindResources.ICON_ARMY1);
        addSkill("connection_t2", "connection_t1", 0, 300, 8, "Cortex Control II", "Allows establishing a mental link with basic Sculk units.\n§a+2 Control Limit\n§7Unlocks: Creeper, Sheep, Phantom, Vindicator", "Cost: 8 Evolution", GravemindResources.ICON_ARMY2);
        addSkill("connection_t3", "connection_t2", 0, 400, 12, "Cortex Control III", "Allows establishing a mental link with basic Sculk units.\n§a+2 Control Limit\n§7Unlocks: Squid, Ravager, Witch", "Cost: 12 Evolution", GravemindResources.ICON_ARMY2);
        addSkill("connection_t4", "connection_t3", 0, 500, 19, "Apex Connector", "Allows establishing a mental link with basic Sculk units.\n§7Unlocks: Enderman", "Cost: 19 Evolution", GravemindResources.ICON_ARMY3);
    }

    private void addSkill(String id, String parentId, int x, int y, int cost, String title, String desc, String obtain, ResourceLocation icon) {
        addNode(new SkillNode(id, parentId, x, y, cost, title, desc, obtain, icon));
    }

    private void addNode(SkillNode node) {
        skills.add(node);
        ClientSkillData.registerSkillIcon(node.id, node.icon);
    }

    private boolean isHovering(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static class SkillNode {
        String id; String parentId; String secondaryParentId = null; boolean isUltimate = false;
        int x, y; int cost; String title, description, obtainMethod; ResourceLocation icon;

        public SkillNode(String id, String parentId, int x, int y, int cost, String title, String description, String obtainMethod, ResourceLocation icon) {
            this.id = id; this.parentId = parentId; this.x = x; this.y = y; this.cost = cost;
            this.title = title; this.description = description; this.obtainMethod = obtainMethod; this.icon = icon;
        }
        public SkillNode setSecondaryParent(String parentId) { this.secondaryParentId = parentId; return this; }
        public SkillNode setUltimate() { this.isUltimate = true; return this; }

        public void render(GuiGraphics graphics, int x, int y, double worldMouseX, double worldMouseY) {
            int size = 26;
            boolean hovered = worldMouseX >= x && worldMouseX <= x + size && worldMouseY >= y && worldMouseY <= y + size;
            boolean unlocked = ClientSkillData.hasSkill(this.id);

            
            if (unlocked) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int infectSize = 80;
                int offset = (infectSize - size) / 2;
                graphics.blit(SKULK_INFECTION_TEXTURE, x - offset, y - offset, 0, 0, infectSize, infectSize, infectSize, infectSize);
            }

            
            graphics.fill(x, y, x + size, y + size, 0xFF000000);

            
            if (!unlocked) {
                RenderSystem.setShaderColor(0.4F, 0.4F, 0.4F, 0.8F); 
            } else {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
            RenderSystem.enableBlend();
            graphics.blit(icon, x, y, 0, 0, size, size, size, size);

            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            if (isUltimate) {
                long time = System.currentTimeMillis();
                float hue = (time % 2000) / 2000f;
                int borderColor = Mth.hsvToRgb(hue, 1.0f, 1.0f);
                if (!unlocked) borderColor = 0xFF550055; 

                
                float r = ((borderColor >> 16) & 0xFF) / 255.0f;
                float g = ((borderColor >> 8) & 0xFF) / 255.0f;
                float b = (borderColor & 0xFF) / 255.0f;

                RenderSystem.setShaderColor(r, g, b, 1.0F);
                graphics.blit(BORDER_ICON_TEXTURE, x - 2, y - 2, 0, 0, 30, 30, 30, 30);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); 

            } else if (unlocked) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); 
                graphics.blit(BORDER_ICON_TEXTURE, x - 2, y - 2, 0, 0, 30, 30, 30, 30);
            } else {
                if (hovered) {
                    RenderSystem.setShaderColor(0.0F, 1.0F, 1.0F, 1.0F); 
                } else {
                    RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, 1.0F); 
                }
                graphics.blit(BORDER_ICON_TEXTURE, x - 2, y - 2, 0, 0, 30, 30, 30, 30);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); 
            }
        }
    }
}
