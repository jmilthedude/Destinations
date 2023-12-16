package net.ninjadev.destinations.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.ninjadev.destinations.util.INBTSerializable;

import java.util.HashMap;
import java.util.UUID;

public class PlayerDestinations extends HashMap<UUID, DestinationMap> implements INBTSerializable<NbtCompound> {

    @Override
    public NbtCompound serialize() {
        NbtCompound nbt = new NbtCompound();
        this.forEach((key, value) -> nbt.put(key.toString(), value.serialize()));
        return nbt;
    }

    @Override
    public void deserialize(NbtCompound nbt) {
        for (String key : nbt.getKeys()) {
            UUID id = UUID.fromString(key);
            this.put(id, new DestinationMap(nbt.getList(key, NbtElement.COMPOUND_TYPE)));
        }
    }
}
