package net.ninjadev.destinations.init;

import net.ninjadev.destinations.config.DestinationsConfig;

public class ModConfigs {

    public static DestinationsConfig GENERAL;

    public static void register() {
        GENERAL = new DestinationsConfig().readConfig();
    }
}
