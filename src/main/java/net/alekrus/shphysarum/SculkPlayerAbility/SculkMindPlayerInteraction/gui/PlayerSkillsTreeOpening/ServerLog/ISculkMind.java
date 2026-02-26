package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public interface ISculkMind {
    
    Set<String> getUnlockedSkills();
    void unlockSkill(String skillId);
    boolean hasSkill(String skillId);

    
    int getZombieKills();
    void incrementZombieKills();
    void setZombieKills(int amount);

    
    
    int getUserFollowerLimit();
    void setUserFollowerLimit(int limit);

    Set<String> getAllowedFollowerTypes();
    void setAllowedFollowerTypes(Set<String> types);
    


    
    int getEvoPoints();
    void setEvoPoints(int amount);
    void addEvoPoints(int amount);
    boolean consumeEvoPoints(int amount);

    
    Set<String> getActiveAbilitiesSet();
    void addActiveAbility(String skillId);
    void removeActiveAbility(String skillId);
    boolean isAbilityActive(String skillId);

    
    List<BlockPos> getKnownAnchors();
    boolean addKnownAnchor(BlockPos pos);
    void removeKnownAnchor(BlockPos pos);
    void setKnownAnchors(List<BlockPos> anchors);

    
    int getFaith();
    void setFaith(int amount);
    void addFaith(int amount);
    boolean consumeFaith(int amount);

    
    boolean hasVisitedVillage(String structureLoc);
    void addVisitedVillage(String structureLoc);

    
    void setPendingReward(ItemStack stack);
    ItemStack getPendingReward();
    boolean hasPendingReward();
    void clearPendingReward();

    
    void setActiveTaskNBT(CompoundTag tag);
    CompoundTag getActiveTaskNBT();

    
    boolean areTentaclesActive();
    void setTentaclesActive(boolean active);
    

    
    void saveNBT(CompoundTag nbt);
    void loadNBT(CompoundTag nbt);
    void copyFrom(ISculkMind source);
}
