package net.ninjadev.destinations.data;

import net.minecraft.nbt.NbtCompound;
import net.ninjadev.destinations.Destinations;
import net.ninjadev.destinations.init.ModConfigs;

public class GlobalDestinationsState extends DestinationsState {

    private static final String DATA_NAME = Destinations.MOD_ID + "_GlobalDestinations";

    @Override
    protected int getMaxDestinations() {
        return ModConfigs.GENERAL.getMaxCreated();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.put("globalDestinations", this.destinations.serialize());
        return nbt;
    }

    private static GlobalDestinationsState load(NbtCompound nbt) {
        GlobalDestinationsState state = new GlobalDestinationsState();
        NbtCompound playerDestinations = nbt.getCompound("globalDestinations");
        state.destinations.deserialize(playerDestinations);
        return state;
    }


    public static GlobalDestinationsState get() {
        return Destinations.server.getOverworld()
                .getPersistentStateManager()
                .getOrCreate(GlobalDestinationsState::load, GlobalDestinationsState::new, DATA_NAME);
    }
}
