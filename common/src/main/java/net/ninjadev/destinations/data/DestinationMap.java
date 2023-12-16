package net.ninjadev.destinations.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.ninjadev.destinations.util.INBTSerializable;

import java.util.HashMap;
import java.util.UUID;

public class DestinationMap extends HashMap<UUID, Destination> implements INBTSerializable<NbtList> {

    public DestinationMap() {
    }

    public DestinationMap(NbtList nbtList) {
        this.deserialize(nbtList);
    }

    public Destination add(Destination destination) {
        return this.put(destination.getId(), destination);
    }

    public Destination remove(Destination destination) {
        return this.remove(destination.getId());
    }

    public boolean exists(Destination destination) {
        return this.containsKey(destination.getId());
    }

    @Override
    public NbtList serialize() {
        NbtList list = new NbtList();
        this.values().forEach(destination -> list.add(destination.serialize()));
        return list;
    }

    @Override
    public void deserialize(NbtList nbtList) {
        nbtList.stream().map(tag -> (NbtCompound) tag).map(Destination::new).forEach(this::add);
    }
}
