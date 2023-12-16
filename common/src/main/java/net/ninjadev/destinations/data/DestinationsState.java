package net.ninjadev.destinations.data;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.ninjadev.destinations.util.DestinationStructure;

import java.util.*;

public abstract class DestinationsState extends PersistentState {

    protected final PlayerDestinations destinations = new PlayerDestinations();

    protected abstract int getMaxDestinations();

    public Set<Destination> getDestinationsFor(PlayerEntity player) {
        return ImmutableSet.copyOf(destinations.getOrDefault(player.getUuid(), new DestinationMap()).values());
    }

    public Set<Destination> getDestinationsForPlayerSorted(PlayerEntity player) {
        return ImmutableSet.copyOf(destinations.getOrDefault(player.getUuid(), new DestinationMap()).values().stream().sorted(Comparator.comparingInt(destination -> destination.getDistance(player))).toList());
    }

    public Optional<Destination> getDestinationForPlayerByLocation(PlayerEntity player, ServerWorld world, BlockPos origin) {
        return this.getDestinationsFor(player)
                .stream()
                .filter(destination -> destination.getWorld() == world.getRegistryKey() && destination.getY() == origin.getY() && destination.getX() == origin.getX() && destination.getZ() == origin.getZ())
                .findFirst();
    }

    public Optional<Destination> getDestinationByLocation(ServerWorld world, BlockPos origin) {
        return this.destinations.values()
                .stream()
                .map(HashMap::values)
                .map(ArrayList::new)
                .flatMap(List::stream)
                .filter(destination -> destination.getWorld() == world.getRegistryKey() && destination.getY() == origin.getY() && destination.getX() == origin.getX() && destination.getZ() == origin.getZ())
                .findFirst();
    }

    public Optional<Destination> getDestinationById(UUID id) {
        return this.destinations.values().stream().map(map -> map.get(id)).findFirst();
    }

    public Set<Destination> getAllDestinations() {
        return ImmutableSet.copyOf(this.destinations.values()
                .stream()
                .map(HashMap::values)
                .map(ArrayList::new)
                .flatMap(List::stream)
                .toList()
        );
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
        UUID playerId = player.getUuid();
        if (destinations.containsKey(playerId)) {
            DestinationMap playerDestinations = this.destinations.get(playerId);
            if (playerDestinations.containsKey(destination.getId())) return false;

            if (playerDestinations.size() >= this.getMaxDestinations()) {
                player.sendMessage(Text.literal(String.format("You have reached your destination limit of %s destinations.", this.getMaxDestinations())).formatted(Formatting.GOLD), true);
                return false;
            }

            playerDestinations.add(destination);
            this.markDirty();
            return true;
        }
        DestinationMap playerDestinations = new DestinationMap();
        playerDestinations.add(destination);
        this.destinations.put(playerId, playerDestinations);
        this.markDirty();
        return true;
    }

    public boolean remove(PlayerEntity player, Destination destination) {
        UUID playerId = player.getUuid();
        if (!this.destinations.containsKey(playerId)) return false;

        DestinationMap playerDestinations = this.destinations.get(playerId);
        if (!playerDestinations.exists(destination)) return false;

        playerDestinations.remove(destination);
        this.markDirty();
        return true;
    }

    public void removeFromAllPlayers(Destination destination) {
        for (DestinationMap map : this.destinations.values()) {
            List<Destination> toRemove = new ArrayList<>();
            for (Destination other : map.values()) {
                if (other.getId().equals(destination.getId())) {
                    toRemove.add(other);
                }
            }
            for (Destination remove : toRemove) {
                map.remove(remove);
            }
        }
        this.markDirty();
    }
}
