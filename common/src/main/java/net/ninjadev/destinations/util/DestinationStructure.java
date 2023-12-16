package net.ninjadev.destinations.util;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.ninjadev.destinations.init.ModConfigs;

import java.util.Optional;

public class DestinationStructure {

    public static boolean isValid(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof AbstractSignBlock) && !isValidBlock(block)) return false;

        BlockPos top = findOrigin(world, pos);
        if (top == null) return false;
        for (int i = 1; i <= 2; i++) {
            BlockPos nextPos = top.offset(Direction.DOWN, i);
            Block next = world.getBlockState(nextPos).getBlock();
            if (!isValidBase(next)) return false;
        }
        return true;
    }

    public static Optional<BlockPos> findOrigin(World world, BlockPos pos) {
        BlockPos top = pos.mutableCopy();
        BlockState state = world.getBlockState(pos);
        Block current = state.getBlock();
        if (current instanceof AbstractSignBlock sign) {
            Direction direction = state.get(HorizontalFacingBlock.FACING);
            top = pos.offset(direction.getOpposite());
            current = world.getBlockState(top).getBlock();
        }
        if (isValidTop(current)) return Optional.of(top);
        if (isValidBase(current)) {
            for (int i = 1; i <= 2; i++) {
                BlockPos nextPos = pos.offset(Direction.UP, i);
                Block block = world.getBlockState(nextPos).getBlock();
                if (isValidTop(block)) return Optional.of(nextPos);
            }
        }
        return Optional.empty();
    }

    public static boolean isValidBase(Block block) {
        return ModConfigs.GENERAL.getBaseBlocks().contains(block);
    }

    public static boolean isValidTop(Block block) {
        return ModConfigs.GENERAL.getTopBlocks().contains(block);
    }

    public static boolean isValidBlock(Block block) {
        return isValidBase(block) || isValidTop(block);
    }

    public static void destroyStructure(ServerPlayerEntity player, BlockPos pos) {
        World world = player.getWorld();
        Optional<BlockPos> originOptional = DestinationStructure.findOrigin(world, pos);
        if (originOptional.isEmpty()) return;
        BlockPos origin = originOptional.get();
        player.getServerWorld().breakBlock(origin, true, player);
        for (int i = 1; i <= 2; i++) {
            BlockPos nextPos = origin.offset(Direction.DOWN, i);
            player.getServerWorld().breakBlock(nextPos, true, player);
        }
    }
}
