package net.ninjadev.destinations.data;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.ninjadev.destinations.Destinations;
import net.ninjadev.destinations.util.DestinationStructure;

import java.util.*;
import java.util.stream.Collectors;

public class DestinationsState extends PersistentState {
    private static final String DATA_NAME = Destinations.MOD_ID + "_Data";

    private final Map<UUID, Set<Destination>> entries = new HashMap<>();


    public Set<Destination> getDestinations(PlayerEntity player) {
        return ImmutableSet.copyOf(entries.getOrDefault(player.getUuid(), new HashSet<>()));
    }

    public Optional<Destination> getDestination(PlayerEntity player, World world, BlockPos pos) {
        BlockPos top = DestinationStructure.findTop(world, pos);
        if (top == null) return Optional.empty();
        Set<Destination> destinations = this.getDestinations(player);
        for (Destination destination : destinations) {
            if (!destination.getWorld().equals(world.getRegistryKey())) continue;
            if (destination.getX() == top.getX() && destination.getY() == top.getY() && destination.getZ() == top.getZ()) {
                return Optional.of(destination);
            }
        }
        return Optional.empty();
    }

    public boolean exists(World world, BlockPos pos) {
        if (!DestinationStructure.isValid(world, pos)) return false;
        BlockPos top = DestinationStructure.findTop(world, pos);
        if (top == null) return false;
        return this.entries.values()
                .stream()
                .map(ArrayList::new)
                .flatMap(List::stream)
                .anyMatch(destination -> destination.getWorld() == world.getRegistryKey() && destination.getY() == top.getY() && destination.getX() == top.getX() && destination.getZ() == top.getZ());
    }

    public boolean add(PlayerEntity player, Destination destination) {
        return this.add(player.getUuid(), destination);
    }

    public boolean add(UUID uuid, Destination destination) {
        if (entries.containsKey(uuid)) {
            if (entries.get(uuid).size() == 9) return false;
            boolean added = entries.get(uuid).add(destination);
            if (added) this.markDirty();
            return added;
        }
        Set<Destination> destinations = new HashSet<>();
        destinations.add(destination);
        entries.put(uuid, destinations);
        this.markDirty();
        return true;
    }

    public boolean remove(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (!entries.containsKey(uuid)) {
            return false;
        }

        Set<Destination> destinations = entries.get(uuid);
        boolean removed = destinations.remove(destination);
        if (removed) this.markDirty();
        return removed;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (Map.Entry<UUID, Set<Destination>> entry : entries.entrySet()) {
            entry.getValue().forEach(destination -> list.add(destination.serialize()));
            nbt.put(entry.getKey().toString(), list);
        }
        return nbt;
    }

    private static DestinationsState load(NbtCompound nbt) {
        DestinationsState state = new DestinationsState();
        for (String key : nbt.getKeys()) {
            NbtList list = nbt.getList(key, NbtElement.COMPOUND_TYPE);
            Set<Destination> destinations = list.stream().map(tag -> (NbtCompound) tag).map(Destination::new).collect(Collectors.toSet());
            state.entries.put(UUID.fromString(key), destinations);
        }
        return state;
    }

    public static DestinationsState get() {
        return Destinations.server.getOverworld()
                .getPersistentStateManager()
                .getOrCreate(DestinationsState::load, DestinationsState::new, DATA_NAME);
    }
}
