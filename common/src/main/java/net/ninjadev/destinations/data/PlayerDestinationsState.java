package net.ninjadev.destinations.data;

import net.minecraft.nbt.NbtCompound;
import net.ninjadev.destinations.Destinations;

public class PlayerDestinationsState extends DestinationsState {
    private static final String DATA_NAME = Destinations.MOD_ID + "_PlayerDestinations";

    @Override
    protected int getMaxDestinations() {
        return 9;
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
