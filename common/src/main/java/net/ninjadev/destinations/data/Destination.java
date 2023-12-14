package net.ninjadev.destinations.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ninjadev.destinations.util.INBTSerializable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Destination implements INBTSerializable<NbtCompound> {
    private UUID owner;
    private String name;
    private int x;
    private int y;
    private int z;
    private RegistryKey<World> world;

    public Destination(UUID owner, String name, int x, int y, int z, RegistryKey<World> world) {
        this.owner = owner;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public Destination(NbtCompound nbt) {
        this.deserialize(nbt);
    }



    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public RegistryKey<World> getWorld() {
        return world;
    }

    @Override
    public NbtCompound serialize() {
        NbtCompound compound = new NbtCompound();
        compound.putUuid("owner", this.owner);
        compound.putString("name", this.name);
        compound.putInt("x", this.x);
        compound.putInt("y", this.y);
        compound.putInt("z", this.z);
        compound.putString("world", world.getValue().toString());
        return compound;
    }

    @Override
    public void deserialize(NbtCompound nbt) {
        this.owner = nbt.getUuid("owner");
        this.name = nbt.getString("name");
        this.x = nbt.getInt("x");
        this.y = nbt.getInt("y");
        this.z = nbt.getInt("z");
        this.world = RegistryKey.of(RegistryKeys.WORLD, new Identifier(nbt.getString("world")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Destination that)) return false;

        if (x != that.x) return false;
        if (y != that.y) return false;
        if (z != that.z) return false;
        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + (world != null ? world.hashCode() : 0);
        return result;
    }
}
