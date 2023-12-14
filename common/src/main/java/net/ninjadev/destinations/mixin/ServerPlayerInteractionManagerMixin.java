package net.ninjadev.destinations.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ninjadev.destinations.data.Destination;
import net.ninjadev.destinations.data.DestinationsState;
import net.ninjadev.destinations.init.ModConfigs;
import net.ninjadev.destinations.util.DestinationStructure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow protected ServerWorld world;

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    public void preventBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!DestinationStructure.isValid(this.world, pos)) return;
        DestinationsState destinationsState = DestinationsState.get();
        if (!destinationsState.exists(world, pos)) return;
        Optional<Destination> destination = destinationsState.getDestination(player, world, pos);
        if (destination.isEmpty()) {
            cir.setReturnValue(false);
            cir.cancel();
        } else {
            cir.setReturnValue(false);
            cir.cancel();
            DestinationStructure.destroyStructure(player, pos);
            destinationsState.remove(player, destination.get());
        }
    }

    @Inject(method = "interactBlock", at = @At("RETURN"))
    public void createDestination(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        Item item = stack.getItem();
        if (ModConfigs.GENERAL.getTopBlock().asItem() != item) return;
        BlockPos blockPos = hitResult.getBlockPos().up();
        if (!DestinationStructure.isValid(world, blockPos)) return;
        DestinationsState.get().add(player, new Destination(player.getUuid(), "Test", blockPos.getX(), blockPos.getY(), blockPos.getZ(), world.getRegistryKey()));
    }

}
