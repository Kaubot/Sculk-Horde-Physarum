package net.alekrus.shphysarum.ModSounds;


import net.alekrus.shphysarum.Block.SculkBeaconBlockEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;


public class RaidMusicInstance extends AbstractTickableSoundInstance {
    private final SculkBeaconBlockEntity beacon;
    private boolean fadingOut = false;

    public RaidMusicInstance(SculkBeaconBlockEntity beacon) {
        
        super(ModSounds.RAID_MUSIC.get(), SoundSource.RECORDS, net.minecraft.util.RandomSource.create());
        this.beacon = beacon;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.01f; 

        
        this.relative = true; 
        this.attenuation = Attenuation.NONE; 

        
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    @Override
    public void tick() {
        
        if (this.beacon.isRemoved()) {
            this.stop();
            return;
        }

        
        if (this.beacon.isRaidActive() && !fadingOut) {
            
            if (this.volume < 1.0f) {
                this.volume += 0.01f;
            }
        } else {
            
            this.fadingOut = true;
            this.volume -= 0.02f;

            if (this.volume <= 0.0f) {
                this.stop();
            }
        }
    }
}