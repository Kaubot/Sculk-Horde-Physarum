package net.alekrus.shphysarum.Config;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue PLAY_RAID_BEACON_MUSIC;

    static {
        BUILDER.push("Music Settings");

        PLAY_RAID_BEACON_MUSIC = BUILDER
                .comment("If true, custom music will play during the Charge from the beacon.")
                .define("playRaidBeaconMusic", true);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}