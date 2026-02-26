package net.alekrus.shphysarum.Config;
import net.alekrus.shphysarum.Config.ModClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen extends Screen {

    
    private final Screen parentScreen;

    public ModConfigScreen(Screen parentScreen) {
        super(Component.literal("Addon Physarum Settings"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        
        boolean currentMusicSetting = ModClientConfig.PLAY_RAID_BEACON_MUSIC.get();

        
        this.addRenderableWidget(Button.builder(
                Component.literal("Raid Music: " + (currentMusicSetting ? "§aON" : "§cOFF")),
                button -> {
                    
                    boolean newValue = !ModClientConfig.PLAY_RAID_BEACON_MUSIC.get();
                    ModClientConfig.PLAY_RAID_BEACON_MUSIC.set(newValue); 

                    
                    button.setMessage(Component.literal("Raid Music: " + (newValue ? "§aON" : "§cOFF")));
                }
        ).bounds(this.width / 2 - 100, this.height / 2 - 20, 200, 20).build());

        
        this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                button -> this.minecraft.setScreen(this.parentScreen)
        ).bounds(this.width / 2 - 100, this.height / 2 + 10, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        
        this.renderBackground(graphics);

        
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        
        this.minecraft.setScreen(this.parentScreen);
    }
}