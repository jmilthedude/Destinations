package net.ninjadev.destinations;

import net.minecraft.server.MinecraftServer;
import net.ninjadev.destinations.init.ModConfigs;

public class Destinations
{
	public static final String MOD_ID = "destinations";
	public static MinecraftServer server;

	public static void init() {
		ModConfigs.register();
	}


}
