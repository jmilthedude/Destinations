package net.ninjadev.destinations.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.ninjadev.destinations.events.event.SignTextChangeEvent;
import net.ninjadev.destinations.init.ModEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin {

    @Inject(method = "getTextWithMessages", at = @At("RETURN"), cancellable = true)
    public void onChangeText(PlayerEntity player, List<FilteredMessage> messages, SignText text, CallbackInfoReturnable<SignText> cir) {
        SignBlockEntity instance = (SignBlockEntity) (Object) this;
        if (instance.getWorld() instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer) {
            SignTextChangeEvent.Data data = ModEvents.SIGN_TEXT_CHANGE.invoke(new SignTextChangeEvent.Data(instance, serverWorld, serverPlayer, new ArrayList<>(messages)));
            if (data.isCancelled()) {
                instance.setWaxed(true);
                for (int i = 0; i < data.getMessages().size(); ++i) {
                    FilteredMessage filteredMessage = data.getMessages().get(i);
                    Style style = text.getMessage(i, player.shouldFilterText()).getStyle();
                    text = player.shouldFilterText() ? text.withMessage(i, Text.literal(filteredMessage.getString()).setStyle(style)) : text.withMessage(i, Text.literal(filteredMessage.raw()).setStyle(style), Text.literal(filteredMessage.getString()).setStyle(style));
                }
                cir.setReturnValue(text.withGlowing(true));
            }
        }
    }
}
