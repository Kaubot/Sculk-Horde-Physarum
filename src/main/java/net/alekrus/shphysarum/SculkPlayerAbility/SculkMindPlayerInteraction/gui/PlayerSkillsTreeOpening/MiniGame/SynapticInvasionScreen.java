package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.MiniGame;

import net.alekrus.shphysarum.PacketHandler;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;


public class SynapticInvasionScreen extends Screen {

    private final Screen parent;
    private static final int GRID_SIZE = 7;
    private static final int CELL_SIZE = 34; 
    private static final int GAP = 2; 

    
    private int[][] grid = new int[GRID_SIZE][GRID_SIZE];

    private boolean gameOver = false;
    private String statusMessage = "Infect the Target Node";
    private int xpCostPerMove = 50;
    @Override
    public boolean isPauseScreen() {
        return false;
    }


    public SynapticInvasionScreen(Screen parent) {
        super(Component.literal("Synaptic Invasion"));
        this.parent = parent;
        initGrid(); 
    }

    private void initGrid() {
        
        for(int x=0; x<GRID_SIZE; x++)
            for(int y=0; y<GRID_SIZE; y++) grid[x][y] = 0;
        grid[3][0] = 3;
        grid[3][GRID_SIZE-1] = 4;
        grid[3][0] = 1;
    }

    public void updateGridState(int[][] newGrid, boolean won, boolean lost) {
        this.grid = newGrid;
        if (won) {
            this.gameOver = true;
            this.statusMessage = "§a>>> SYSTEM BREACH SUCCESSFUL <<<";
        } else if (lost) {
            this.gameOver = true;
            this.statusMessage = "§c>>> CONNECTION TERMINATED <<<";
        }
    }

    private final ItemStack rewardIcon = new ItemStack(net.alekrus.shphysarum.ItemsAndTab.Items.ModItems.MYCELIUM_SPROUT.get());

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int boardWidth = GRID_SIZE * (CELL_SIZE + GAP);
        int boardHeight = GRID_SIZE * (CELL_SIZE + GAP);
        int startX = (this.width - boardWidth) / 2;
        int startY = (this.height - boardHeight) / 2;

        
        graphics.fill(startX - 10, startY - 30, startX + boardWidth + 10, startY + boardHeight + 10, 0xAA000000);

        
        
        
        int slotSize = 26;
        int slotX = startX - 50; 
        int slotY = startY + (boardHeight / 2) - (slotSize / 2); 

        
        graphics.fill(slotX - 2, slotY - 2, slotX + slotSize + 2, slotY + slotSize + 2, 0xFF00AAAA); 
        graphics.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0xFF000000); 

        
        graphics.drawCenteredString(this.font, "Reward", slotX + slotSize / 2, slotY - 12, 0xAAAAAA);

        
        boolean isWin = gameOver && statusMessage.contains("SUCCESS"); 

        if (isWin) {
            
            graphics.renderFakeItem(rewardIcon, slotX + 5, slotY + 5);
            

            
            if (mouseX >= slotX && mouseX <= slotX + slotSize && mouseY >= slotY && mouseY <= slotY + slotSize) {
                graphics.renderTooltip(this.font, rewardIcon, mouseX, mouseY);
            }
        } else {
            
            graphics.drawCenteredString(this.font, "?", slotX + slotSize / 2, slotY + 9, 0xFF555555);
        }
        


        
        graphics.drawCenteredString(this.font, "§bSynaptic Invasion Protocol", this.width / 2, startY - 20, 0xFFFFFF);

        
        int statusColor = gameOver ? (statusMessage.contains("SUCCESS") ? 0x55FF55 : 0xFF5555) : 0xFFFFFF;
        graphics.drawCenteredString(this.font, statusMessage, this.width / 2, startY + boardHeight + 20, statusColor);

        if (!gameOver) {
            graphics.drawCenteredString(this.font, "§7Cost: §a" + xpCostPerMove + " XP", this.width / 2, startY + boardHeight + 35, 0xAAAAAA);
        }

        
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                int cellX = startX + x * (CELL_SIZE + GAP);
                int cellY = startY + y * (CELL_SIZE + GAP);

                int type = grid[x][y];
                int innerColor = 0xFF111111;
                int borderColor = 0xFF333333;

                if (type == 1) { innerColor = 0xFF00AA00; borderColor = 0xFF55FF55; }
                else if (type == 2) { innerColor = 0xFFAA0000; borderColor = 0xFFFF5555; }
                else if (type == 3) { innerColor = 0xFFAAAA00; borderColor = 0xFFFFFF55; }
                else if (type == 4) { innerColor = 0xFF00AAAA; borderColor = 0xFF55FFFF; }

                boolean isHovered = mouseX >= cellX && mouseX < cellX + CELL_SIZE && mouseY >= cellY && mouseY < cellY + CELL_SIZE;
                if (!gameOver && isHovered && isValidMove(x, y)) {
                    borderColor = 0xFFFFFFFF;
                    innerColor = 0xFF225522;
                }

                graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, borderColor);
                graphics.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, innerColor);
            }
        }

        if (gameOver) {
            graphics.drawCenteredString(this.font, "[ Press ESC ]", this.width / 2, startY + boardHeight + 50, 0xFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (gameOver) {
            this.onClose();
            return true;
        }

        int boardWidth = GRID_SIZE * (CELL_SIZE + GAP);
        int boardHeight = GRID_SIZE * (CELL_SIZE + GAP);
        int startX = (this.width - boardWidth) / 2;
        int startY = (this.height - boardHeight) / 2;

        if (mouseX >= startX && mouseX < startX + boardWidth && mouseY >= startY && mouseY < startY + boardHeight) {
            
            int relativeX = (int) (mouseX - startX);
            int relativeY = (int) (mouseY - startY);

            int gridX = relativeX / (CELL_SIZE + GAP);
            int gridY = relativeY / (CELL_SIZE + GAP);

            
            int modX = relativeX % (CELL_SIZE + GAP);
            int modY = relativeY % (CELL_SIZE + GAP);

            if (modX < CELL_SIZE && modY < CELL_SIZE && gridX < GRID_SIZE && gridY < GRID_SIZE) {
                if (isValidMove(gridX, gridY)) {
                    PacketHandler.CHANNEL.sendToServer(new net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.MiniGame.SynapticGamePacket(gridX, gridY));
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isValidMove(int x, int y) {
        if (grid[x][y] == 1 || grid[x][y] == 2) return false;
        boolean connected = false;
        if (x > 0 && grid[x-1][y] == 1) connected = true;
        if (x < GRID_SIZE-1 && grid[x+1][y] == 1) connected = true;
        if (y > 0 && grid[x][y-1] == 1) connected = true;
        if (y < GRID_SIZE-1 && grid[x][y+1] == 1) connected = true;
        return connected;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
