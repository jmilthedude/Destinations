package net.ninjadev.destinations.events.event;

import net.minecraft.server.MinecraftServer;

public abstract class ServerTickEvent extends Event<ServerTickEvent, ServerTickEvent.Data> {

    public ServerTickEvent() {
    }

    public ServerTickEvent(ServerTickEvent parent) {
        super(parent);
    }

    public static class Pre extends ServerTickEvent {

        public Pre() {
        }

        public Pre(Pre pre) {
            super(pre);
        }

        @Override
        public Pre createChild() {
            return new Pre(this);
        }
    }

    public static class Post extends ServerTickEvent {

        public Post() {
        }

        public Post(ServerTickEvent parent) {
            super(parent);
        }

        @Override
        public Post createChild() {
            return new Post(this);
        }
    }

    public static class Data {

        private final MinecraftServer server;

        public Data(MinecraftServer server) {
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }
}
