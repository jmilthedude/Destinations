package net.ninjadev.destinations.events.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.ninjadev.destinations.events.Event;

public class BlockInteractEvent extends Event<BlockInteractEvent, BlockInteractEvent.Data> {

    public BlockInteractEvent() {
    }

    public BlockInteractEvent(BlockInteractEvent parent) {
        super(parent);
    }

    @Override
    public BlockInteractEvent createChild() {
        return new BlockInteractEvent(this);
    }

    public static class Data {
        private final ServerPlayerEntity player;
        private final ServerWorld world;
        private final ItemStack stack;
        private final Hand hand;
        private final BlockHitResult hitResult;
        private ActionResult result;
        private boolean cancelled;

        public Data(ServerPlayerEntity player, ServerWorld world, ItemStack stack, Hand hand, BlockHitResult hitResult, ActionResult result) {
            this.player = player;
            this.world = world;
            this.stack = stack;
            this.hand = hand;
            this.hitResult = hitResult;
        }

        public ServerPlayerEntity getPlayer() {
            return player;
        }

        public ServerWorld getWorld() {
            return world;
        }

        public ItemStack getStack() {
            return stack;
        }

        public Hand getHand() {
            return hand;
        }

        public BlockHitResult getHitResult() {
            return hitResult;
        }

        public ActionResult getResult() {
            return result;
        }

        public void setResult(ActionResult result) {
            this.result = result;
            if(result == ActionResult.SUCCESS) {
                this.cancelled = true;
            }
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }
}
