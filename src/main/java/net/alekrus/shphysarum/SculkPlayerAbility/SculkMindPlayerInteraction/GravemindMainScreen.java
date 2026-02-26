package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.EvolutionTab;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.SupportTab;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.TasksTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GravemindMainScreen extends Screen {

    
    private int windowWidth;
    private int windowHeight;
    private int guiLeft;
    private int guiTop;

    private final List<Question> questions = new ArrayList<>();
    private Question currentQuestion = null;
    private int textTicker = 0;

    public GravemindMainScreen() {
        super(Component.literal("Gravemind"));
    }

    @Override
    protected void init() {
        
        
        this.windowWidth = Math.min(512, this.width - 40);
        this.windowHeight = Math.min(440, this.height - 40);

        this.guiLeft = (this.width - this.windowWidth) / 2;
        this.guiTop = (this.height - this.windowHeight) / 2;

        if (questions.isEmpty()) {
            questions.add(new Question(
                    "What happened to me?",
                    "You asked for a gift, and you received it. Your body is beginning to transform into a perfect organism. Is that not why you are here? \n\nIn truth, I am somewhat surprised. Rarely do I see a self-aware being approach me to ask for a gift. However, I will give you a chance. If you wish to become better, prove it, and I shall repay you in kind. \n\nAnd yes, fortunately for you, I am always with you now. I also wanted to restructure your mind so you could become superior, but that would have driven you mad. Therefore, I am keeping this process under control. You are a very strange creature; a single improvement to your existing brain, and you would no longer be who you were."
            ));

            questions.add(new Question(
                    "Where do I start?",
                    "You have two important sets of points. The first is Faith — my trust in you, which you can use to request support from the Horde. The second is Evolution Points, which you can spend to evolve your body into a perfect organism, like us. \n\nYou can acquire both by completing the tasks I will assign to you. Prove your worth, and you will receive appropriate rewards, and perhaps, my trust."
            ));

            questions.add(new Question(
                    "The Sculk Horde",
                    "I control all of my entities. They will not harm you, as you are now one of us. I have allowed you to retain your right to independent thought solely because of your belief in us within your mind. And I am curious to see how far a creature like you can go. \n\nBy improving your synaptic connection with me, you will be able to intercept control over organisms that correspond to your enhancements."
            ));

            questions.add(new Question(
                    "Sculk Anchor",
                    "The anchor will allow you to bind your soul to a precise location of your choosing. However, to do this, it must be fueled by Crying Souls, so make sure the charge does not drop to zero."
            ));

            questions.add(new Question(
                    "Experience",
                    "I calculated that a quarter of your body consisted of a digestive system, and I replaced it with a more reliable and compact equivalent. Your body, as well as your newly acquired abilities, will now feed on the essence of experience. This will move you one step closer to becoming a superior organism."
            ));

            if (ClientGravemindState.hasSeenBeacon) {
                questions.add(new Question(
                        "The Sculk Beacon",
                        "This beacon was designed to saturate budding organisms for radical physiological enhancements. When you place a Mycelium Sprout inside, it will be infused for rapid maturation. \n\nHowever, this process is highly conspicuous and may attract adversaries. You had better prepare for battle. \n\n And yes, for it to work, it needs a flat area of 40 by 40 and an open sky."
                ));
            }
        }

        
        int btnY = this.guiTop + 60;
        int btnWidth = (this.windowWidth / 2) - 40;
        int btnX = this.guiLeft + 20;

        for (Question q : questions) {
            this.addRenderableWidget(Button.builder(Component.literal(q.questionText), (b) -> {
                if (currentQuestion != q) {
                    currentQuestion = q;
                    textTicker = 0;
                    Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                }
            }).bounds(btnX, btnY, btnWidth, 20).build());

            btnY += 25;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (currentQuestion != null) {
            textTicker++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int centerX = this.guiLeft + (this.windowWidth / 2);

        renderWindowBackground(graphics, this.guiLeft, this.guiTop);

        graphics.fill(centerX - 1, this.guiTop + 40, centerX + 1, this.guiTop + this.windowHeight - 20, 0xFF00AAAA);
        graphics.drawCenteredString(this.font, "§7Inquiries", this.guiLeft + (this.windowWidth / 4), this.guiTop + 45, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§bGravemind Response", this.guiLeft + (this.windowWidth / 4 * 3), this.guiTop + 45, 0xFFFFFF);

        renderAnswerPanel(graphics, centerX, this.guiTop + 60, (this.windowWidth / 2) - 20, this.windowHeight - 80);

        renderTabs(graphics, mouseX, mouseY, this.guiLeft, this.guiTop, 0);

        renderBorder(graphics, this.guiLeft - 1, this.guiTop - 1, this.windowWidth + 2, this.windowHeight + 2, 0xFF00AAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderAnswerPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        if (currentQuestion == null) {
            RenderSystem.setShaderTexture(0, GravemindResources.ICON_DEFAULT);
            RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.5F);
            int iconSize = Math.min(64, w / 2); 
            graphics.blit(GravemindResources.ICON_DEFAULT, x + (w - iconSize) / 2, y + (h - iconSize) / 2, 0, 0, iconSize, iconSize, iconSize, iconSize);

            graphics.drawCenteredString(this.font, "§8Waiting for input...", x + w / 2, y + h / 2 + (iconSize / 2) + 10, 0xFFFFFF);
            return;
        }

        graphics.fill(x + 10, y, x + w - 10, y + h, 0x55000000);

        String fullText = currentQuestion.answerText;
        int lengthToShow = Math.min(fullText.length(), textTicker * 3);
        String textToShow = fullText.substring(0, lengthToShow);

        
        graphics.drawWordWrap(this.font, Component.literal(textToShow), x + 20, y + 20, w - 40, 0x55FFFF);
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

    public static void renderBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    protected void renderTabs(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int activeTabID) {
        int startX = x + 20;
        int startY = y + 10;
        int gap = 40;

        drawSingleTab(graphics, GravemindResources.TAB_ICON_DIALOGUE, startX + (gap * 0), startY, activeTabID == 0, mouseX, mouseY);
        drawSingleTab(graphics, GravemindResources.TAB_ICON_SKILLTREE,  startX + (gap * 1), startY, activeTabID == 1, mouseX, mouseY);
        drawSingleTab(graphics, GravemindResources.TAB_ICON_TASK, startX + (gap * 2), startY, activeTabID == 2, mouseX, mouseY);
        drawSingleTab(graphics, GravemindResources.TAB_ICON_SUPPORT,  startX + (gap * 3), startY, activeTabID == 3, mouseX, mouseY);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startX = this.guiLeft + 20;
            int startY = this.guiTop + 10;
            int gap = 40;
            int size = 20;

            if (isHovering(startX + (gap * 0), startY, size, size, mouseX, mouseY)) return true;
            if (isHovering(startX + (gap * 1), startY, size, size, mouseX, mouseY)) {
                this.minecraft.setScreen(new EvolutionTab(this)); return true;
            }
            if (isHovering(startX + (gap * 2), startY, size, size, mouseX, mouseY)) {
                this.minecraft.setScreen(new TasksTab(this)); return true;
            }
            if (isHovering(startX + (gap * 3), startY, size, size, mouseX, mouseY)) {
                this.minecraft.setScreen(new SupportTab(this)); return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

    @Override
    public boolean isPauseScreen() { return false; }

    private static class Question {
        String questionText;
        String answerText;

        public Question(String q, String a) {
            this.questionText = q;
            this.answerText = a;
        }
    }
}
