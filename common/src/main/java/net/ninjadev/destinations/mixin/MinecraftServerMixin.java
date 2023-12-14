package net.ninjadev.destinations.mixin;

import net.minecraft.server.MinecraftServer;
import net.ninjadev.destinations.Destinations;
import net.ninjadev.destinations.events.event.ServerTickEvent;
import net.ninjadev.destinations.init.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "startServer", at = @At(value = "RETURN"))
    private static <S extends MinecraftServer> void onServerStart(Function<Thread, S> serverFactory, CallbackInfoReturnable<S> cir) {
        Destinations.server = cir.getReturnValue();
        Destinations.getEventHandler().init();
    }

    @Inject(method = "shutdown", at = @At(value = "HEAD"))
    private void onServerShutdown(CallbackInfo ci) {
        Destinations.getEventHandler().release();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onServerTickPre(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ModEvents.SERVER_TICK_PRE.invoke(new ServerTickEvent.Data((MinecraftServer) (Object) this));
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onServerTickPost(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ModEvents.SERVER_TICK_POST.invoke(new ServerTickEvent.Data((MinecraftServer) (Object) this));
    }

}
