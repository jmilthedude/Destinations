package net.ninjadev.destinations.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.ninjadev.destinations.data.Destination;
import net.ninjadev.destinations.data.DestinationsState;
import net.ninjadev.destinations.init.ModConfigs;
import net.ninjadev.destinations.screen.inventory.DestinationInventory;
import net.ninjadev.destinations.screen.slot.DestinationSlot;
import net.ninjadev.destinations.screen.slot.NonInteractiveSlot;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DestinationScreenHandler extends ScreenHandler {
    private final DestinationInventory inventory;

    public DestinationScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X5, syncId);
        this.inventory = new DestinationInventory();

        this.load(playerInventory.player);
        int index = 0;
        for (int row = 0; row < 5; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new DestinationSlot(this.inventory, index++, 8 + column * 18, 18 + row * 18));
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
        Set<Destination> storedDestinations = DestinationsState.get().getStoredDestinations(player);
        int index = 0 + 0;
        for (Destination destination : storedDestinations) {
            int distance = destination.getDistance(player);
            int xpCost = ModConfigs.GENERAL.getXpCostMultiplier() * distance;
            ItemStack stack = destination.getStack(xpCost, player.experienceLevel);
            this.inventory.setPseudoItem(index++, stack);
        }
        for (int i = 9 + 0; i < 18; i++) {
            this.inventory.setPseudoItem(i, new ItemStack(Items.GRAY_STAINED_GLASS_PANE).setCustomName(Text.literal("")));
        }

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
        Slot slot = this.getSlot(slotIndex);
        System.out.println("SlotIndex: " + slot.getIndex());
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
