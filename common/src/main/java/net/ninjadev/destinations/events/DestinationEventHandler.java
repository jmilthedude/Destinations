package net.ninjadev.destinations.events;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.ninjadev.destinations.Destinations;
import net.ninjadev.destinations.data.Destination;
import net.ninjadev.destinations.data.DestinationsState;
import net.ninjadev.destinations.events.event.BlockBreakEvent;
import net.ninjadev.destinations.events.event.BlockInteractEvent;
import net.ninjadev.destinations.events.event.ItemUseEvent;
import net.ninjadev.destinations.events.event.SignTextChangeEvent;
import net.ninjadev.destinations.init.ModConfigs;
import net.ninjadev.destinations.init.ModEvents;
import net.ninjadev.destinations.screen.DestinationScreenHandler;
import net.ninjadev.destinations.util.DestinationStructure;
import net.ninjadev.destinations.util.ServerScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DestinationEventHandler {


    public void init() {
        this.registerEvents();
    }

    public void release() {
        Destinations.LOGGER.info("Releasing Events");
        ModEvents.release(this);
    }

    private void registerEvents() {
        Destinations.LOGGER.info("Registering Events");
        ModEvents.BLOCK_BREAK.register(this, DestinationEventHandler::handleBlockBreak);
        ModEvents.BLOCK_INTERACT.register(this, DestinationEventHandler::handleBlockInteraction);
        ModEvents.ITEM_USE.register(this, DestinationEventHandler::handleItemUse);
        ModEvents.SIGN_TEXT_CHANGE.register(this, DestinationEventHandler::handleSignTextChange);
    }

    private static void handleBlockInteraction(BlockInteractEvent.Data data) {
        BlockHitResult hitResult = data.getHitResult();
        ServerWorld world = data.getWorld();
        BlockPos blockPos = hitResult.getBlockPos();
        if (!DestinationStructure.isValid(world, blockPos)) return;
        Optional<Destination> destination = DestinationsState.get().getDestination(data.getWorld(), blockPos);
        if (destination.isEmpty()) return;
        if (DestinationsState.get().addStored(data.getPlayer(), destination.get())) {
            String name = destination.get().getName();
            world.playSound(null, blockPos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.75f, 1.25f);
            data.getPlayer().sendMessage(Text.literal(String.format("You have added %s%s%s to your list of destinations!", Formatting.DARK_AQUA, name, Formatting.RESET)), true);
            data.setResult(ActionResult.SUCCESS);
        }

    }

    private static void handleSignTextChange(SignTextChangeEvent.Data data) {
        SignBlockEntity signBlock = data.getSignBlock();
        World world = signBlock.getWorld();
        ServerPlayerEntity player = data.getPlayer();
        if (world == null) return;

        BlockPos signPos = signBlock.getPos();
        BlockState state = world.getBlockState(signBlock.getPos());
        Direction direction = state.get(HorizontalFacingBlock.FACING);
        BlockPos topBlock = signPos.offset(direction.getOpposite());

        if (DestinationStructure.isValid(world, topBlock)) {
            boolean exists = DestinationsState.get().exists(world, topBlock);
            if (exists) {
                player.sendMessage(Text.literal("This destination already exists!").formatted(Formatting.RED), true);
                world.breakBlock(signPos, true, player);
            } else {
                List<FilteredMessage> newText = new ArrayList<>();
                FilteredMessage first = data.getMessages().get(0);
                newText.add(first);
                newText.add(FilteredMessage.EMPTY);
                newText.add(FilteredMessage.permitted("Owner:"));
                newText.add(FilteredMessage.permitted(player.getDisplayName().getString()));
                data.setMessages(newText);
                String name = first.raw();
                if (DestinationsState.get().add(player, new Destination(player.getUuid(), name, topBlock, world.getRegistryKey()))) {
                    player.sendMessage(Text.literal(String.format("You have created a new Destination: %s%s%s", Formatting.DARK_AQUA, name, Formatting.RESET)), true);
                }
            }
        }
    }

    private static void handleItemUse(ItemUseEvent.Data data) {
        ServerPlayerEntity player = data.getPlayer();
        Hand hand = data.getHand();
        ItemStack stack = player.getMainHandStack();
        if (hand == Hand.MAIN_HAND && stack.getItem() == ModConfigs.GENERAL.getItem()) {
            DestinationScreenHandler.open(player);
            data.setResult(TypedActionResult.success(stack));
            data.setCancelled();
        }
    }

    private static void handleBlockBreak(BlockBreakEvent.Data data) {
        ServerWorld world = data.getWorld();
        BlockPos pos = data.getPos();
        ServerPlayerEntity player = data.getPlayer();
        if (!DestinationStructure.isValid(world, pos)) return;
        DestinationsState destinationsState = DestinationsState.get();
        if (!destinationsState.exists(world, pos)) return;
        Optional<Destination> destination = destinationsState.getDestination(player, world, pos);
        if (destination.isEmpty()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SignBlockEntity signBlock) {
                ServerScheduler.INSTANCE.schedule(5, () -> player.networkHandler.sendPacket(signBlock.toUpdatePacket()));
            }
            player.sendMessage(Text.literal("You are not the owner of this destination!").formatted(Formatting.RED), true);
            data.setCancelled();
        } else {
            data.setCancelled();
            DestinationStructure.destroyStructure(player, pos);
            if (destinationsState.remove(player, destination.get())) {
                player.sendMessage(Text.literal(String.format("The destination '%s%s%s' has been removed!", Formatting.DARK_AQUA, destination.get().getName(), Formatting.RESET)), true);
            }
        }
    }
}