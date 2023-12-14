package net.ninjadev.destinations.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.ninjadev.destinations.init.ModConfigs;
import net.ninjadev.destinations.screen.DestinationScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Shadow
    public abstract TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand);

    @Inject(method = "use", at = @At(value = "HEAD"), cancellable = true)
    public void onUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = player.getMainHandStack();
        if (hand == Hand.MAIN_HAND && stack.getItem() == ModConfigs.GENERAL.getItem()) {
            DestinationScreenHandler.open(player);
            cir.setReturnValue(TypedActionResult.success(stack));
            cir.cancel();
        }
    }

    @Inject(method = "useOnBlock", at = @At(value = "HEAD"), cancellable = true)
    public void onUseOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        Hand hand = context.getHand();
        PlayerEntity player = context.getPlayer();
        if (player == null) return;
        ItemStack stack = player.getMainHandStack();

        World world = context.getWorld();
        BlockState state = world.getBlockState(context.getBlockPos());
        Block block = state.getBlock();
        if(block != ModConfigs.GENERAL.getBaseBlock() || block != ModConfigs.GENERAL.getTopBlock()) return;



        if (hand == Hand.MAIN_HAND && stack.getItem() == ModConfigs.GENERAL.getItem()) {

            cir.cancel();
        }
    }


}
