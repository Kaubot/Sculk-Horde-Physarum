package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ClientSkillData {

    private static final Set<String> learnedSkills = new HashSet<>();
    private static int evolutionPoints = 0;
    private static SculkAbility selectedAbility = SculkAbility.NONE;
    private static int faith = 0;
    private static int userFollowerLimit = 2;
    private static final Set<String> allowedFollowerTypes = new HashSet<>();
    private static final List<BlockPos> clientAnchors = new ArrayList<>();

    
    private static final Set<String> activeAbilities = new HashSet<>();

    public static void syncActiveAbilities(Set<String> active) {
        activeAbilities.clear();
        activeAbilities.addAll(active);
    }

    public static Set<String> getActiveAbilities() {
        return activeAbilities;
    }

    public static boolean isAbilityActive(String skillId) {
        return activeAbilities.contains(skillId);
    }
    

    
    private static final Map<String, ResourceLocation> SKILL_ICONS = new HashMap<>();

    public static void registerSkillIcon(String skillId, ResourceLocation icon) {
        SKILL_ICONS.put(skillId, icon);
    }

    public static ResourceLocation getIcon(String skillId) {
        return SKILL_ICONS.get(skillId);
    }

    public static ResourceLocation getIconForAbility(SculkAbility ability) {
        if (ability == SculkAbility.NONE) return null;
        return SKILL_ICONS.get(ability.name().toLowerCase());
    }

    
    public static void sync(Collection<String> skills, int points) {
        learnedSkills.clear();
        learnedSkills.addAll(skills);
        evolutionPoints = points;
    }

    public static boolean hasSkill(String skillId) { return learnedSkills.contains(skillId); }
    public static void addLearnedSkill(String skillId) { learnedSkills.add(skillId); }
    public static int getPoints() { return evolutionPoints; }
    public static void setPoints(int points) { evolutionPoints = points; }
    public static SculkAbility getSelectedAbility() { return selectedAbility; }
    public static void setSelectedAbility(SculkAbility ability) { selectedAbility = ability; }
    public static void setFaith(int f) { faith = f; }
    public static int getFaith() { return faith; }
    public static void syncConnectionData(int limit, Set<String> allowed) {
        userFollowerLimit = limit;
        allowedFollowerTypes.clear();
        allowedFollowerTypes.addAll(allowed);
    }
    public static int getUserFollowerLimit() { return userFollowerLimit; }
    public static Set<String> getAllowedFollowerTypes() { return allowedFollowerTypes; }
    public static void setAnchors(List<BlockPos> list) {
        clientAnchors.clear();
        clientAnchors.addAll(list);
    }
    public static List<BlockPos> getAnchors() { return clientAnchors; }
}
