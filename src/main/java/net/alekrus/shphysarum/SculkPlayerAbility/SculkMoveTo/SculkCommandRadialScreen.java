package net.alekrus.shphysarum.SculkPlayerAbility.SculkMoveTo;

import net.alekrus.shphysarum.KeyBind.ModKeyBindings;
import net.alekrus.shphysarum.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;

public class SculkCommandRadialScreen extends Screen {

    private enum Selection {
        NONE, LEFT, RIGHT, BOTTOM 
    }

    private Selection currentSelection = Selection.NONE;

    public SculkCommandRadialScreen() {
        super(Component.literal("Sculk Command"));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int w = this.width;
        int h = this.height;
        int cx = w / 2;
        int cy = h / 2;

        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 20) {
            currentSelection = Selection.NONE;
        } else {
            double angle = Math.toDegrees(Math.atan2(dy, dx)); 

            
            
            if (angle > 45 && angle < 135) {
                currentSelection = Selection.BOTTOM;
            } else if (dx < 0) {
                currentSelection = Selection.LEFT;
            } else {
                currentSelection = Selection.RIGHT;
            }
        }

        int colorLeft = (currentSelection == Selection.LEFT) ? 0xAA00FFFF : 0x66000000;
        int colorRight = (currentSelection == Selection.RIGHT) ? 0xAA00FFFF : 0x66000000;
        int colorBottom = (currentSelection == Selection.BOTTOM) ? 0xAA00FFFF : 0x66000000;
        int textColor = 0xFFFFFF;

        
        graphics.fill(cx - 110, cy - 20, cx - 10, cy + 20, colorLeft);
        graphics.drawCenteredString(this.font, "MOVE TO TARGET", cx - 60, cy - 4, textColor);

        
        graphics.fill(cx + 10, cy - 20, cx + 110, cy + 20, colorRight);
        graphics.drawCenteredString(this.font, "GUARD ME", cx + 60, cy - 4, textColor);

        
        graphics.fill(cx - 50, cy + 30, cx + 50, cy + 60, colorBottom);
        graphics.drawCenteredString(this.font, "SET CONNECTION", cx, cy + 41, textColor);

        if (currentSelection == Selection.LEFT) {
            graphics.drawCenteredString(this.font, "[Aiming at crosshair]", cx, cy + 70, 0xAAAAAA);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ModKeyBindings.MOVE_KEY.matches(keyCode, scanCode)) {
            executeCommand();
            
            if (currentSelection != Selection.BOTTOM) {
                this.onClose();
            }
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void executeCommand() {
        if (currentSelection == Selection.NONE) return;

        if (currentSelection == Selection.LEFT) {
            HitResult trace = Minecraft.getInstance().player.pick(50.0D, 0.0F, false);
            if (trace.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((net.minecraft.world.phys.BlockHitResult) trace).getBlockPos();
                PacketHandler.CHANNEL.sendToServer(new SculkCommandPacket(SculkCommandPacket.Action.MOVE_TO_POS, pos));
            }
        }
        else if (currentSelection == Selection.RIGHT) {
            PacketHandler.CHANNEL.sendToServer(new SculkCommandPacket(SculkCommandPacket.Action.TOGGLE_FOLLOW, null));
        }
        else if (currentSelection == Selection.BOTTOM) {
            
            Minecraft.getInstance().setScreen(new ConnectionManagerScreen());
        }
    }
}
