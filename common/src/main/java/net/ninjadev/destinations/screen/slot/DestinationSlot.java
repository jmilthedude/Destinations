package net.ninjadev.destinations.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.ninjadev.destinations.screen.inventory.DestinationInventory;

public class DestinationSlot extends Slot {
    public DestinationSlot(String name, Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public void setStack(ItemStack stack) {
        if (this.inventory instanceof DestinationInventory destinationInventory) {
            destinationInventory.setPseudoItem(this.getIndex(), stack);
        }
    }

    @Override
    public ItemStack getStack() {
        if (this.inventory instanceof DestinationInventory destinationInventory) {
            destinationInventory.getPseudoStack(this.getIndex());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }
}
