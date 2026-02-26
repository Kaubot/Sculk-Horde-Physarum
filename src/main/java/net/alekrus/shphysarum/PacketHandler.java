package net.alekrus.shphysarum;

import net.alekrus.shphysarum.AnimationAll.PlayerVisualSyncPacket;
import net.alekrus.shphysarum.Block.BeaconUnlockSyncPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.ExpirienseStore.NutrientActionPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.FastHealing.ImmediateActionStatePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.InfectedAroundPlayer.SporeBurstPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.PlayerBurrowInSculk.SculkBurrowPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.PlayerBurrowInSculk.SculkBurrowSyncPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.PlayerIntecraftSculkSummoner.RaidRequestPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.PlayerJumpCrosshair.SculkLeapPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.RaidPlayerInitiator.RaidStartPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindMessagePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.DialogHandler.GravemindScenePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.FaithSystem.FaithSyncPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.SupportTabInteraction.SupportPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.TaskActionPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.TasksGravemindPlayer.TaskProgressPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.MiniGame.SynapticGamePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillSyncPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMoveTo.ConnectionConfigPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkPhantomFly.PhantomFlightPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkVision.SculkVisionStatePacket;
import net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility.TentacleActionPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.TentaclePlayerAbility.TentacleTogglePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMoveTo.SculkCommandPacket;
import net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog.SkillBuyPacket;

public class PacketHandler {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("shphysarum", "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void register() {
        int id = 0;

        
        CHANNEL.registerMessage(id++, SculkCommandPacket.class,
                SculkCommandPacket::encode,
                SculkCommandPacket::decode,
                SculkCommandPacket::handle
        );

        
        CHANNEL.registerMessage(id++, RaidRequestPacket.class,
                RaidRequestPacket::encode,
                RaidRequestPacket::decode,
                RaidRequestPacket::handle
        );

        
        CHANNEL.registerMessage(id++, SporeBurstPacket.class,
                SporeBurstPacket::encode,
                SporeBurstPacket::decode,
                SporeBurstPacket::handle);

        
        CHANNEL.registerMessage(id++, RaidStartPacket.class,
                RaidStartPacket::encode,
                RaidStartPacket::decode,
                RaidStartPacket::handle);

        
        CHANNEL.registerMessage(id++, SupportPacket.class,
                SupportPacket::encode,
                SupportPacket::decode,
                SupportPacket::handle);

        
        CHANNEL.registerMessage(id++, FaithSyncPacket.class,
                FaithSyncPacket::encode,
                FaithSyncPacket::decode,
                FaithSyncPacket::handle);

        
        CHANNEL.registerMessage(id++, GravemindScenePacket.class,
                GravemindScenePacket::encode,
                GravemindScenePacket::decode,
                GravemindScenePacket::handle);


        CHANNEL.registerMessage(id++, TaskActionPacket.class,
                TaskActionPacket::encode,
                TaskActionPacket::decode,
                TaskActionPacket::handle);

        CHANNEL.registerMessage(id++, TaskProgressPacket.class,
                TaskProgressPacket::encode,
                TaskProgressPacket::decode,
                TaskProgressPacket::handle);

        CHANNEL.registerMessage(id++, SkillSyncPacket.class,
                SkillSyncPacket::encode,
                SkillSyncPacket::decode,
                SkillSyncPacket::handle);


        CHANNEL.registerMessage(id++, SkillBuyPacket.class,
                SkillBuyPacket::encode,
                SkillBuyPacket::decode,
                SkillBuyPacket::handle);



        CHANNEL.registerMessage(id++, SculkLeapPacket.class,
                SculkLeapPacket::encode,
                SculkLeapPacket::decode,
                SculkLeapPacket::handle);

        CHANNEL.registerMessage(id++, SculkBurrowPacket.class,
                SculkBurrowPacket::encode,
                SculkBurrowPacket::decode,
                SculkBurrowPacket::handle);

        CHANNEL.registerMessage(id++, SculkBurrowSyncPacket.class,
                SculkBurrowSyncPacket::encode,
                SculkBurrowSyncPacket::decode,
                SculkBurrowSyncPacket::handle);

        CHANNEL.registerMessage(id++, SynapticGamePacket.class,
                SynapticGamePacket::encode,
                SynapticGamePacket::decode,
                SynapticGamePacket::handle);

        CHANNEL.registerMessage(id++, TentacleTogglePacket.class, TentacleTogglePacket::encode, TentacleTogglePacket::decode, TentacleTogglePacket::handle);
        CHANNEL.registerMessage(id++, TentacleActionPacket.class, TentacleActionPacket::encode, TentacleActionPacket::decode, TentacleActionPacket::handle);

        CHANNEL.registerMessage(id++, ImmediateActionStatePacket.class,
                ImmediateActionStatePacket::encode,
                ImmediateActionStatePacket::decode,
                ImmediateActionStatePacket::handle);
        CHANNEL.registerMessage(id++, ConnectionConfigPacket.class,
                ConnectionConfigPacket::encode,
                ConnectionConfigPacket::decode,
                ConnectionConfigPacket::handle);

        CHANNEL.registerMessage(id++,
                SculkVisionStatePacket.class,
                SculkVisionStatePacket::encode,
                SculkVisionStatePacket::decode,
                SculkVisionStatePacket::handle
        );

        CHANNEL.registerMessage(id++,
                BeaconUnlockSyncPacket.class,
                BeaconUnlockSyncPacket::encode,
                BeaconUnlockSyncPacket::decode,
                BeaconUnlockSyncPacket::handle);

        CHANNEL.messageBuilder(PhantomFlightPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(PhantomFlightPacket::decode)
                .encoder(PhantomFlightPacket::encode)
                .consumerMainThread(PhantomFlightPacket::handle)
                .add();

        CHANNEL.registerMessage(id++, NutrientActionPacket.class,
                NutrientActionPacket::encode,
                NutrientActionPacket::decode,
                NutrientActionPacket::handle);
        CHANNEL.registerMessage(id++, PlayerVisualSyncPacket.class, PlayerVisualSyncPacket::encode, PlayerVisualSyncPacket::decode, PlayerVisualSyncPacket::handle);

        CHANNEL.registerMessage(id++, GravemindMessagePacket.class,
                GravemindMessagePacket::encode,
                GravemindMessagePacket::decode, 
                GravemindMessagePacket::handle);


    }


}
