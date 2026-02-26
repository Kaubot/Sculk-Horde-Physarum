package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.MiniGame;
import net.minecraft.client.Minecraft;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData; 


public class SynapticClientLogic {
    public static void handle(int[][] grid, boolean won, boolean lost) {
        
        if (Minecraft.getInstance().screen instanceof SynapticInvasionScreen screen) {
            screen.updateGridState(grid, won, lost);
        }

        
        if (won) {
            ClientSkillData.addLearnedSkill("adaptive_morph");
        }
    }
}