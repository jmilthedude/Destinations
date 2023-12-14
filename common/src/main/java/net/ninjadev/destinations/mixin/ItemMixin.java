package net.ninjadev.destinations.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.ninjadev.destinations.events.event.ItemUseEvent;
import net.ninjadev.destinations.events.event.ItemUseOnBlockEvent;
import net.ninjadev.destinations.init.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    public void onUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer) {
            ItemUseEvent.Data data = ModEvents.ITEM_USE.invoke(new ItemUseEvent.Data(serverWorld, serverPlayer, hand, cir.getReturnValue()));
            if (data.isCancelled()) {
                cir.setReturnValue(data.getResult());
                cir.cancel();
            }
        }
    }

    @Inject(method = "useOnBlock", at = @At(value = "HEAD"), cancellable = true)
    public void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (!(context.getWorld() instanceof ServerWorld world)) return;
        ItemUseOnBlockEvent.Data data = ModEvents.ITEM_USE_ON_BLOCK.invoke(new ItemUseOnBlockEvent.Data(context, cir.getReturnValue()));
        if(data.isCancelled()) {
            cir.setReturnValue(data.getResult());
            cir.cancel();
        }
    }


}
