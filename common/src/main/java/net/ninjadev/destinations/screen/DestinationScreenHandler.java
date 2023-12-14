package net.ninjadev.destinations.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.ninjadev.destinations.screen.inventory.DestinationInventory;
import net.ninjadev.destinations.screen.slot.DestinationSlot;
import net.ninjadev.destinations.screen.slot.NonInteractiveSlot;
import org.jetbrains.annotations.Nullable;

public class DestinationScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public DestinationScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X5, syncId);
        this.inventory = new DestinationInventory();

        for (int row = 0; row < 5; ++row) {
            for (int slot = 0; slot < 9; ++slot) {
                this.addSlot(new DestinationSlot("", this.inventory, row + slot * 9, 8 + slot * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int slot = 0; slot < 9; ++slot) {
                this.addSlot(new NonInteractiveSlot(playerInventory, slot + row * 9 + 9, 8 + slot * 18, 103 + row * 18));
            }
        }

        // Add hotbar
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            this.addSlot(new NonInteractiveSlot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 161));
        }
    }

    private void load(PlayerEntity player) {

    }

    public static void open(PlayerEntity player) {
        NamedScreenHandlerFactory screenFactory = new NamedScreenHandlerFactory() {
            @Nullable
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new DestinationScreenHandler(syncId, playerInventory);
            }

            @Override
            public Text getDisplayName() {
                return Text.translatable("screen.destinations.destination_screen.title");
            }
        };
        player.openHandledScreen(screenFactory);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        System.out.println("Slot: " + slotIndex);
        System.out.println("SlotActionType: " + actionType);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
