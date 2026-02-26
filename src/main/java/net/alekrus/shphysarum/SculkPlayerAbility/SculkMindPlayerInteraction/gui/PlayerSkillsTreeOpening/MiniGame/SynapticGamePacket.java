package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.MiniGame;

import net.alekrus.shphysarum.PacketHandler;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.*;
import java.util.function.Supplier;
import java.util.*;

public class SynapticGamePacket {

    private int x, y;
    private int[][] syncGrid;
    private boolean isUpdatePacket = false;
    private boolean won = false;
    private boolean lost = false;

    
    public static SynapticGamePacket decode(FriendlyByteBuf buf) {
        return new SynapticGamePacket(buf);
    }

    public SynapticGamePacket(int x, int y) { this.x = x; this.y = y; }
    public SynapticGamePacket(int[][] grid, boolean won, boolean lost) {
        this.syncGrid = grid; this.isUpdatePacket = true; this.won = won; this.lost = lost;
    }

    public SynapticGamePacket(FriendlyByteBuf buf) {
        this.isUpdatePacket = buf.readBoolean();
        if (isUpdatePacket) {
            this.won = buf.readBoolean();
            this.lost = buf.readBoolean();
            this.syncGrid = new int[7][7];
            for(int i=0; i<7; i++) for(int j=0; j<7; j++) syncGrid[i][j] = buf.readInt();
        } else {
            this.x = buf.readInt();
            this.y = buf.readInt();
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(isUpdatePacket);
        if (isUpdatePacket) {
            buf.writeBoolean(won);
            buf.writeBoolean(lost);
            for(int i=0; i<7; i++) for(int j=0; j<7; j++) buf.writeInt(syncGrid[i][j]);
        } else {
            buf.writeInt(x);
            buf.writeInt(y);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (isUpdatePacket) {
                
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        SynapticClientLogic.handle(syncGrid, won, lost)
                );
            } else {
                
                ServerPlayer player = context.getSender();
                if (player == null) return;

                int cost = 50;
                if (player.totalExperience < cost && !player.isCreative()) {
                    player.sendSystemMessage(Component.literal("§cNot enough XP! Need " + cost));
                    return;
                }
                if (!player.isCreative()) player.giveExperiencePoints(-cost);

                int[][] grid = loadGrid(player);
                validateGrid(grid); 

                
                if (x >= 0 && x < 7 && y >= 0 && y < 7) {
                    if (grid[x][y] == 0 || grid[x][y] == 3 || grid[x][y] == 4) {
                        grid[x][y] = 1;
                    }
                }

                
                boolean playerWon = false;
                for(int i=0; i<7; i++) if (grid[i][6] == 1) playerWon = true;

                
                if (!playerWon) {
                    
                    List<int[]> emptySpots = new ArrayList<>();
                    for(int i=0; i<7; i++) {
                        for(int j=0; j<7; j++) {
                            if (grid[i][j] == 0) emptySpots.add(new int[]{i, j});
                        }
                    }

                    
                    Collections.shuffle(emptySpots);

                    
                    for (int[] spot : emptySpots) {
                        int ex = spot[0];
                        int ey = spot[1];

                        grid[ex][ey] = 2; 

                        if (canReachTarget(grid)) {
                            
                            break;
                        } else {
                            
                            grid[ex][ey] = 0;
                        }
                    }
                }

                saveGrid(player, grid);

                if (playerWon) {
                    player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> cap.unlockSkill("adaptive_morph"));
                    player.sendSystemMessage(Component.literal("§d[Evolution] The hive mind expands. Mutation complete."));

                    
                    net.minecraft.world.item.ItemStack reward = new net.minecraft.world.item.ItemStack(net.alekrus.shphysarum.ItemsAndTab.Items.ModItems.MYCELIUM_SPROUT.get());
                    if (!player.getInventory().add(reward)) player.drop(reward, false);

                    resetGrid(player);
                }

                PacketHandler.CHANNEL.sendTo(new SynapticGamePacket(grid, playerWon, false), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
        context.setPacketHandled(true);
    }

    
    
    private void validateGrid(int[][] grid) {
        boolean hasStart = false;
        boolean hasEnd = false;

        
        if (grid[3][0] == 3 || grid[3][0] == 1) hasStart = true;
        if (grid[3][6] == 4) hasEnd = true;

        
        if (!hasStart) grid[3][0] = 1; 
        if (!hasEnd) grid[3][6] = 4;   
    }

    
    private boolean canReachTarget(int[][] grid) {
        int w = 7;
        int h = 7;
        boolean[][] visited = new boolean[w][h];
        Queue<int[]> queue = new LinkedList<>();

        
        for(int i=0; i<w; i++) {
            for(int j=0; j<h; j++) {
                if (grid[i][j] == 1 || grid[i][j] == 3) {
                    queue.add(new int[]{i, j});
                    visited[i][j] = true;
                }
            }
        }

        
        if (queue.isEmpty()) return false;

        int[][] directions = {{0,1}, {0,-1}, {1,0}, {-1,0}};

        while(!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];

            
            if (grid[cx][cy] == 4) return true;

            for(int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                    if (!visited[nx][ny]) {
                        
                        if (grid[nx][ny] == 0 || grid[nx][ny] == 4) {
                            visited[nx][ny] = true;
                            queue.add(new int[]{nx, ny});
                        }
                    }
                }
            }
        }
        return false;
    }

    private int[][] loadGrid(ServerPlayer player) {
        int[][] grid = new int[7][7];
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains("SynapticGrid")) { resetGrid(player); return loadGrid(player); }
        int[] flat = tag.getIntArray("SynapticGrid");
        if (flat.length != 49) { resetGrid(player); return loadGrid(player); }
        for(int i=0; i<7; i++) for(int j=0; j<7; j++) grid[i][j] = flat[i * 7 + j];
        return grid;
    }

    private void saveGrid(ServerPlayer player, int[][] grid) {
        int[] flat = new int[49];
        for(int i=0; i<7; i++) for(int j=0; j<7; j++) flat[i * 7 + j] = grid[i][j];
        player.getPersistentData().putIntArray("SynapticGrid", flat);
    }

    private void resetGrid(ServerPlayer player) {
        int[][] grid = new int[7][7];
        grid[3][0] = 1; grid[3][6] = 4;
        saveGrid(player, grid);
    }
}
