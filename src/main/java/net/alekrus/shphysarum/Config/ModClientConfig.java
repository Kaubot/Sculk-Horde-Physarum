package net.alekrus.shphysarum.Config;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue PLAY_RAID_BEACON_MUSIC;
    public static final ForgeConfigSpec.IntValue BEACON_PLATFORM_RADIUS; 

    static {
        BUILDER.push("Music Settings");
        PLAY_RAID_BEACON_MUSIC = BUILDER
                .comment("If true, custom music will play during the Charge from the beacon.")
                .define("playRaidBeaconMusic", true);
        BUILDER.pop();

        BUILDER.push("Gameplay Settings");
        BEACON_PLATFORM_RADIUS = BUILDER
                .comment("Radius of the solid platform required beneath the beacon. 5 = 10x10, 20 = 40x40. Minimum is 5.")
                .defineInRange("beaconPlatformRadius", 20, 5, 50);
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
