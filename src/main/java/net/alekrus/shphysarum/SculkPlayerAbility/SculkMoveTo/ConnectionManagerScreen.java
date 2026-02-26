package net.alekrus.shphysarum.SculkPlayerAbility.SculkMoveTo;

import com.mojang.blaze3d.systems.RenderSystem;
import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ConnectionManagerScreen extends Screen {

    private static final int WINDOW_WIDTH = 340;
    private static final int WINDOW_HEIGHT = 220;

    private int maxAllowedLimit = 2;
    
    private final Set<String> allowedTypes = new HashSet<>();

    private LimitSlider limitSlider;
    private MobSelectionList mobList;

    public ConnectionManagerScreen() {
        super(Component.literal("Connection Manager"));
    }

    @Override
    protected void init() {
        
        maxAllowedLimit = 2;
        if (ClientSkillData.hasSkill("connection_t1")) maxAllowedLimit += 2;
        if (ClientSkillData.hasSkill("connection_t2")) maxAllowedLimit += 2;
        if (ClientSkillData.hasSkill("connection_t3")) maxAllowedLimit += 2;

        int guiLeft = (this.width - WINDOW_WIDTH) / 2;
        int guiTop = (this.height - WINDOW_HEIGHT) / 2;

        
        int savedLimit = ClientSkillData.getUserFollowerLimit();
        
        if (savedLimit > maxAllowedLimit) savedLimit = maxAllowedLimit;
        
        if (savedLimit == 0) savedLimit = maxAllowedLimit;

        
        int leftX = guiLeft + 20;
        int leftY = guiTop + 40;

        
        limitSlider = new LimitSlider(leftX, leftY, 140, 20, Component.empty(), 0, maxAllowedLimit);

        
        double sliderPos = (double) savedLimit / (double) maxAllowedLimit;
        limitSlider.setValue(sliderPos);

        this.addRenderableWidget(limitSlider);

        
        this.addRenderableWidget(Button.builder(Component.literal("Apply & Exit"), b -> this.onClose())
                .bounds(leftX, guiTop + WINDOW_HEIGHT - 40, 140, 20)
                .build());

        
        int listX = guiLeft + 180;
        int listY = guiTop + 35;
        int listWidth = 140;
        int listHeight = WINDOW_HEIGHT - 55;

        mobList = new MobSelectionList(this.minecraft, listWidth, listHeight, listY, listY + listHeight, 24);
        mobList.setLeftPos(listX);

        
        Set<String> savedAllowed = ClientSkillData.getAllowedFollowerTypes();
        
        boolean enableAll = savedAllowed.isEmpty();

        addMobsIfUnlocked(SculkFollowerManager.TIER_0_MOBS, "unknown_connection_root", savedAllowed, enableAll);
        addMobsIfUnlocked(SculkFollowerManager.TIER_1_MOBS, "connection_t1", savedAllowed, enableAll);
        addMobsIfUnlocked(SculkFollowerManager.TIER_2_MOBS, "connection_t2", savedAllowed, enableAll);
        addMobsIfUnlocked(SculkFollowerManager.TIER_3_MOBS, "connection_t3", savedAllowed, enableAll);
        addMobsIfUnlocked(SculkFollowerManager.TIER_4_MOBS, "connection_t4", savedAllowed, enableAll);

        this.addRenderableWidget(mobList);
    }

    private void addMobsIfUnlocked(List<String> mobs, String skill, Set<String> savedAllowed, boolean enableAll) {
        if (ClientSkillData.hasSkill(skill)) {
            for (String mob : mobs) {
                
                if (enableAll || savedAllowed.contains(mob)) {
                    allowedTypes.add(mob);
                }
                
                mobList.addEntry(new MobEntry(mob));
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int guiLeft = (this.width - WINDOW_WIDTH) / 2;
        int guiTop = (this.height - WINDOW_HEIGHT) / 2;

        graphics.fill(guiLeft, guiTop, guiLeft + WINDOW_WIDTH, guiTop + WINDOW_HEIGHT, 0xFF252525);
        graphics.renderOutline(guiLeft, guiTop, WINDOW_WIDTH, WINDOW_HEIGHT, 0xFF00AAAA);

        graphics.drawCenteredString(this.font, "Connection Protocol", this.width / 2, guiTop + 10, 0xFFFFFF);
        graphics.drawString(this.font, "Swarm Limit", guiLeft + 20, guiTop + 28, 0xAAAAAA);
        graphics.drawString(this.font, "Allowed Units", guiLeft + 180, guiTop + 28, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        
        int limit = (int) Math.round(limitSlider.value * maxAllowedLimit);
        
        PacketHandler.CHANNEL.sendToServer(new ConnectionConfigPacket(limit, allowedTypes));
        super.onClose();
    }

    
    
    

    
    public class MobSelectionList extends ObjectSelectionList<MobEntry> {
        public MobSelectionList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight) {
            super(mc, width, height, y0, y1, itemHeight);
            this.setRenderHeader(false, 0);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
        }

        @Override
        protected void renderBackground(GuiGraphics graphics) {} 

        @Override public int getRowWidth() { return this.width - 10; }
        @Override protected int getScrollbarPosition() { return this.getLeft() + this.width - 6; }
        @Override public int addEntry(MobEntry entry) { return super.addEntry(entry); }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getLeft(); int y = this.getTop(); int w = this.getWidth(); int h = this.getHeight();
            graphics.fill(x, y, x + w, y + h, 0xFF151515);
            graphics.renderOutline(x - 1, y - 1, w + 2, h + 2, 0xFF444444);
            super.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    
    public class MobEntry extends ObjectSelectionList.Entry<MobEntry> {
        private final String mobId;
        private final Component name;

        public MobEntry(String mobId) {
            this.mobId = mobId;
            String s = mobId.replace("sculkhorde:", "").replace("sculk_", "").replace("_", " ");
            this.name = Component.literal(capitalize(s));
        }

        private String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            
            boolean isSelected = allowedTypes.contains(mobId);

            int boxSize = 12;
            int boxX = left + 4;
            int boxY = top + 4;

            graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xFF000000);
            graphics.renderOutline(boxX, boxY, boxSize, boxSize, isSelected ? 0xFF00AA00 : 0xFF666666);
            if (isSelected) graphics.fill(boxX + 2, boxY + 2, boxX + boxSize - 2, boxY + boxSize - 2, 0xFF00FF00);

            int color = isSelected ? 0xFFFFFF : 0xAAAAAA;
            graphics.drawString(Minecraft.getInstance().font, name, boxX + 18, top + 6, color);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (allowedTypes.contains(mobId)) allowedTypes.remove(mobId);
            else allowedTypes.add(mobId);

            Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }

        @Override public Component getNarration() { return name; }
    }

    
    private class LimitSlider extends AbstractWidget {
        private final double maxVal;
        public double value;

        public LimitSlider(int x, int y, int w, int h, Component msg, double min, double max) {
            super(x, y, w, h, msg);
            this.maxVal = max;
            updateMessage();
        }

        @Override protected void updateWidgetNarration(NarrationElementOutput output) { this.defaultButtonNarrationText(output); }

        private void setValueFromMouse(double mouseX) {
            double relative = (mouseX - (this.getX() + 4)) / (double) (this.width - 8);
            this.value = Mth.clamp(relative, 0.0, 1.0);
            updateMessage();
        }

        public void setValue(double val) {
            this.value = Mth.clamp(val, 0.0, 1.0);
            updateMessage();
        }

        private void updateMessage() {
            int val = (int) Math.round(value * maxVal);
            this.setMessage(Component.literal("Limit: " + val + " / " + (int) maxVal));
        }

        @Override public void onClick(double mouseX, double mouseY) { setValueFromMouse(mouseX); }
        @Override public void onDrag(double mouseX, double mouseY, double dragX, double dragY) { setValueFromMouse(mouseX); }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableBlend();
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000);
            graphics.renderOutline(getX(), getY(), width, height, 0xFF555555);
            int fillWidth = (int) (value * (width - 2));
            graphics.fill(getX() + 1, getY() + 1, getX() + 1 + fillWidth, getY() + height - 1, 0xFF00AAAA);
            graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFF);
        }
    }
}
