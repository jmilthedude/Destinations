package net.ninjadev.destinations.events.event;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;

public class ItemUseEvent extends Event<ItemUseEvent, ItemUseEvent.Data> {

    public ItemUseEvent() {
    }

    public ItemUseEvent(ItemUseEvent parent) {
        super(parent);
    }

    @Override
    public ItemUseEvent createChild() {
        return new ItemUseEvent(this);
    }

    public static class Data {

        private final ServerWorld world;
        private final ServerPlayerEntity player;
        private final Hand hand;
        private TypedActionResult<ItemStack> result;
        private boolean isCancelled;

        public Data(ServerWorld world, ServerPlayerEntity player, Hand hand, TypedActionResult<ItemStack> result) {
            this.world = world;
            this.player = player;
            this.hand = hand;
            this.result = result;
        }

        public void setCancelled() {
            this.isCancelled = true;
        }

        public boolean isCancelled() {
            return isCancelled;
        }

        public ServerWorld getWorld() {
            return world;
        }

        public ServerPlayerEntity getPlayer() {
            return player;
        }

        public Hand getHand() {
            return hand;
        }

        public void setResult(TypedActionResult<ItemStack> result) {
            this.result = result;
        }

        public TypedActionResult<ItemStack> getResult() {
            return result;
        }
    }
}
