package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GravemindTask {

    public enum Type {
        KILL_PILLAGERS,
        KILL_VILLAGERS,
        SCOUT_LOCATION,
        INFECT_AREA,
        GIVE_ITEMS,
        FIND_NEW_VILLAGE,
        KILL_WARDEN,
        OFFER_NETHER_STAR,
        SACRIFICE_LEVELS 
    }

    public Type type;
    public int requiredAmount;
    public int currentAmount;
    public int faithReward;
    public int evoReward;
    public BlockPos targetLocation;
    public String locationName;
    public ItemStack requiredItem = ItemStack.EMPTY;
    public ItemStack rewardItem = ItemStack.EMPTY;

    public GravemindTask(Type type, int required, int faithReward, int evoReward) {
        this.type = type;
        this.requiredAmount = required;
        this.currentAmount = 0;
        this.faithReward = faithReward;
        this.evoReward = evoReward;
        this.targetLocation = BlockPos.ZERO;
        this.locationName = "";
    }

    
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Type", type.ordinal());
        tag.putInt("Req", requiredAmount);
        tag.putInt("Cur", currentAmount);
        tag.putInt("RewFaith", faithReward);
        tag.putInt("RewEvo", evoReward);
        tag.putLong("Pos", targetLocation.asLong());
        if (!requiredItem.isEmpty()) tag.put("ReqItem", requiredItem.save(new CompoundTag()));
        if (!rewardItem.isEmpty()) tag.put("RewItem", rewardItem.save(new CompoundTag()));
        return tag;
    }

    
    public static GravemindTask load(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return null;
        Type t = Type.values()[tag.getInt("Type")];
        int req = tag.getInt("Req");
        int faith = tag.getInt("RewFaith");
        int evo = tag.getInt("RewEvo");

        GravemindTask task = new GravemindTask(t, req, faith, evo);
        task.currentAmount = tag.getInt("Cur");
        task.targetLocation = BlockPos.of(tag.getLong("Pos"));
        if (!task.targetLocation.equals(BlockPos.ZERO)) task.locationName = task.targetLocation.toShortString();

        if (tag.contains("ReqItem")) task.requiredItem = ItemStack.of(tag.getCompound("ReqItem"));
        if (tag.contains("RewItem")) task.rewardItem = ItemStack.of(tag.getCompound("RewItem"));

        return task;
    }

    public GravemindTask setRequiredItem(ItemStack stack) { this.requiredItem = stack; return this; }
    public GravemindTask setRewardItem(ItemStack stack) { this.rewardItem = stack; return this; }
    public boolean isComplete() { return currentAmount >= requiredAmount; }

    public Component getDescription() {
        switch (type) {
            case KILL_PILLAGERS: return Component.literal("Eliminate Pillagers");
            case KILL_VILLAGERS: return Component.literal("Consume Villagers");
            case SCOUT_LOCATION: return Component.literal("Scout Area: " + locationName);
            case INFECT_AREA: return Component.literal("Spread Infection near " + locationName);
            case KILL_WARDEN: return Component.literal("Eliminate the Warden");
            case FIND_NEW_VILLAGE: return Component.literal("Discover a New Village");
            case OFFER_NETHER_STAR: return Component.literal("Bring Nether Star to Node");
            case SACRIFICE_LEVELS: return Component.literal("Sacrifice 30 XP Levels to Node"); 
            case GIVE_ITEMS:
                if (!requiredItem.isEmpty()) return Component.literal("Offer " + requiredItem.getHoverName().getString());
                return Component.literal("Offer Sacrifice");
            default: return Component.literal("Unknown Directive");
        }
    }
}
