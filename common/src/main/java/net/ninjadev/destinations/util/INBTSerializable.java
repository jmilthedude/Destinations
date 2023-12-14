package net.ninjadev.destinations.util;

import net.minecraft.nbt.NbtElement;

public interface INBTSerializable<N extends NbtElement> {

    N serialize();
    void deserialize(N nbt);
}
