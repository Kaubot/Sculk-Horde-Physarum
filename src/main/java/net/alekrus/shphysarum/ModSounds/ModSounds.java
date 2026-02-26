package net.alekrus.shphysarum.ModSounds;

import net.alekrus.shphysarum.shPhysarum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, shPhysarum.MODID);

    
    
    public static final RegistryObject<SoundEvent> RAID_MUSIC = registerSoundEvent("raid_music");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(shPhysarum.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(location));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}