package net.ninjadev.destinations.mixin;

import net.minecraft.server.MinecraftServer;
import net.ninjadev.destinations.Destinations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "startServer", at = @At(value = "RETURN"))
    private static <S extends MinecraftServer> void onServerStart(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir) {
        Destinations.server = cir.getReturnValue();
    }

}
