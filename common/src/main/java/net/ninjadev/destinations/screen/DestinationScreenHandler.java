package net.ninjadev.destinations.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.ninjadev.destinations.data.Destination;
import net.ninjadev.destinations.data.PlayerDestinationsState;
import net.ninjadev.destinations.screen.inventory.DestinationInventory;
import net.ninjadev.destinations.screen.slot.DestinationSlot;
import net.ninjadev.destinations.screen.slot.NonInteractiveSlot;
import net.ninjadev.destinations.util.DestinationUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

public class DestinationScreenHandler extends ScreenHandler {
    private final DestinationInventory inventory;
    private static final Supplier<ItemStack> blank = () -> new ItemStack(Items.GRAY_STAINED_GLASS_PANE).setCustomName(Text.literal(""));

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
        Set<Destination> destinations = PlayerDestinationsState.get().getDestinationsForPlayerSorted(player);
        int index = 0;
        for (Destination destination : destinations) {
            ItemStack stack = destination.createStack(player);
            this.inventory.setPseudoItem(index++, stack);
        }
        for (int i = index; i < 9; i++) {
            this.inventory.setPseudoItem(i, blank.get());
        }
        for (int i = 9; i < 45; i++) {
            if (i != 22 && i != 39 && i != 41) this.inventory.setPseudoItem(i, blank.get());
        }
        this.inventory.setPseudoItem(39, new ItemStack(Items.ENDER_PEARL).setCustomName(Text.literal(String.format("%sTeleport to: %s%s", Formatting.WHITE, Formatting.RED, "None Selected"))));
        this.inventory.setPseudoItem(41, new ItemStack(Items.BARRIER).setCustomName(Text.literal(String.format("%sDelete: %s%s", Formatting.WHITE, Formatting.RED, "None Selected"))));
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
                return Text.literal("Destinations");
            }
        };
        player.openHandledScreen(screenFactory);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == -999) return;
        Slot slot = this.getSlot(slotIndex);
        if (slot instanceof DestinationSlot destinationSlot) {
            ItemStack stack = destinationSlot.getStack();

            if (slotIndex == 22) return;
            if (slotIndex < 9) {
                NbtCompound nbt = stack.getOrCreateNbt();
                if (!nbt.contains("destination")) return;
                Destination destination = new Destination(nbt.getCompound("destination"));
                boolean canTravel = destination.canTravel(player);
                this.inventory.setPseudoItem(22, stack);

                String teleportString = canTravel ? String.format("%sTeleport to: %s", Formatting.GREEN, stack.getName().getString()) : String.format("%sTOO FAR", Formatting.RED);
                this.inventory.setPseudoItem(39, new ItemStack(Items.ENDER_PEARL).setCustomName(Text.literal(teleportString)));

                this.inventory.setPseudoItem(41, new ItemStack(Items.BARRIER).setCustomName(Text.literal(String.format("%sDelete: %s", Formatting.RED, stack.getName().getString()))));

                this.updateToClient();
                return;
            }

            ItemStack destinationStack = this.inventory.getPseudoStack(22);
            NbtCompound nbt = destinationStack.getOrCreateNbt();
            if (!nbt.contains("destination")) return;
            Destination destination = new Destination(nbt.getCompound("destination"));
            if (slotIndex == 39) {
                DestinationUtil.attemptTeleportPlayer(player, destination);
            } else if (slotIndex == 41) {
                PlayerDestinationsState.get().remove(player, destination);
                DestinationScreenHandler.open(player);
            }
        }
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
