package net.alekrus.shphysarum.attackgoal;

import com.github.sculkhorde.common.entity.goal.CustomMeleeAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import java.lang.reflect.Method;
import java.util.EnumSet;

public class SculkAllyGoals {


    public static class PlayerCommandedTargetGoal extends Goal {
        private final Mob mob;

        public PlayerCommandedTargetGoal(Mob mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {

            LivingEntity commandedTarget = SculkAllyManager.COMMANDED_TARGETS.get(this.mob);

            if (commandedTarget == null || !commandedTarget.isAlive()) {

                SculkAllyManager.COMMANDED_TARGETS.remove(this.mob);
                return false;
            }


            return this.mob.getTarget() != commandedTarget;
        }

        @Override
        public void start() {
            LivingEntity commandedTarget = SculkAllyManager.COMMANDED_TARGETS.get(this.mob);
            if (commandedTarget != null) {

                this.mob.setTarget(commandedTarget);
                this.mob.setLastHurtByMob(commandedTarget);
            }
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity commandedTarget = SculkAllyManager.COMMANDED_TARGETS.get(this.mob);
            return commandedTarget != null && commandedTarget.isAlive();
        }
    }


    public static class InjectedCustomMeleeAttackGoal extends CustomMeleeAttackGoal {

        public InjectedCustomMeleeAttackGoal(PathfinderMob mob) {
            super(mob, 1.0D, true, 10);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive()) return false;
            return this.mob.getNavigation().createPath(target, 0) != null;
        }

        @Override
        protected void triggerAnimation() {
            try {
                Method triggerAnimMethod = this.mob.getClass().getMethod("triggerAnim", String.class, String.class);
                triggerAnimMethod.invoke(this.mob, "attack_controller", "attack_animation");
            } catch (Exception e) {

            }
        }
    }
}
