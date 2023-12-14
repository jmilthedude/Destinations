package net.ninjadev.destinations.events.event;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class SignTextChangeEvent extends Event<SignTextChangeEvent, SignTextChangeEvent.Data> {
    public SignTextChangeEvent() {
    }

    public SignTextChangeEvent(SignTextChangeEvent parent) {
        super(parent);
    }

    @Override
    public SignTextChangeEvent createChild() {
        return new SignTextChangeEvent(this);
    }

    public static class Data {
        private final SignBlockEntity signBlock;
        private final ServerWorld world;
        private final ServerPlayerEntity player;
        private final List<FilteredMessage> messages;
        private boolean isCancelled;

        public Data(SignBlockEntity signBlock, ServerWorld world, ServerPlayerEntity player, List<FilteredMessage> messages) {
            this.signBlock = signBlock;
            this.world = world;
            this.player = player;
            this.messages = messages;
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

        public SignBlockEntity getSignBlock() {
            return signBlock;
        }

        public List<FilteredMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<FilteredMessage> messages) {
            this.messages.clear();
            this.messages.addAll(messages);
            this.setCancelled();
        }
    }
}
