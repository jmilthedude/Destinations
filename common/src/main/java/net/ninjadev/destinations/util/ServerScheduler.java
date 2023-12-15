package net.ninjadev.destinations.util;

import net.minecraft.util.Pair;
import net.ninjadev.destinations.events.impl.ServerTickEvent;
import net.ninjadev.destinations.init.ModEvents;

import java.util.Iterator;
import java.util.LinkedList;

public class ServerScheduler {

    public static final ServerScheduler INSTANCE = new ServerScheduler();
    private static final Object lock = new Object();

    private boolean inTick = false;
    private final LinkedList<Pair<Runnable, Counter>> queue = new LinkedList<>();
    private final LinkedList<Pair<Runnable, Integer>> waiting = new LinkedList<>();

    private ServerScheduler() {
        ModEvents.SERVER_TICK_POST.register(this, this::onServerTick);
    }

    public void onServerTick(ServerTickEvent.Data data) {
        inTick = true;
        synchronized (lock) {
            inTick = true;
            Iterator<Pair<Runnable, Counter>> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Pair<Runnable, Counter> r = iterator.next();
                r.getRight().decrement();
                if (r.getRight().getValue() <= 0) {
                    r.getLeft().run();
                    iterator.remove();
                }
            }
            inTick = false;
            for (Pair<Runnable, Integer> wait : waiting) {
                queue.addLast(new Pair<>(wait.getLeft(), new Counter(wait.getRight())));
            }
        }
        waiting.clear();
    }

    public void schedule(int tickDelay, Runnable r) {
        synchronized (lock) {
            if (inTick) {
                waiting.addLast(new Pair<>(r, tickDelay));
            } else {
                queue.addLast(new Pair<>(r, new Counter(tickDelay)));
            }
        }
    }
}
