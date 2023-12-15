package net.ninjadev.destinations.events.event;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

import java.util.List;

public class ItemStackTooltipEvent extends Event<ItemStackTooltipEvent, ItemStackTooltipEvent.Data> {

    public ItemStackTooltipEvent() {
    }

    public ItemStackTooltipEvent(ItemStackTooltipEvent parent) {
        super(parent);
    }

    @Override
    public ItemStackTooltipEvent createChild() {
        return new ItemStackTooltipEvent(this);
    }

    public static class Data {

        public final PlayerEntity player;
        public final ItemStack stack;
        public final TooltipContext tooltipContext;
        public final List<Text> result;

        public Data(PlayerEntity player, ItemStack stack, TooltipContext tooltipContext, List<Text> result) {
            this.player = player;
            this.stack = stack;
            this.tooltipContext = tooltipContext;
            this.result = result;
        }

    }
}
