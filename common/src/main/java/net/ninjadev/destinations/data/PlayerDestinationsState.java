package net.ninjadev.destinations.data;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;
import net.ninjadev.destinations.Destinations;

import java.util.*;

public class PlayerDestinationsState extends PersistentState {
    private static final String DATA_NAME = Destinations.MOD_ID + "_PlayerDestinations";
    private final PlayerDestinations destinations = new PlayerDestinations();

    public Set<Destination> getDestinations(PlayerEntity player) {
        return ImmutableSet.copyOf(destinations.getOrDefault(player.getUuid(), new DestinationMap()).values());
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

//    public Optional<Destination> getDestinationByPosition(World world, BlockPos pos) {
//        Optional<BlockPos> originOptional = DestinationStructure.findOrigin(world, pos);
//        if (originOptional.isEmpty()) return Optional.empty();
//        BlockPos origin = originOptional.get();
//        this.getDestinations()
//    }

    public boolean add(PlayerEntity player, Destination destination) {
        UUID playerId = player.getUuid();
        if (destinations.containsKey(playerId)) {
            DestinationMap playerDestinations = this.destinations.get(playerId);
            if (playerDestinations.size() >= 9) {
                player.sendMessage(Text.literal("You have reached your destination limit of 9 destinations.").formatted(Formatting.GOLD), true);
                return false;
            }

            if (playerDestinations.containsKey(destination.getId())) return false;

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

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("playerDestinations", this.destinations.serialize());
        return nbt;
    }

    private static PlayerDestinationsState load(NbtCompound nbt) {
        PlayerDestinationsState state = new PlayerDestinationsState();
        NbtCompound playerDestinations = nbt.getCompound("playerDestinations");
        state.destinations.deserialize(playerDestinations);
        return state;
    }


    public static PlayerDestinationsState get() {
        return Destinations.server.getOverworld()
                .getPersistentStateManager()
                .getOrCreate(PlayerDestinationsState::load, PlayerDestinationsState::new, DATA_NAME);
    }
}
