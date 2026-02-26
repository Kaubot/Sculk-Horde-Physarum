package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.GravemindMainScreen;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.GravemindResources;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.SupportTabInteraction.SupportPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.alekrus.shphysarum.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportTab extends Screen {

    private final Screen parent;

    
    private int windowWidth;
    private int windowHeight;
    private int guiLeft;
    private int guiTop;

    private final List<SupportOption> supportOptions = new ArrayList<>();
    private SupportOption selectedOption = null;

    
    private BlockPos selectedAnchor = null;
    private int anchorScrollOffset = 0;
    private static final int ANCHOR_ITEM_HEIGHT = 25;

    private Button actionButton;
    private String statusMessage = "";

    public SupportTab(Screen parent) {
        super(Component.literal("Support Request"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        initSupportOptions();

        
        this.windowWidth = Math.min(512, this.width - 40);
        this.windowHeight = Math.min(440, this.height - 40);
        this.guiLeft = (this.width - this.windowWidth) / 2;
        this.guiTop = (this.height - this.windowHeight) / 2;
        int centerX = this.guiLeft + (this.windowWidth / 2);

        
        int btnY = this.guiTop + 60;
        int btnX = this.guiLeft + 20;
        int btnW = (this.windowWidth / 2) - 40; 

        for (SupportOption option : supportOptions) {
            this.addRenderableWidget(Button.builder(Component.literal(option.title), (b) -> {
                selectOption(option);
            }).bounds(btnX, btnY, btnW, 20).build());
            btnY += 25;
        }

        
        
        int executeBtnW = 140;
        int executeBtnX = centerX + ((this.windowWidth / 2) - executeBtnW) / 2;
        int executeBtnY = this.guiTop + this.windowHeight - 35; 

        actionButton = Button.builder(Component.literal("Execute"), (b) -> {
            performAction();
        }).bounds(executeBtnX, executeBtnY, executeBtnW, 20).build();
        actionButton.visible = false;
        this.addRenderableWidget(actionButton);
    }

    private void selectOption(SupportOption option) {
        this.selectedOption = option;
        this.statusMessage = "";

        
        if (option.id == 1) { 
            List<BlockPos> anchors = ClientSkillData.getAnchors();
            if (!anchors.isEmpty()) {
                selectedAnchor = anchors.get(anchors.size() - 1);
            } else {
                selectedAnchor = null;
            }
        }

        updateActionButton();
        actionButton.visible = true;
    }

    private void updateActionButton() {
        if (selectedOption == null) return;
        String text = selectedOption.buttonText + " (" + selectedOption.cost + " Faith)";
        actionButton.setMessage(Component.literal(text));

        
        if (selectedOption.id == 1 && selectedAnchor == null) {
            actionButton.active = false;
        } else {
            actionButton.active = true;
        }
    }

    private void performAction() {
        if (selectedOption == null) return;

        if (ClientGravemindState.getFaith() < selectedOption.cost) {
            statusMessage = "§cInsufficient Faith!";
            Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0F));
            return;
        }

        BlockPos targetPos = BlockPos.ZERO;

        
        if (selectedOption.id == 1) {
            if (selectedAnchor == null) return;
            targetPos = selectedAnchor;
        }

        PacketHandler.CHANNEL.sendToServer(new SupportPacket(selectedOption.id, targetPos));
        ClientGravemindState.addFaith(-selectedOption.cost);
        statusMessage = "§aRequest Sent: " + selectedOption.title;
        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int centerX = this.guiLeft + (this.windowWidth / 2);

        renderWindowBackground(graphics, this.guiLeft, this.guiTop);

        
        graphics.fill(centerX - 1, this.guiTop + 40, centerX + 1, this.guiTop + this.windowHeight - 20, 0xFF00AAAA);
        GravemindMainScreen.renderBorder(graphics, this.guiLeft - 1, this.guiTop - 1, this.windowWidth + 2, this.windowHeight + 2, 0xFF00AAAA);

        renderTabs(graphics, mouseX, mouseY, this.guiLeft, this.guiTop, 3);

        graphics.drawCenteredString(this.font, "§6Available Options", this.guiLeft + (this.windowWidth / 4), this.guiTop + 45, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§3Operation Details", this.guiLeft + (this.windowWidth / 4 * 3), this.guiTop + 45, 0xFFFFFF);
        graphics.drawString(this.font, "Faith: " + ClientGravemindState.getFaith(), this.guiLeft + 20, this.guiTop + this.windowHeight - 20, 0x55FFFF, false);

        if (selectedOption != null) {
            int rightX = centerX + 20;
            int rightY = this.guiTop + 60;
            int rightW = (this.windowWidth / 2) - 40; 

            
            graphics.drawCenteredString(this.font, "§e" + selectedOption.title, centerX + (this.windowWidth / 4), rightY, 0xFFFFFF);

            
            graphics.drawWordWrap(this.font, Component.literal("§7" + selectedOption.description), rightX, rightY + 20, rightW, 0xCCCCCC);

            
            if (selectedOption.id == 1) {
                renderAnchorList(graphics, rightX, rightY + 50, rightW, mouseX, mouseY);
            }

            
            if (!statusMessage.isEmpty()) {
                graphics.drawCenteredString(this.font, statusMessage, centerX + (this.windowWidth / 4), this.guiTop + this.windowHeight - 55, 0xFFFFFF);
            }

        } else {
            graphics.drawCenteredString(this.font, "Select an operation", centerX + (this.windowWidth / 4), this.guiTop + (this.windowHeight / 2), 0x555555);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderAnchorList(GuiGraphics graphics, int x, int y, int width, int mouseX, int mouseY) {
        List<BlockPos> anchors = ClientSkillData.getAnchors();
        List<BlockPos> reversedAnchors = new ArrayList<>(anchors);
        Collections.reverse(reversedAnchors); 

        
        int maxListHeight = this.windowHeight - (y - this.guiTop) - 60;
        int listHeight = Math.max(50, maxListHeight);

        graphics.fill(x, y, x + width, y + listHeight, 0x44000000); 
        GravemindMainScreen.renderBorder(graphics, x, y, width, listHeight, 0xFF555555);

        if (anchors.isEmpty()) {
            graphics.drawCenteredString(this.font, "No Soul Anchors linked.", x + width / 2, y + listHeight / 2 - 5, 0x888888);
            return;
        }

        graphics.enableScissor(x, y + 1, x + width, y + listHeight - 1);
        graphics.pose().pushPose();
        graphics.pose().translate(0, -anchorScrollOffset, 0);

        int curY = y + 5;
        for (BlockPos pos : reversedAnchors) {
            boolean isSelected = pos.equals(selectedAnchor);
            boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= curY - anchorScrollOffset && mouseY <= curY + ANCHOR_ITEM_HEIGHT - anchorScrollOffset;

            int bgColor = isSelected ? 0xFF00AAAA : (isHovered ? 0xFF444444 : 0xFF222222);
            graphics.fill(x + 2, curY, x + width - 2, curY + ANCHOR_ITEM_HEIGHT - 2, bgColor);

            String txt = "Anchor: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
            graphics.drawString(this.font, txt, x + 8, curY + 6, 0xFFFFFF);

            
            if (isHovered && Minecraft.getInstance().mouseHandler.isLeftPressed()) {
                selectedAnchor = pos;
                updateActionButton();
            }

            curY += ANCHOR_ITEM_HEIGHT;
        }

        graphics.pose().popPose();
        graphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (selectedOption != null && selectedOption.id == 1) { 
            anchorScrollOffset = (int) Math.max(0, anchorScrollOffset - delta * 20);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    protected void renderTabs(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int activeTabID) {
        int startX = x + 20; int startY = y + 10; int gap = 40;

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
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);
        graphics.pose().popPose();
    }

    private void renderWindowBackground(GuiGraphics graphics, int x, int y) {
        int tileSize = 32;
        RenderSystem.setShaderTexture(0, GravemindResources.SCULK_BLOCK_TEXTURE);
        RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        graphics.enableScissor(x, y, x + this.windowWidth, y + this.windowHeight);
        for (int i = 0; i < this.windowWidth; i += tileSize) {
            for (int j = 0; j < this.windowHeight; j += tileSize) {
                graphics.blit(GravemindResources.SCULK_BLOCK_TEXTURE, x + i, y + j, 0, 0, tileSize, tileSize, 16, 16);
            }
        }
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = this.guiLeft + 20; int startY = this.guiTop + 10; int gap = 40; int size = 20;

        
        if (isHovering(startX, startY, size, size, mouseX, mouseY)) { this.minecraft.setScreen(new GravemindMainScreen()); return true; }
        if (isHovering(startX + gap, startY, size, size, mouseX, mouseY)) { this.minecraft.setScreen(new EvolutionTab(new GravemindMainScreen())); return true; }
        if (isHovering(startX + (gap * 2), startY, size, size, mouseX, mouseY)) { this.minecraft.setScreen(new TasksTab(new GravemindMainScreen())); return true; }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isHovering(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose(); return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void initSupportOptions() {
        if (!supportOptions.isEmpty()) return;

        supportOptions.add(new SupportOption(0, "Spore Delivery", "Courier delivers a Sculk Spore Spewer.", "Order", 11, GravemindResources.ICON_DEFAULT));
        supportOptions.add(new SupportOption(4, "Creeper Squad", "Courier delivers 3 Sculk Creepers.", "Order", 8, GravemindResources.ICON_TALK));
        supportOptions.add(new SupportOption(2, "Skeleton Squad", "Summons reinforced skeletons.", "Summon", 4, GravemindResources.ICON_TALK));
        supportOptions.add(new SupportOption(3, "Witch Support", "Calls for a Witch support.", "Call", 3, GravemindResources.ICON_TALK));
        supportOptions.add(new SupportOption(1, "Biomass Warp", "Traverse the network to a linked Soul Anchor.", "Warp", 5, GravemindResources.ICON_DEFAULT));
    }

    private static class SupportOption {
        int id;
        String title;
        String description;
        String buttonText;
        int cost;
        ResourceLocation icon;

        public SupportOption(int id, String title, String description, String buttonText, int cost, ResourceLocation icon) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.buttonText = buttonText;
            this.cost = cost;
            this.icon = icon;
        }
    }
}
