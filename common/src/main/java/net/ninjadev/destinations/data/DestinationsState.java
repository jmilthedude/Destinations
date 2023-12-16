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

public class DestinationsState extends PersistentState {

    private static final String DATA_NAME = Destinations.MOD_ID + "_Data";

    private final DestinationMap destinations = new DestinationMap();


    public Set<Destination> getCreatedDestinations(PlayerEntity player) {
        return ImmutableSet.copyOf(destinations.getOrDefault(player.getUuid(), new HashMap<>()).values());
    }

    public Optional<Destination> getDestination(PlayerEntity player, World world, BlockPos pos) {
        Optional<BlockPos> originOptional = DestinationStructure.findOrigin(world, pos);
        if (originOptional.isEmpty()) return Optional.empty();
        BlockPos origin = originOptional.get();
        Set<Destination> destinations = this.getCreatedDestinations(player);
        for (Destination destination : destinations) {
            if (!destination.getWorld().equals(world.getRegistryKey())) continue;
            if (destination.getX() == origin.getX() && destination.getY() == origin.getY() && destination.getZ() == origin.getZ()) {
                return Optional.of(destination);
            }
        }
        return Optional.empty();
    }

    public Optional<Destination> getDestinationById(UUID id) {
        return this.destinations.values().stream().map(map -> map.get(id)).findFirst();
    }

    public Optional<Destination> getDestination(World world, BlockPos pos) {
        Optional<BlockPos> originOptional = DestinationStructure.findOrigin(world, pos);
        if (originOptional.isEmpty()) return Optional.empty();
        BlockPos origin = originOptional.get();
        return this.destinations.values()
                .stream()
                .map(HashMap::values)
                .map(ArrayList::new)
                .flatMap(List::stream)
                .filter(destination -> destination.getWorld() == world.getRegistryKey() && destination.getY() == origin.getY() && destination.getX() == origin.getX() && destination.getZ() == origin.getZ())
                .findFirst();
    }

    public boolean exists(World world, BlockPos pos) {
        Optional<BlockPos> originOptional = DestinationStructure.findOrigin(world, pos);
        if (originOptional.isEmpty()) return false;
        BlockPos origin = originOptional.get();
        return this.destinations.values()
                .stream()
                .map(HashMap::values)
                .map(ArrayList::new)
                .flatMap(List::stream)
                .anyMatch(destination -> destination.getWorld() == world.getRegistryKey() && destination.getY() == origin.getY() && destination.getX() == origin.getX() && destination.getZ() == origin.getZ());
    }

    public boolean add(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (destinations.containsKey(uuid)) {
            int max = ModConfigs.GENERAL.getMaxCreated();
            HashMap<UUID, Destination> map = destinations.get(uuid);
            if (map.size() == max) {
                player.sendMessage(Text.literal(String.format("You have reached your destination limit of %s destinations.", max)).formatted(Formatting.GOLD), true);
                return false;
            }
            if (map.containsKey(destination.getId())) return false;

            map.put(destination.getId(), destination);
            this.markDirty();
            return true;
        }
        HashMap<UUID, Destination> destinations = new HashMap<>();
        destinations.put(destination.getId(), destination);
        this.destinations.put(uuid, destinations);
        this.markDirty();
        return true;
    }

    public boolean remove(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (!destinations.containsKey(uuid)) {
            return false;
        }

        HashMap<UUID, Destination> destinations = this.destinations.get(uuid);
        if (!destinations.containsKey(destination.getId())) {
            return false;
        }
        destinations.remove(destination.getId());
        this.markDirty();
        return true;
    }

    public boolean addStored(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (stored.containsKey(uuid)) {
            HashMap<UUID, Destination> map = stored.get(uuid);
            if (map.size() == 9) {
                player.sendMessage(Text.literal("You have reached your destination limit of 9 destinations.").formatted(Formatting.GOLD), true);
                return false;
            }
            if (map.containsKey(destination.getId())) return false;

            map.put(destination.getId(), destination);
            this.markDirty();
            return true;
        }
        HashMap<UUID, Destination> destinations = new HashMap<>();
        destinations.put(destination.getId(), destination);
        stored.put(uuid, destinations);
        this.markDirty();
        return true;
    }

    public boolean removeStored(PlayerEntity player, Destination destination) {
        UUID uuid = player.getUuid();
        if (!stored.containsKey(uuid)) {
            return false;
        }

        HashMap<UUID, Destination> destinations = stored.get(uuid);
        if (!destinations.containsKey(destination.getId())) {
            return false;
        }
        destinations.remove(destination.getId());
        this.markDirty();
        return true;
    }

    public boolean removeStored(Destination destination) {
        boolean removed = this.stored.values().removeIf(map -> map.get(destination.getId()) != null);

        if (removed) this.markDirty();

        return removed;
    }

    public Set<Destination> getStoredDestinations(PlayerEntity player) {
        return ImmutableSet.copyOf(stored.getOrDefault(player.getUuid(), new HashMap<>()).values());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound createdTag = new NbtCompound();
        NbtList createdList = new NbtList();
        for (Map.Entry<UUID, HashMap<UUID, Destination>> entry : this.destinations.entrySet()) {
            entry.getValue().forEach((id, destination) -> createdList.add(destination.serialize()));
            createdTag.put(entry.getKey().toString(), createdList);
        }

        nbt.put("created", createdTag);
        return nbt;
    }

    private static DestinationsState load(NbtCompound nbt) {
        DestinationsState state = new DestinationsState();
        NbtCompound createdNbt = nbt.getCompound("created");
        for (String key : createdNbt.getKeys()) {
            NbtList list = nbt.getList(key, NbtElement.COMPOUND_TYPE);
            HashMap<UUID, Destination> destinations = new HashMap<>();
            list.stream().map(tag -> (NbtCompound) tag).map(Destination::new).forEach(destination -> destinations.put(destination.getId(), destination));
            state.destinations.put(UUID.fromString(key), destinations);
        }
        return state;
    }

    public static DestinationsState get() {
        return Destinations.server.getOverworld()
                .getPersistentStateManager()
                .getOrCreate(DestinationsState::load, DestinationsState::new, DATA_NAME);
    }
}
