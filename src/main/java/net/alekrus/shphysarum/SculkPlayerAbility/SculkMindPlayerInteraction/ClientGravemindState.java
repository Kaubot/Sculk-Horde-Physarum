package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction;

import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.GravemindTask;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.TaskManager;
import net.minecraft.nbt.CompoundTag;

import java.util.LinkedList;
import java.util.Queue;

public class ClientGravemindState {

    private static boolean hasUnreadDialogue = false;
    private static int faith = 0;
    public static boolean hasSeenBeacon = false;

    public static boolean isBrainMaxed = false;
    public static boolean isBodyMaxed = false;
    public static boolean tentaclesActive = false;
    public static Queue<String> messageQueue = new LinkedList<>();
    public static String currentMessage = "";
    public static int displayTicks = 0;
    public static boolean isOverlayActive = false;

    
    public static boolean isBlocking = false; 
    public static long lastAttackTime = 0;    
    

    public static void setUnreadDialogue(boolean value) { hasUnreadDialogue = value; }
    public static boolean hasUnreadDialogue() { return hasUnreadDialogue; }

    public static int getFaith() {
        if (isBrainMaxed && isBodyMaxed) return 999999;
        return faith;
    }
    public static void setFaith(int amount) { faith = amount; }
    public static void addFaith(int amount) { faith += amount; }

    public static void queueMessage(String message) {
        if (isOverlayActive && currentMessage != null && currentMessage.equals(message)) {
            return;
        }
        if (messageQueue.contains(message)) {
            return;
        }
        messageQueue.add(message);
        if (!isOverlayActive) {
            currentMessage = messageQueue.poll();
            isOverlayActive = true;
            displayTicks = 0;
        }
    }

    public static void setTentaclesActive(boolean active) {
        tentaclesActive = active;
        
        if (!active) {
            isBlocking = false;
        }
    }

    
    public static void triggerAttackAnimation() {
        lastAttackTime = System.currentTimeMillis();
    }
    

    public static void syncTask(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            TaskManager.clientCurrentTask = null;
        } else {
            TaskManager.clientCurrentTask = GravemindTask.load(tag);
        }
    }

    public static void tick() {
        if (isOverlayActive) {
            displayTicks++;
            if (displayTicks >= 72) {
                isOverlayActive = false;
                displayTicks = 0;
                if (!messageQueue.isEmpty()) {
                    currentMessage = messageQueue.poll();
                    isOverlayActive = true;
                }
            }
        } else {
            if (!messageQueue.isEmpty()) {
                currentMessage = messageQueue.poll();
                isOverlayActive = true;
                displayTicks = 0;
            }
        }
    }
}
