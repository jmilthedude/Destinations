package net.ninjadev.destinations.screen;

import net.minecraft.block.BedBlock;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.ninjadev.destinations.Destinations;
import net.ninjadev.destinations.data.Destination;
import net.ninjadev.destinations.data.DestinationsState;
import net.ninjadev.destinations.init.ModConfigs;
import net.ninjadev.destinations.screen.inventory.DestinationInventory;
import net.ninjadev.destinations.screen.slot.DestinationSlot;
import net.ninjadev.destinations.screen.slot.NonInteractiveSlot;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
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
        Set<Destination> storedDestinations = DestinationsState.get().getStoredDestinations(player);
        int index = 0;
        for (Destination destination : storedDestinations) {
            int distance = destination.getDistance(player);
            int xpCost = ModConfigs.GENERAL.getCost(distance);
            ItemStack stack = destination.createStack(xpCost, player.experienceLevel);
            this.inventory.setPseudoItem(index++, stack);
        }
        for (int i = index; i < 9; i++) {
            this.inventory.setPseudoItem(i, blank.get());
        }
        for (int i = 9; i < 45; i++) {
            if (i != 22 && i != 39 && i != 41) this.inventory.setPseudoItem(i, blank.get());
        }
        this.inventory.setPseudoItem(39, new ItemStack(Items.ENDER_PEARL));
        this.inventory.setPseudoItem(41, new ItemStack(Items.BARRIER));
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
                this.inventory.setPseudoItem(22, stack);
                this.updateToClient();
                return;
            }

            ItemStack destinationStack = this.inventory.getPseudoStack(22);
            NbtCompound nbt = destinationStack.getOrCreateNbt();
            if (!nbt.contains("destination")) return;
            Destination destination = new Destination(nbt.getCompound("destination"));
            ServerWorld world = Destinations.server.getWorld(destination.getWorld());
            if (world == null) return;

            if (slotIndex == 39) {
                int cost = ModConfigs.GENERAL.getCost(destination.getDistance(player));
                if (cost > player.experienceLevel) return;
                BlockPos pos = destination.getBlockPos().offset(player.getHorizontalFacing().getOpposite());
                Vec3d vec3d = BedBlock.findWakeUpPosition(player.getType(), world, pos.offset(Direction.DOWN, 2), player.getHorizontalFacing(), player.getYaw()).orElseGet(() -> {
                    BlockPos nextPos = pos.up();
                    return new Vec3d((double) nextPos.getX() + 0.5, (double) nextPos.getY() + 0.1, (double) nextPos.getZ() + 0.5);
                });
                player.teleport(world, vec3d.x + .5, vec3d.y, vec3d.z + .5, new HashSet<>(), player.getYaw(), player.getPitch());
                player.addExperienceLevels(-cost);
                player.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 0.75f, 1.0f);
            } else if (slotIndex == 41) {
                DestinationsState.get().removeStored(player, destination);
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
