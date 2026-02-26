package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.GravemindMainScreen;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.GravemindResources;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.GravemindTask;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.TaskActionPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.TaskManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class TasksTab extends Screen {
    private final Screen parent;

    
    private int windowWidth;
    private int windowHeight;
    private int guiLeft;
    private int guiTop;

    private ItemStack gravemindHead;
    private Button completeButton;
    private Button cancelButton;
    private List<GravemindTask> availableOptions;
    private double scrollOffset = 0;

    
    private static final int ITEM_HEIGHT = 50;

    public TasksTab(Screen parent) {
        super(Component.literal("Gravemind Tasks"));
        this.parent = parent;
        ClientGravemindState.setUnreadDialogue(false);
    }

    @Override
    protected void init() {
        if (gravemindHead == null) {
            Item nodeItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("sculkhorde", "sculk_ancient_node"));
            gravemindHead = new ItemStack(nodeItem != null ? nodeItem : Blocks.SCULK_SHRIEKER);
        }

        
        this.windowWidth = Math.min(512, this.width - 40);
        this.windowHeight = Math.min(440, this.height - 40);
        this.guiLeft = (this.width - this.windowWidth) / 2;
        this.guiTop = (this.height - this.windowHeight) / 2;
        int centerX = this.guiLeft + (this.windowWidth / 2);

        GravemindTask current = TaskManager.getCurrentTask();

        this.clearWidgets();

        if (current == null) {
            Player player = Minecraft.getInstance().player;
            availableOptions = TaskManager.getAvailableOptions(player);
        } else {
            
            int actionY = this.guiTop + this.windowHeight - 65;

            completeButton = Button.builder(Component.literal("Claim Reward"), (button) -> {
                if (current.isComplete()) {
                    BlockPos target = current.targetLocation != null ? current.targetLocation : BlockPos.ZERO;

                    PacketHandler.CHANNEL.sendToServer(new TaskActionPacket(
                            0, 
                            current.type.ordinal(),
                            current.requiredAmount,
                            current.faithReward,
                            current.evoReward,
                            target
                    ));

                    ClientGravemindState.addFaith(current.faithReward);

                    TaskManager.forceSetTask(Minecraft.getInstance().player, null);
                    TaskManager.generateOptions(Minecraft.getInstance().player);

                    this.init();
                }
            }).bounds(centerX - 80, actionY, 160, 20).build();

            completeButton.active = current.isComplete();
            this.addRenderableWidget(completeButton);

            cancelButton = Button.builder(Component.literal("§cAbandon (-3 Evo, -4 Faith)"), (button) -> {
                PacketHandler.CHANNEL.sendToServer(new TaskActionPacket(3, 0)); 

                TaskManager.forceSetTask(Minecraft.getInstance().player, null);
                TaskManager.generateOptions(Minecraft.getInstance().player);

                this.init();
            }).bounds(centerX - 80, actionY + 25, 160, 20).build();

            this.addRenderableWidget(cancelButton);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int centerX = this.guiLeft + (this.windowWidth / 2);

        renderWindowBackground(graphics, this.guiLeft, this.guiTop);
        
        renderGravemindHead(graphics, centerX, this.guiTop + 70, mouseX, mouseY);

        GravemindTask current = TaskManager.getCurrentTask();
        String dialogue = (current == null) ?  "§3The horde requires sustenance." : "§3The plan is in motion.";
        graphics.drawCenteredString(this.font, dialogue, centerX, this.guiTop + 130, 0xFFFFFF);

        if (current == null) {
            renderScrollableList(graphics, this.guiLeft, this.guiTop, mouseX, mouseY);
        } else {
            if (completeButton != null) {
                completeButton.active = current.isComplete();
            }
            renderActiveTask(graphics, this.guiLeft, this.guiTop, current);
        }

        GravemindMainScreen.renderBorder(graphics, this.guiLeft - 1, this.guiTop - 1, this.windowWidth + 2, this.windowHeight + 2, 0xFF00AAAA);

        renderTabs(graphics, mouseX, mouseY, this.guiLeft, this.guiTop, 2);

        super.render(graphics, mouseX, mouseY, partialTick);
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

    private void renderScrollableList(GuiGraphics graphics, int guiLeft, int guiTop, int mouseX, int mouseY) {
        if (availableOptions == null || availableOptions.isEmpty()) {
            graphics.drawCenteredString(this.font, "No available directives.", guiLeft + (this.windowWidth / 2), guiTop + 180, 0x888888);
            return;
        }

        int listX = guiLeft + 30;
        int listY = guiTop + 160; 
        int listWidth = this.windowWidth - 60;
        
        int listHeight = Math.max(50, this.windowHeight - 180);
        int totalContentHeight = availableOptions.size() * ITEM_HEIGHT;

        graphics.enableScissor(listX, listY, listX + listWidth, listY + listHeight);
        graphics.pose().pushPose();
        graphics.pose().translate(0, -scrollOffset, 0);

        int currentY = listY;
        for (GravemindTask task : availableOptions) {
            graphics.fill(listX, currentY, listX + listWidth, currentY + ITEM_HEIGHT - 5, 0x44000000);
            graphics.drawString(this.font, task.getDescription(), listX + 10, currentY + 5, 0xFFFFFF);

            String rewardText = "§6Reward: ";
            if (task.faithReward > 0) rewardText += task.faithReward + " Faith ";
            if (task.evoReward > 0) rewardText += "§a" + task.evoReward + " Evo ";
            if (!task.rewardItem.isEmpty()) rewardText += "§d" + task.rewardItem.getHoverName().getString();

            graphics.drawString(this.font, rewardText, listX + 10, currentY + 20, 0xFFAA00);

            int btnW = 80; int btnH = 20;
            int btnX = listX + listWidth - btnW - 10;
            int btnY = currentY + 10;
            boolean hovered = (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY - scrollOffset && mouseY <= btnY + btnH - scrollOffset);

            int btnBg = hovered ? 0xFF444444 : 0xFF222222;
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF888888);
            graphics.fill(btnX + 1, btnY + 1, btnX + btnW - 1, btnY + btnH - 1, btnBg);
            graphics.drawCenteredString(this.font, "Accept", btnX + btnW / 2, btnY + 6, hovered ? 0xFFFFFF : 0xAAAAAA);

            currentY += ITEM_HEIGHT;
        }
        graphics.pose().popPose();
        graphics.disableScissor();

        
        if (totalContentHeight > listHeight) {
            int scrollBarX = listX + listWidth + 2;
            int scrollBarH = (int) ((float) listHeight / totalContentHeight * listHeight);
            int scrollY = listY + (int) ((float) scrollOffset / (totalContentHeight - listHeight) * (listHeight - scrollBarH));

            graphics.fill(scrollBarX, listY, scrollBarX + 4, listY + listHeight, 0x44000000);
            graphics.fill(scrollBarX, scrollY, scrollBarX + 4, scrollY + scrollBarH, 0xFF888888);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (TaskManager.getCurrentTask() == null && availableOptions != null) {
            int totalContentHeight = availableOptions.size() * ITEM_HEIGHT;
            int listHeight = Math.max(50, this.windowHeight - 180);
            if (totalContentHeight > listHeight) {
                scrollOffset = Math.max(0, Math.min(scrollOffset - delta * 20, totalContentHeight - listHeight));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startX = this.guiLeft + 20; int startY = this.guiTop + 10; int gap = 40; int size = 20;
            if (isHovering(startX, startY, size, size, mouseX, mouseY)) { this.minecraft.setScreen(new GravemindMainScreen()); return true; }
            if (isHovering(startX + gap, startY, size, size, mouseX, mouseY)) { this.minecraft.setScreen(new EvolutionTab(new GravemindMainScreen())); return true; }
            if (isHovering(startX + (gap * 3), startY, size, size, mouseX, mouseY)) { this.minecraft.setScreen(new SupportTab(new GravemindMainScreen())); return true; }

            if (TaskManager.getCurrentTask() == null && availableOptions != null) {
                int listX = this.guiLeft + 30;
                int listY = this.guiTop + 160;
                int listWidth = this.windowWidth - 60;
                int listHeight = Math.max(50, this.windowHeight - 180);

                if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
                    double relativeY = mouseY - listY + scrollOffset;
                    int index = (int) (relativeY / ITEM_HEIGHT);

                    if (index >= 0 && index < availableOptions.size()) {
                        GravemindTask task = availableOptions.get(index);
                        int btnW = 80;
                        int btnX = listX + listWidth - btnW - 10;
                        if (mouseX >= btnX) {
                            BlockPos p = task.targetLocation != null ? task.targetLocation : BlockPos.ZERO;
                            PacketHandler.CHANNEL.sendToServer(new TaskActionPacket(2, task.type.ordinal(), task.requiredAmount, task.faithReward, task.evoReward, p));

                            TaskManager.forceSetTask(Minecraft.getInstance().player, task);
                            ClientGravemindState.queueMessage("Directive Accepted.");

                            this.init();
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderActiveTask(GuiGraphics graphics, int guiLeft, int guiTop, GravemindTask current) {
        int centerX = guiLeft + (this.windowWidth / 2);

        
        int cardY = guiTop + 160;
        int cardW = Math.min(300, this.windowWidth - 40); 
        int cardH = this.windowHeight - 240; 
        if (cardH < 90) cardH = 90; 
        int cardX = centerX - (cardW / 2);

        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0x66000000);
        GravemindMainScreen.renderBorder(graphics, cardX, cardY, cardW, cardH, 0xFF00AAAA);

        graphics.drawCenteredString(this.font, "§eACTIVE DIRECTIVE", centerX, cardY + 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, current.getDescription(), centerX, cardY + 30, 0xCCCCCC);

        if (current.targetLocation != null && !current.targetLocation.equals(BlockPos.ZERO)) {
            String coords = "Target: §b" + current.targetLocation.getX() + ", " + current.targetLocation.getY() + ", " + current.targetLocation.getZ();
            graphics.drawCenteredString(this.font, coords, centerX, cardY + 45, 0xAAAAAA);
        } else {
            graphics.drawCenteredString(this.font, "§7No specific location", centerX, cardY + 45, 0x555555);
        }

        String rewardText = "Reward: " + current.faithReward + " Faith " + current.evoReward + " Evo";
        graphics.drawCenteredString(this.font, rewardText, centerX, cardY + 70, 0xFFAA00);

        int barW = cardW - 40;
        int barH = 12;
        int barX = centerX - (barW / 2);
        int barY = cardY + 85;
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF222222);

        float percent = current.requiredAmount > 0 ? Math.min(1.0f, (float)current.currentAmount / current.requiredAmount) : 0;
        int filledW = (int)(barW * percent);
        int color = current.isComplete() ? 0xFF00FF00 : 0xFF00AAAA;

        graphics.fill(barX, barY, barX + filledW, barY + barH, color);

        String progressText = current.isComplete() ? "COMPLETE" : (current.currentAmount + "/" + current.requiredAmount);
        graphics.drawCenteredString(this.font, progressText, centerX, barY + 2, 0xFFFFFF);
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

    private void renderGravemindHead(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        float yaw = (float)Math.atan2(mouseX - x, 300.0f);
        float pitch = (float)Math.atan2(mouseY - y, 300.0f);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 150);
        graphics.pose().scale(80.0F, 80.0F, -80.0F); 

        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI);
        rotation.mul(new Quaternionf().rotateY(yaw));
        rotation.mul(new Quaternionf().rotateX(pitch));

        graphics.pose().mulPose(rotation);

        Minecraft.getInstance().getItemRenderer().renderStatic(gravemindHead, net.minecraft.world.item.ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, graphics.pose(), Minecraft.getInstance().renderBuffers().bufferSource(), Minecraft.getInstance().level, 0);
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        graphics.pose().popPose();
    }

    private boolean isHovering(int x, int y, int w, int h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
