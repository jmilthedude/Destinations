package net.ninjadev.destinations.screen.inventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DestinationInventory extends SimpleInventory {


    public DestinationInventory() {
        super(45);
    }

    public void setPseudoItem(int slot, ItemStack stack) {
        super.setStack(slot, stack);
    }

    public ItemStack getPseudoStack(int slot) {
        return super.getStack(slot);
    }

    @Override
    public ItemStack getStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> clearToList() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(Item item, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack addStack(ItemStack stack) {
        return stack;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {

    }
}
