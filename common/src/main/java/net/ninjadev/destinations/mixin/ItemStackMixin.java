package net.ninjadev.destinations.mixin;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.ninjadev.destinations.events.event.ItemStackTooltipEvent;
import net.ninjadev.destinations.init.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void getTooltip(PlayerEntity player, TooltipContext tooltipContext, CallbackInfoReturnable<List<Text>> info) {
        ModEvents.ITEM_STACK_TOOLTIP.invoke(new ItemStackTooltipEvent.Data(player, (ItemStack) (Object) this, tooltipContext, info.getReturnValue()));
    }
}
