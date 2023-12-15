package net.ninjadev.destinations.events.impl;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.ninjadev.destinations.events.Event;

public class ItemUseOnBlockEvent extends Event<ItemUseOnBlockEvent, ItemUseOnBlockEvent.Data> {

    public ItemUseOnBlockEvent() {
    }

    public ItemUseOnBlockEvent(ItemUseOnBlockEvent parent) {
        super(parent);
    }

    @Override
    public ItemUseOnBlockEvent createChild() {
        return new ItemUseOnBlockEvent(this);
    }

    public static class Data {

        private final ItemUsageContext context;
        private ActionResult result;
        private boolean isCancelled;

        public Data(ItemUsageContext context, ActionResult result) {
            this.context = context;
            this.result = result;
        }

        public ItemUsageContext getContext() {
            return context;
        }

        public void setResult(ActionResult result) {
            this.result = result;
        }

        public ActionResult getResult() {
            return result;
        }

        public void setCancelled() {
            this.isCancelled = true;
        }

        public boolean isCancelled() {
            return isCancelled;
        }
    }
}
