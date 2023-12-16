package net.ninjadev.destinations.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.ninjadev.destinations.Destinations;

@Mod(Destinations.MOD_ID)
public class DestinationsForge {
    public DestinationsForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            throw new RuntimeException("The Destinations mod is for servers only. Please remove it from your client's /mods folder.");
        }

        Destinations.init();

    }
}