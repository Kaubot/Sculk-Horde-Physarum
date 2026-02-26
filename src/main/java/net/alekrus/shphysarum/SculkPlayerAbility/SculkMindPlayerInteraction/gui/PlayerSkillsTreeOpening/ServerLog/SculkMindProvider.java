package net.alekrus.shphysarum.SculkPlayerAbility.SculkMindPlayerInteraction.gui.PlayerSkillsTreeOpening.ServerLog;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SculkMindProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<ISculkMind> SCULK_MIND = CapabilityManager.get(new CapabilityToken<>() {});

    private ISculkMind backend = null;
    private final LazyOptional<ISculkMind> optional = LazyOptional.of(this::createSculkMind);

    private ISculkMind createSculkMind() {
        if (backend == null) {
            backend = new SculkMind();
        }
        return backend;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == SCULK_MIND) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createSculkMind().saveNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createSculkMind().loadNBT(nbt);
    }
}
