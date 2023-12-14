package net.ninjadev.destinations.data;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.ninjadev.destinations.Destinations;
import net.ninjadev.destinations.init.ModConfigs;
import net.ninjadev.destinations.util.DestinationStructure;

import java.util.*;
import java.util.stream.Collectors;

public class DestinationsState extends PersistentState {

    private static final String DATA_NAME = Destinations.MOD_ID + "_Data";

    private final Map<UUID, Set<Destination>> created = new HashMap<>();
    private final Map<UUID, Set<Destination>> stored = new HashMap<>();


    public Set<Destination> getDestinations(PlayerEntity player) {
        return ImmutableSet.copyOf(created.getOrDefault(player.getUuid(), new HashSet<>()));
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

    public Optional<Destination> getDestination(World world, BlockPos pos) {
        BlockPos top = DestinationStructure.findTop(world, pos);
        if (top == null) return Optional.empty();
        return this.created.values()
                .stream()
                .map(ArrayList::new)
                .flatMap(List::stream)
                .filter(destination -> destination.getWorld() == world.getRegistryKey() && destination.getY() == top.getY() && destination.getX() == top.getX() && destination.getZ() == top.getZ())
                .findFirst();
    }

    public boolean exists(World world, BlockPos pos) {
        BlockPos top = DestinationStructure.findTop(world, pos);
        if (top == null) return false;
        return this.created.values()
                .stream()
                .map(ArrayList::new)
                .flatMap(List::stream)
                .anyMatch(destination -> destination.getWorld() == world.getRegistryKey() && destination.getY() == top.getY() && destination.getX() == top.getX() && destination.getZ() == top.getZ());
    }

    public boolean add(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (created.containsKey(uuid)) {
            int max = ModConfigs.GENERAL.getMaxCreated();
            if (created.get(uuid).size() == max) {
                player.sendMessage(Text.literal(String.format("You have reached your destination limit of %s destinations.", max)).formatted(Formatting.GOLD), true);
                return false;
            }
            boolean added = created.get(uuid).add(destination);
            if (added) this.markDirty();
            return added;
        }
        Set<Destination> destinations = new HashSet<>();
        destinations.add(destination);
        created.put(uuid, destinations);
        this.markDirty();
        return true;
    }

    public boolean remove(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (!created.containsKey(uuid)) {
            return false;
        }

        Set<Destination> destinations = created.get(uuid);
        boolean removed = destinations.remove(destination);
        if (removed) this.markDirty();
        return removed;
    }

    public boolean addStored(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (stored.containsKey(uuid)) {
            if (stored.get(uuid).size() == 9) {
                player.sendMessage(Text.literal("You have reached your stored destination limit of 9 destinations.").formatted(Formatting.GOLD), true);
                return false;
            }
            boolean added = stored.get(uuid).add(destination);
            if (added) this.markDirty();
            return added;
        }
        Set<Destination> destinations = new HashSet<>();
        destinations.add(destination);
        stored.put(uuid, destinations);
        this.markDirty();
        return true;
    }

    public boolean removeStored(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (!stored.containsKey(uuid)) {
            return false;
        }

        Set<Destination> destinations = stored.get(uuid);
        boolean removed = destinations.remove(destination);
        if (removed) this.markDirty();
        return removed;
    }

    public Set<Destination> getStoredDestinations(PlayerEntity player) {
        return ImmutableSet.copyOf(stored.getOrDefault(player.getUuid(), new HashSet<>()));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound createdTag = new NbtCompound();
        NbtList createdList = new NbtList();
        for (Map.Entry<UUID, Set<Destination>> entry : this.created.entrySet()) {
            entry.getValue().forEach(destination -> createdList.add(destination.serialize()));
            createdTag.put(entry.getKey().toString(), createdList);
        }

        NbtCompound storedTag = new NbtCompound();
        NbtList storedList = new NbtList();
        for (Map.Entry<UUID, Set<Destination>> entry : stored.entrySet()) {
            entry.getValue().forEach(destination -> storedList.add(destination.serialize()));
            storedTag.put(entry.getKey().toString(), storedList);
        }

        nbt.put("created", createdTag);
        nbt.put("stored", storedTag);
        return nbt;
    }

    private static DestinationsState load(NbtCompound nbt) {
        DestinationsState state = new DestinationsState();
        NbtCompound createdNbt = nbt.getCompound("created");
        for (String key : createdNbt.getKeys()) {
            NbtList list = nbt.getList(key, NbtElement.COMPOUND_TYPE);
            Set<Destination> destinations = list.stream().map(tag -> (NbtCompound) tag).map(Destination::new).collect(Collectors.toSet());
            state.created.put(UUID.fromString(key), destinations);
        }
        NbtCompound storedNbt = nbt.getCompound("stored");
        for (String key : storedNbt.getKeys()) {
            NbtList list = nbt.getList(key, NbtElement.COMPOUND_TYPE);
            Set<Destination> destinations = list.stream().map(tag -> (NbtCompound) tag).map(Destination::new).collect(Collectors.toSet());
            state.stored.put(UUID.fromString(key), destinations);
        }
        return state;
    }

    public static DestinationsState get() {
        return Destinations.server.getOverworld()
                .getPersistentStateManager()
                .getOrCreate(DestinationsState::load, DestinationsState::new, DATA_NAME);
    }
}