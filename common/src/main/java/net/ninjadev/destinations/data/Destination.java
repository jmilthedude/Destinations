package net.ninjadev.destinations.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ninjadev.destinations.init.ModConfigs;
import net.ninjadev.destinations.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Destination implements INBTSerializable<NbtCompound> {

    private UUID id;
    private UUID owner;
    private String name;
    private int x;
    private int y;
    private int z;
    private RegistryKey<World> world;

    @Nullable
    private Item icon;


    public Destination(UUID owner, String name, int x, int y, int z, RegistryKey<World> world, String iconId) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        if (iconId == null) return;
        Identifier iconIdentifier = new Identifier(iconId);
        if (Registries.ITEM.getIds().contains(iconIdentifier)) this.icon = Registries.ITEM.get(iconIdentifier);
    }

    public Destination(UUID owner, String name, int x, int y, int z, RegistryKey<World> world) {
        this(owner, name, x, y, z, world, null);
    }

    public Destination(UUID owner, String name, BlockPos pos, RegistryKey<World> world) {
        this(owner, name, pos.getX(), pos.getY(), pos.getZ(), world);
    }

    public Destination(UUID owner, String name, BlockPos pos, RegistryKey<World> world, String iconId) {
        this(owner, name, pos.getX(), pos.getY(), pos.getZ(), world, iconId);
    }

    public Destination(NbtCompound nbt) {
        this.deserialize(nbt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public RegistryKey<World> getWorld() {
        return world;
    }

    public Optional<Item> getIcon() {
        return Optional.ofNullable(icon);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public int getDistance(PlayerEntity player) {
        double distance = player.squaredDistanceTo(this.x, this.y, this.z);
        return (int) Math.sqrt(distance);
    }

    public ItemStack createStack(int xpCost, int currentXp) {
        boolean canTravel = xpCost <= currentXp;
        Item item = this.getIcon().isPresent() ? this.getIcon().get() : canTravel ? Items.GREEN_STAINED_GLASS_PANE : Items.RED_STAINED_GLASS_PANE;
        ItemStack stack = new ItemStack(item);
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.put("destination", this.serialize());
        return stack;
    }

    public List<Text> createTooltip(PlayerEntity player) {
        int distance = this.getDistance(player);
        int cost = ModConfigs.GENERAL.getCost(distance);
        boolean canTravel = cost <= player.experienceLevel;
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.literal(this.name).formatted(Formatting.DARK_AQUA));
        tooltip.add(Text.empty());
        tooltip.add(Text.literal(String.format("%s, %s, %s in %s", this.x, this.y, this.z, this.world.getValue().getPath().toUpperCase())));
        tooltip.add(Text.literal(String.format("Exp Cost: %s%s%s Levels", (canTravel ? Formatting.GREEN : Formatting.RED), cost, Formatting.RESET)));
        return tooltip;
    }

    @Override
    public NbtCompound serialize() {
        NbtCompound compound = new NbtCompound();
        compound.putUuid("id", this.id);
        compound.putUuid("owner", this.owner);
        compound.putString("name", this.name);
        compound.putInt("x", this.x);
        compound.putInt("y", this.y);
        compound.putInt("z", this.z);
        compound.putString("world", world.getValue().toString());
        if (this.icon != null) compound.putString("icon", Registries.ITEM.getId(this.icon).toString());
        return compound;
    }

    @Override
    public void deserialize(NbtCompound nbt) {
        this.id = nbt.getUuid("id");
        this.owner = nbt.getUuid("owner");
        this.name = nbt.getString("name");
        this.x = nbt.getInt("x");
        this.y = nbt.getInt("y");
        this.z = nbt.getInt("z");
        this.world = RegistryKey.of(RegistryKeys.WORLD, new Identifier(nbt.getString("world")));
        if (nbt.contains("icon")) this.icon = Registries.ITEM.get(new Identifier(nbt.getString("icon")));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Destination that)) return false;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner);
    }
}
