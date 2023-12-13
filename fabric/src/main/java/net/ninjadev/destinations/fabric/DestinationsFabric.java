package net.ninjadev.destinations.fabric;

import net.ninjadev.destinations.Destinations;
import net.fabricmc.api.ModInitializer;

public class DestinationsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Destinations.init();
    }
}