package net.ninjadev.destinations.events.impl;

import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.ninjadev.destinations.events.Event;

public class BlockBreakEvent extends Event<BlockBreakEvent, BlockBreakEvent.Data> {

    public BlockBreakEvent() {
    }

    public BlockBreakEvent(BlockBreakEvent parent) {
        super(parent);
    }

    @Override
    public BlockBreakEvent createChild() {
        return new BlockBreakEvent(this);
    }

    public static class Data {

        private final ServerWorld world;
        private final BlockPos pos;
        private final BlockState state;
        private final ServerPlayerEntity player;
        private boolean isCancelled;

        public Data(ServerWorld world, BlockPos pos, BlockState state, ServerPlayerEntity player) {
            this.world = world;
            this.pos = pos;
            this.state = state;
            this.player = player;
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

        public BlockPos getPos() {
            return pos;
        }

        public BlockState getState() {
            return state;
        }

        public ServerPlayerEntity getPlayer() {
            return player;
        }
    }
}
