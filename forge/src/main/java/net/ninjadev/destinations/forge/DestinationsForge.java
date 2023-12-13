package net.ninjadev.destinations.forge;

import net.ninjadev.destinations.Destinations;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Destinations.MOD_ID)
public class DestinationsForge {
    public DestinationsForge() {
        Destinations.init();
    }
}