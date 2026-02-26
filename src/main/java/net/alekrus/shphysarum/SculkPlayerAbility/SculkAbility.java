package net.alekrus.shphysarum.SculkPlayerAbility;

public enum SculkAbility {
    NONE("None", "root"),
    VISION("Sculk Vision", "vision"),
    SUMMONER("Summon Reinforcements", "summoner"),
    BURST("Spore Burst", "burst"),
    RAID("Initiate Scout", "raid"),
    LEAP("Sculk Leap", "leap"),
    BURROW("Sculk Burrow", "burrow"),
    SHARP_TENTACLE("Sharp Tentacle", "adaptive_body_structuring"),
    IMMEDIATE_ACTIONS("Flesh Shedding", "immediate_actions"),
    ESSENCE_EXTRACT("Extract Essence", "essence_extract"); 




    private final String displayName;
    private final String skillId;

    SculkAbility(String displayName, String skillId) {
        this.displayName = displayName;
        this.skillId = skillId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSkillId() {
        return skillId;
    }
}
