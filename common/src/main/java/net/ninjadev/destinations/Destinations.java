package net.ninjadev.destinations;

import net.minecraft.server.MinecraftServer;
import net.ninjadev.destinations.events.DestinationEventHandler;
import net.ninjadev.destinations.init.ModConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Destinations {
    public static final String MOD_ID = "destinations";
    public static final Logger LOGGER = LoggerFactory.getLogger(Destinations.class);
    public static MinecraftServer server;

    private static DestinationEventHandler eventHandler;

    public static DestinationEventHandler getEventHandler() {
        if (eventHandler == null) {
            eventHandler = new DestinationEventHandler();
        }
        return eventHandler;
    }

    public static void init() {
        ModConfigs.register();
    }


}
