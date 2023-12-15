package net.ninjadev.destinations.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.ninjadev.destinations.screen.inventory.DestinationInventory;

public class DestinationSlot extends Slot {

    private final int index;
    public DestinationSlot(DestinationInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.index = index;
    }

    @Override
    public void setStack(ItemStack stack) {
        ((DestinationInventory) this.inventory).setPseudoItem(this.index, stack);

    }

    @Override
    public ItemStack getStack() {
        return ((DestinationInventory) this.inventory).getPseudoStack(this.index);

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
