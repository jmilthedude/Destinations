package net.ninjadev.destinations.init;

import net.ninjadev.destinations.events.Event;
import net.ninjadev.destinations.events.impl.*;

import java.util.ArrayList;
import java.util.List;

public class ModEvents {
    public static final List<Event<?, ?>> REGISTRY = new ArrayList<>();

    public static final BlockBreakEvent BLOCK_BREAK = register(new BlockBreakEvent());
    public static final BlockInteractEvent BLOCK_INTERACT = register(new BlockInteractEvent());
    public static final ItemUseEvent ITEM_USE = register(new ItemUseEvent());
    public static final ItemUseOnBlockEvent ITEM_USE_ON_BLOCK = register(new ItemUseOnBlockEvent());
    public static final SignTextChangeEvent SIGN_TEXT_CHANGE = register(new SignTextChangeEvent());
    public static final ServerTickEvent.Pre SERVER_TICK_PRE = register(new ServerTickEvent.Pre());
    public static final ServerTickEvent.Post SERVER_TICK_POST = register(new ServerTickEvent.Post());

    private static <T extends Event<?,?>> T register(T event) {
        REGISTRY.add(event);
        return event;
    }
    public static void release(Object reference) {
        REGISTRY.forEach(event -> event.release(reference));
    }
}
