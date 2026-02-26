package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SculkMind implements ISculkMind {
    private final Set<String> unlockedSkills = new HashSet<>();
    private final Set<String> visitedVillages = new HashSet<>();
    private int zombieKills = 0;
    private int evoPoints = 0;
    private int faith = 0;
    private ItemStack pendingReward = ItemStack.EMPTY;
    private CompoundTag activeTaskNBT = new CompoundTag();
    private boolean tentaclesActive = false;

    private int userFollowerLimit = 2;
    private final Set<String> allowedFollowerTypes = new HashSet<>();
    private final List<BlockPos> knownAnchors = new ArrayList<>();
    private final Set<String> activeAbilitiesSet = new HashSet<>();

    public SculkMind() {
        
        
    }

    @Override
    public Set<String> getActiveAbilitiesSet() { return activeAbilitiesSet; }
    @Override
    public void addActiveAbility(String skillId) { activeAbilitiesSet.add(skillId); }
    @Override
    public void removeActiveAbility(String skillId) { activeAbilitiesSet.remove(skillId); }
    @Override
    public boolean isAbilityActive(String skillId) { return activeAbilitiesSet.contains(skillId); }

    @Override
    public List<BlockPos> getKnownAnchors() { return knownAnchors; }
    @Override
    public boolean addKnownAnchor(BlockPos pos) {
        if (!knownAnchors.contains(pos)) {
            knownAnchors.add(pos);
            return true;
        }
        knownAnchors.remove(pos);
        knownAnchors.add(pos);
        return false;
    }
    @Override
    public void removeKnownAnchor(BlockPos pos) { knownAnchors.remove(pos); }
    @Override
    public void setKnownAnchors(List<BlockPos> anchors) {
        this.knownAnchors.clear();
        this.knownAnchors.addAll(anchors);
    }

    @Override public int getUserFollowerLimit() { return userFollowerLimit; }
    @Override public void setUserFollowerLimit(int limit) { this.userFollowerLimit = limit; }
    @Override public Set<String> getAllowedFollowerTypes() { return allowedFollowerTypes; }
    @Override
    public void setAllowedFollowerTypes(Set<String> types) {
        this.allowedFollowerTypes.clear();
        this.allowedFollowerTypes.addAll(types);
    }

    @Override public Set<String> getUnlockedSkills() { return unlockedSkills; }
    @Override public void unlockSkill(String skillId) { unlockedSkills.add(skillId); }
    @Override public boolean hasSkill(String skillId) { return unlockedSkills.contains(skillId); }

    @Override public int getZombieKills() { return zombieKills; }
    @Override public void incrementZombieKills() { this.zombieKills++; }
    @Override public void setZombieKills(int amount) { this.zombieKills = amount; }

    @Override public int getEvoPoints() { return evoPoints; }
    @Override public void setEvoPoints(int amount) { this.evoPoints = amount; }
    @Override public void addEvoPoints(int amount) { this.evoPoints += amount; }
    @Override public boolean consumeEvoPoints(int amount) {
        if (evoPoints >= amount) {
            evoPoints -= amount;
            return true;
        }
        return false;
    }

    @Override public int getFaith() { return faith; }
    @Override public void setFaith(int amount) { this.faith = amount; }
    @Override public void addFaith(int amount) { this.faith += amount; }
    @Override public boolean consumeFaith(int amount) {
        if (faith >= amount) {
            faith -= amount;
            return true;
        }
        return false;
    }

    @Override public boolean hasVisitedVillage(String loc) { return visitedVillages.contains(loc); }
    @Override public void addVisitedVillage(String loc) { visitedVillages.add(loc); }

    @Override public void setPendingReward(ItemStack stack) { this.pendingReward = stack.copy(); }
    @Override public ItemStack getPendingReward() { return pendingReward; }
    @Override public boolean hasPendingReward() { return !pendingReward.isEmpty(); }
    @Override public void clearPendingReward() { this.pendingReward = ItemStack.EMPTY; }

    @Override public void setActiveTaskNBT(CompoundTag tag) { this.activeTaskNBT = tag; }
    @Override public CompoundTag getActiveTaskNBT() { return activeTaskNBT; }

    @Override public boolean areTentaclesActive() { return tentaclesActive; }
    @Override public void setTentaclesActive(boolean active) { this.tentaclesActive = active; }

    @Override
    public void saveNBT(CompoundTag nbt) {
        ListTag skillList = new ListTag();
        for (String skill : unlockedSkills) skillList.add(net.minecraft.nbt.StringTag.valueOf(skill));
        nbt.put("UnlockedSkills", skillList);

        ListTag villageList = new ListTag();
        for (String v : visitedVillages) villageList.add(net.minecraft.nbt.StringTag.valueOf(v));
        nbt.put("VisitedVillages", villageList);

        nbt.putInt("ZombieKills", zombieKills);
        nbt.putInt("EvoPoints", evoPoints);
        nbt.putInt("Faith", faith);
        nbt.put("ActiveTask", activeTaskNBT);
        nbt.putBoolean("TentaclesActive", tentaclesActive);

        if (!pendingReward.isEmpty()) {
            nbt.put("PendingReward", pendingReward.save(new CompoundTag()));
        }

        nbt.putInt("UserFollowerLimit", userFollowerLimit);
        ListTag allowedList = new ListTag();
        for (String type : allowedFollowerTypes) allowedList.add(net.minecraft.nbt.StringTag.valueOf(type));
        nbt.put("AllowedFollowers", allowedList);

        ListTag anchorList = new ListTag();
        for (BlockPos pos : knownAnchors) {
            anchorList.add(NbtUtils.writeBlockPos(pos));
        }
        nbt.put("SoulAnchors", anchorList);

        ListTag activeList = new ListTag();
        for (String s : activeAbilitiesSet) activeList.add(net.minecraft.nbt.StringTag.valueOf(s));
        nbt.put("ActiveAbilities", activeList);
    }

    @Override
    public void loadNBT(CompoundTag nbt) {
        unlockedSkills.clear();
        if (nbt.contains("UnlockedSkills")) {
            ListTag list = nbt.getList("UnlockedSkills", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) unlockedSkills.add(list.getString(i));
        }
        
        

        visitedVillages.clear();
        if (nbt.contains("VisitedVillages")) {
            ListTag list = nbt.getList("VisitedVillages", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) visitedVillages.add(list.getString(i));
        }

        zombieKills = nbt.getInt("ZombieKills");
        evoPoints = nbt.getInt("EvoPoints");
        faith = nbt.getInt("Faith");

        if (nbt.contains("ActiveTask")) activeTaskNBT = nbt.getCompound("ActiveTask");
        else activeTaskNBT = new CompoundTag();

        if (nbt.contains("TentaclesActive")) tentaclesActive = nbt.getBoolean("TentaclesActive");

        if (nbt.contains("PendingReward")) pendingReward = ItemStack.of(nbt.getCompound("PendingReward"));
        else pendingReward = ItemStack.EMPTY;

        if (nbt.contains("UserFollowerLimit")) userFollowerLimit = nbt.getInt("UserFollowerLimit");
        allowedFollowerTypes.clear();
        if (nbt.contains("AllowedFollowers")) {
            ListTag list = nbt.getList("AllowedFollowers", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) allowedFollowerTypes.add(list.getString(i));
        }

        knownAnchors.clear();
        if (nbt.contains("SoulAnchors")) {
            ListTag list = nbt.getList("SoulAnchors", 10);
            for (int i = 0; i < list.size(); i++) {
                knownAnchors.add(NbtUtils.readBlockPos(list.getCompound(i)));
            }
        }

        activeAbilitiesSet.clear();
        if (nbt.contains("ActiveAbilities")) {
            ListTag list = nbt.getList("ActiveAbilities", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) activeAbilitiesSet.add(list.getString(i));
        }
    }

    @Override
    public void copyFrom(ISculkMind source) {
        this.unlockedSkills.clear();
        this.unlockedSkills.addAll(source.getUnlockedSkills());
        this.zombieKills = source.getZombieKills();
        this.evoPoints = source.getEvoPoints();
        this.faith = source.getFaith();

        if (source instanceof SculkMind sm) {
            this.visitedVillages.clear();
            this.visitedVillages.addAll(sm.visitedVillages);
            this.pendingReward = sm.pendingReward.copy();
            this.activeTaskNBT = sm.activeTaskNBT.copy();
            this.tentaclesActive = false;

            this.userFollowerLimit = sm.userFollowerLimit;
            this.allowedFollowerTypes.clear();
            this.allowedFollowerTypes.addAll(sm.allowedFollowerTypes);

            this.knownAnchors.clear();
            this.knownAnchors.addAll(sm.knownAnchors);

            this.activeAbilitiesSet.clear();
        }
    }
}
