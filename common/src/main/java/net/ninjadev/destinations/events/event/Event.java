package net.ninjadev.destinations.events.event;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class Event<E extends Event<E, T>, T> {

    protected E parent;
    protected boolean child;
    protected boolean cancellable;
    protected boolean isCancelled;

    protected Map<Integer, Map<Object, List<Consumer<T>>>> listeners;
    protected List<Predicate<T>> conditions;

    protected Event() {
        this.listeners = Collections.synchronizedMap(new TreeMap<>());
        this.listeners.put(0, Collections.synchronizedMap(new LinkedHashMap<>()));
        this.child = false;
    }

    protected Event(E parent) {
        this.parent = parent;
        this.conditions = new ArrayList<>();
        this.child = true;
    }

    public TreeMap<Integer, Map<Object, List<Consumer<T>>>> getListeners() {
        TreeMap<Integer, Map<Object, List<Consumer<T>>>> map = new TreeMap<>(Collections.reverseOrder());
        map.putAll(this.listeners);
        return map;
    }

    public boolean isChild() {
        return this.child;
    }

    public E getParent() {
        return this.parent;
    }

    public abstract E createChild();

    public T invoke(T data) {
        if (this.isChild()) {
            this.parent.invoke(data);
        } else {
            for (Integer priority : this.getListeners().keySet()) {
                new ArrayList<>(this.getListeners().get(priority).values()).forEach(list -> list.forEach(consumer -> {
                    try {
                        consumer.accept(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            }

        }

        return data;
    }

    public E register(Object reference, Consumer<T> listener) {
        return this.register(reference, listener, 0);
    }

    public E register(Object reference, Consumer<T> listener, int priority) {
        if (this.isChild()) {
            this.parent.register(reference, t -> {
                for (Predicate<T> condition : this.conditions) {
                    if (!condition.test(t)) return;
                }

                listener.accept(t);
            }, priority);

            return (E) this;
        }

        this.listeners.computeIfAbsent(priority, p -> Collections.synchronizedMap(new LinkedHashMap<>()));
        this.listeners.get(priority).computeIfAbsent(reference, r -> new ArrayList<>()).add(listener);
        return (E) this;
    }

    public E release(Object reference) {
        if (this.isChild()) {
            this.parent.release(reference);
            return (E) this;
        }

        this.listeners.values().forEach(map -> map.remove(reference));
        return (E) this;
    }

    public E filter(Predicate<T> condition) {
        if (this.isChild()) {
            this.conditions.add(condition);
            return (E) this;
        }

        return this.createChild().filter(condition);
    }

}
