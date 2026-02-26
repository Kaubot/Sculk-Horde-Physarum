package net.alekrus.shphysarum.effects.PurityToPlayer;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SculkMindProvider;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicReference;

public class ResistanceLogic {

    public static float getDurationMultiplier(Player player) {
        AtomicReference<Float> multiplier = new AtomicReference<>(1.0f);

        player.getCapability(SculkMindProvider.SCULK_MIND).ifPresent(cap -> {
            if (cap.hasSkill("resistance_5")) {
                multiplier.set(0.20f); 
            } else if (cap.hasSkill("resistance_4")) {
                multiplier.set(0.40f); 
            } else if (cap.hasSkill("resistance_3")) {
                multiplier.set(0.55f); 
            } else if (cap.hasSkill("resistance_2")) {
                multiplier.set(0.70f); 
            } else if (cap.hasSkill("resistance_1")) {
                multiplier.set(0.85f); 
            }
        });

        return multiplier.get();
    }
}
