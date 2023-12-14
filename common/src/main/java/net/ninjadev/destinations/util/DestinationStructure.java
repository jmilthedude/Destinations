package net.ninjadev.destinations.util;

import net.minecraft.block.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.ninjadev.destinations.init.ModConfigs;

public class DestinationStructure {

    public static boolean isValid(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof AbstractSignBlock) && !isValidBlock(block)) return false;

        BlockPos top = findTop(world, pos);
        if (top == null) return false;
        for (int i = 1; i <= 2; i++) {
            BlockPos nextPos = top.offset(Direction.DOWN, i);
            Block next = world.getBlockState(nextPos).getBlock();
            if (!isValidBase(next)) return false;
        }
        return true;
    }

    public static BlockPos findTop(World world, BlockPos pos) {
        BlockPos top = pos.mutableCopy();
        BlockState state = world.getBlockState(pos);
        Block current = state.getBlock();
        if (current instanceof AbstractSignBlock sign) {
            Direction direction = state.get(HorizontalFacingBlock.FACING);
            top = pos.offset(direction.getOpposite());
            current = world.getBlockState(top).getBlock();
        }
        if (isValidTop(current)) return top;
        if (isValidBase(current)) {
            for (int i = 1; i <= 2; i++) {
                BlockPos nextPos = pos.offset(Direction.UP, i);
                Block block = world.getBlockState(nextPos).getBlock();
                if (isValidTop(block)) return nextPos;
            }
        }
        return null;
    }

    public static boolean isValidBase(Block block) {
        return block == ModConfigs.GENERAL.getBaseBlock();
    }

    public static boolean isValidTop(Block block) {
        return block == ModConfigs.GENERAL.getTopBlock();
    }

    public static boolean isValidBlock(Block block) {
        return isValidBase(block) || isValidTop(block);
    }

    public static void destroyStructure(ServerPlayerEntity player, BlockPos pos) {
        World world = player.getWorld();
        BlockPos top = findTop(world, pos);
        if (top == null) return;
        player.getServerWorld().breakBlock(top, true, player);
        for (int i = 1; i <= 2; i++) {
            BlockPos nextPos = top.offset(Direction.DOWN, i);
            player.getServerWorld().breakBlock(nextPos, true, player);
        }
    }
}
