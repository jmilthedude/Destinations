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
import net.ninjadev.destinations.Destinations;
import net.ninjadev.destinations.data.Destination;
import net.ninjadev.destinations.data.GlobalDestinationsState;
import net.ninjadev.destinations.data.PlayerDestinationsState;
import net.ninjadev.destinations.events.impl.*;
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

        Optional<BlockPos> originOptional = DestinationStructure.findOrigin(world, blockPos);
        if (originOptional.isEmpty()) return;

        BlockPos origin = originOptional.get();
        Optional<Destination> destination = GlobalDestinationsState.get().getDestinationByLocation(world, origin);
        if (destination.isEmpty()) return;

        if (PlayerDestinationsState.get().add(data.getPlayer(), destination.get())) {
            String name = destination.get().getName();
            world.playSound(null, origin, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.25f, 1.25f);
            data.getPlayer().sendMessage(Text.literal(String.format("You have added %s%s%s to your list of destinations!", Formatting.DARK_AQUA, name, Formatting.RESET)), true);
            data.setResult(ActionResult.SUCCESS);
        } else {
            world.playSound(null, origin, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.PLAYERS, 0.75f, 1f);
        }

    }

    private static void handleSignTextChange(SignTextChangeEvent.Data data) {
        SignBlockEntity signBlock = data.getSignBlock();
        ServerPlayerEntity player = data.getPlayer();
        if (!(signBlock.getWorld() instanceof ServerWorld world)) return;

        BlockPos signPos = signBlock.getPos();
        BlockState state = world.getBlockState(signBlock.getPos());
        if (!state.contains(HorizontalFacingBlock.FACING)) return; // not placed on a block face

        Direction direction = state.get(HorizontalFacingBlock.FACING);
        BlockPos origin = signPos.offset(direction.getOpposite());

        if (DestinationStructure.isValid(world, origin)) {
            // if placed on structure, but not the top block
            if (!DestinationStructure.isValidTop(world.getBlockState(origin).getBlock())) {
                player.sendMessage(Text.literal("The sign must be placed on any side of the top block of the structure.").formatted(Formatting.RED), true);
                world.breakBlock(signPos, true, player);
                return;
            }

            Optional<Destination> destinationOptional = GlobalDestinationsState.get().getDestinationByLocation(world, origin);
            if (destinationOptional.isPresent()) {
                player.sendMessage(Text.literal("This destination already exists!").formatted(Formatting.RED), true);
                world.breakBlock(signPos, true, player);
            } else {
                List<FilteredMessage> newText = new ArrayList<>();
                FilteredMessage first = data.getMessages().get(0);
                FilteredMessage second = data.getMessages().get(1);

                newText.add(first);
                newText.add(FilteredMessage.EMPTY);
                newText.add(FilteredMessage.permitted("Owner:"));
                newText.add(FilteredMessage.permitted(player.getDisplayName().getString()));

                data.setMessages(newText);

                String name = first.raw();
                String iconId = second.raw();
                Destination destination = new Destination(player.getUuid(), name, origin, world.getRegistryKey(), iconId.isEmpty() ? null : iconId);
                if (GlobalDestinationsState.get().add(player, destination)) {
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

        Optional<BlockPos> origin = DestinationStructure.findOrigin(world, pos);
        if(origin.isEmpty()) return;
        Optional<Destination> destinationOptional = GlobalDestinationsState.get().getDestinationByLocation(world, origin.get());
        if (destinationOptional.isEmpty()) return;

        Destination destination = destinationOptional.get();
        if (!destination.isOwner(player)) {
            data.setCancelled();
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SignBlockEntity signBlock) {
                ServerScheduler.INSTANCE.schedule(5, () -> player.networkHandler.sendPacket(signBlock.toUpdatePacket()));
            }
            player.sendMessage(Text.literal("You are not the owner of this destination!").formatted(Formatting.RED), true);
        } else {
            data.setCancelled();
            DestinationStructure.destroyStructure(player, pos);
            if (GlobalDestinationsState.get().remove(player, destination)) {
                PlayerDestinationsState.get().removeFromAllPlayers(destination);
                player.sendMessage(Text.literal(String.format("The destination '%s%s%s' has been removed!", Formatting.DARK_AQUA, destination.getName(), Formatting.RESET)), true);
            }
        }
    }
}
