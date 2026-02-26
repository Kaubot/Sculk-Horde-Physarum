package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ClientSkillData;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.ClientGravemindState;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.GravemindTask;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.TaskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SkillSyncPacket {
    private final Set<String> skills;
    private final int evoPoints;
    private final int faith;
    private final CompoundTag taskData;
    private final boolean tentaclesActive;
    private final int userLimit;
    private final Set<String> allowedFollowers;
    private final List<BlockPos> anchors;

    
    private final Set<String> activeAbilities;

    
    public SkillSyncPacket(Set<String> skills, int evoPoints, int faith, CompoundTag taskData,
                           boolean tentaclesActive, int userLimit, Set<String> allowedFollowers,
                           List<BlockPos> anchors, Set<String> activeAbilities) {
        this.skills = skills;
        this.evoPoints = evoPoints;
        this.faith = faith;
        this.taskData = taskData;
        this.tentaclesActive = tentaclesActive;
        this.userLimit = userLimit;
        this.allowedFollowers = allowedFollowers;
        this.anchors = anchors;
        this.activeAbilities = activeAbilities; 
    }

    public static SkillSyncPacket decode(FriendlyByteBuf buf) {
        int points = buf.readInt();
        int faith = buf.readInt();
        CompoundTag task = buf.readNbt();
        boolean tentacles = buf.readBoolean();

        int size = buf.readInt();
        Set<String> skills = new HashSet<>();
        for (int i = 0; i < size; i++) skills.add(buf.readUtf());

        int uLim = buf.readInt();
        int aSize = buf.readInt();
        Set<String> aFollowers = new HashSet<>();
        for(int i=0; i<aSize; i++) aFollowers.add(buf.readUtf());

        int anchorCount = buf.readInt();
        List<BlockPos> anchorList = new ArrayList<>();
        for(int i = 0; i < anchorCount; i++) anchorList.add(buf.readBlockPos());

        
        int activeCount = buf.readInt();
        Set<String> activeSet = new HashSet<>();
        for(int i = 0; i < activeCount; i++) activeSet.add(buf.readUtf());

        return new SkillSyncPacket(skills, points, faith, task, tentacles, uLim, aFollowers, anchorList, activeSet);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(evoPoints);
        buf.writeInt(faith);
        buf.writeNbt(taskData);
        buf.writeBoolean(tentaclesActive);

        buf.writeInt(skills.size());
        for (String skill : skills) buf.writeUtf(skill);

        buf.writeInt(userLimit);
        buf.writeInt(allowedFollowers.size());
        for (String s : allowedFollowers) buf.writeUtf(s);

        buf.writeInt(anchors.size());
        for (BlockPos pos : anchors) buf.writeBlockPos(pos);

        
        buf.writeInt(activeAbilities.size());
        for (String s : activeAbilities) buf.writeUtf(s);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientSkillData.sync(skills, evoPoints);
            ClientSkillData.setFaith(faith);
            ClientGravemindState.setFaith(faith);
            ClientGravemindState.syncTask(taskData);
            ClientGravemindState.tentaclesActive = this.tentaclesActive;
            ClientSkillData.syncConnectionData(userLimit, allowedFollowers);
            ClientSkillData.setAnchors(anchors);

            
            ClientSkillData.syncActiveAbilities(activeAbilities);
            

            if (taskData != null && !taskData.isEmpty()) {
                TaskManager.clientCurrentTask = GravemindTask.load(taskData);
            } else {
                TaskManager.clientCurrentTask = null;
            }
        });
        context.setPacketHandled(true);
    }
}
