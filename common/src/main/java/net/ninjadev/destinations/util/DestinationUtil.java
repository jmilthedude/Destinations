package net.ninjadev.destinations.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.ninjadev.destinations.Destinations;
import net.ninjadev.destinations.data.Destination;
import net.ninjadev.destinations.init.ModConfigs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class DestinationUtil {

    private static ImmutableList<Vec3i> getValidSpawnOffsets() {
        List<Vec3i> validSpawns = new ArrayList<>();
        for (int x = -5; x <= 5; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    if (x >= -1 && x <= 1 && z >= -1 && z <= 1) continue;
                    validSpawns.add(new Vec3i(x, y, z));
                }
            }
        }
        return ImmutableList.copyOf(validSpawns);
    }

    public static boolean attemptTeleportPlayer(PlayerEntity player, Destination destination) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;

        int cost = player.isCreative() ? 0 : ModConfigs.GENERAL.getCost(destination.getDistance(player));
        if (!player.isCreative() && cost > serverPlayer.experienceLevel) return false;
        ServerWorld world = Destinations.server.getWorld(destination.getWorld());
        if (world == null) return false;

        BlockPos pos = destination.getBlockPos();
        float playerYaw = serverPlayer.getYaw();
        float playerPitch = serverPlayer.getPitch();
        Vec3d teleportPosition = findValidSpawnPos(serverPlayer, world, pos.offset(Direction.DOWN, 2));
        if (teleportPosition == null) {
            serverPlayer.networkHandler.sendPacket(new CloseScreenS2CPacket(serverPlayer.currentScreenHandler.syncId));
            player.sendMessage(Text.literal(String.format("%sThere is no safe place to teleport to for the Destination: %s%s%s", Formatting.RED, Formatting.DARK_AQUA, destination.getName(), Formatting.RED)));
            return false;
        }

        if (serverPlayer.teleport(world, teleportPosition.x, teleportPosition.y, teleportPosition.z, new HashSet<>(), playerYaw, playerPitch)) {
            serverPlayer.addExperienceLevels(-cost);
            world.playSound(null, teleportPosition.x, teleportPosition.y, teleportPosition.z, SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.PLAYERS, 0.1f, 1.0f);
            serverPlayer.networkHandler.sendPacket(new CloseScreenS2CPacket(serverPlayer.currentScreenHandler.syncId));
            return true;
        }

        return false;
    }

    private static Vec3d findValidSpawnPos(PlayerEntity player, ServerWorld world, BlockPos pos) {
        EntityType<?> entityType = player.getType();
        List<Vec3d> validSpawns = new ArrayList<>();
        for (Vec3i validSpawnOffset : getValidSpawnOffsets()) {
            BlockPos next = pos.add(validSpawnOffset);
            if (entityType.isInvalidSpawn(world.getBlockState(next))) continue;
            Vec3d respawnPos = Dismounting.findRespawnPos(entityType, world, next, false);
            if (respawnPos != null) validSpawns.add(respawnPos.add(0, 0.2, 0));
        }
        if (validSpawns.isEmpty()) return null;
        return validSpawns.stream().min(Comparator.comparingDouble(pos::getSquaredDistance)).orElse(null);
    }

    public static int getCost(PlayerEntity player, Destination destination) {
        if (player.isCreative()) return 0;
        int cost = 0;
        if (!player.getWorld().getRegistryKey().equals(destination.getWorld())) cost++;
        cost += ModConfigs.GENERAL.getCost(destination.getDistance(player));
        return cost;
    }
}
