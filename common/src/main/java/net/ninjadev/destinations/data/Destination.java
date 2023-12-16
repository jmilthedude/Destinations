package net.ninjadev.destinations.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ninjadev.destinations.util.DestinationUtil;
import net.ninjadev.destinations.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
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
        Identifier iconIdentifier = Identifier.tryParse(iconId == null ? "" : iconId);
        if (iconIdentifier == null) return;
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

    public boolean isOwner(ServerPlayerEntity player) {
        return player.getUuid().equals(this.owner);
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

    public boolean canTravel(PlayerEntity player) {
        int cost = DestinationUtil.getCost(player, this);
        return cost <= player.experienceLevel;
    }

    public ItemStack createStack(PlayerEntity player) {
        int distance = this.getDistance(player);
        int cost = DestinationUtil.getCost(player, this);
        Item item = this.getIcon().isPresent() ? this.getIcon().get() : this.canTravel(player) ? Items.LIME_BANNER : Items.RED_BANNER;
        ItemStack stack = new ItemStack(item);
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound displayNbt = new NbtCompound();
        nbt.put("destination", this.serialize());
        NbtList loreNbt = new NbtList();
        List<Text> tooltip = this.createTooltip(this.canTravel(player), distance, cost);
        tooltip.stream().map(Text.Serializer::toJson).map(NbtString::of).forEach(loreNbt::add);
        displayNbt.put(ItemStack.LORE_KEY, loreNbt);
        nbt.put(ItemStack.DISPLAY_KEY, displayNbt);
        stack.setCustomName(Text.literal(String.format("%s%s%s", Formatting.RESET, Formatting.DARK_AQUA, this.name)));
        return stack;
    }

    public List<Text> createTooltip(boolean canTravel, int distance, int cost) {
        String dimension = this.getWorld().getValue().getPath();
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.empty());
        tooltip.add(Text.literal(
                String.format("%s%s%s%s, %s%s%s, %s%s%s in %s%s",
                        Formatting.RESET, Formatting.YELLOW, this.x, Formatting.WHITE,
                        Formatting.YELLOW, this.y, Formatting.WHITE,
                        Formatting.YELLOW, this.z, Formatting.WHITE,
                        this.getDimensionColor(dimension), dimension))
        );
        tooltip.add(Text.literal(String.format("%sDistance: %s%s", Formatting.WHITE, Formatting.YELLOW, distance)));
        tooltip.add(Text.literal(String.format("%sExp Cost: %s%s%s Levels", Formatting.WHITE, (canTravel ? Formatting.GREEN : Formatting.RED), cost, Formatting.WHITE)));
        return tooltip;
    }

    @NotNull
    private Formatting getDimensionColor(String dimension) {
        switch (dimension) {
            case "OVERWORLD" -> {
                return Formatting.DARK_GREEN;
            }
            case "NETHER" -> {
                return Formatting.DARK_RED;
            }
            case "THE_END" -> {
                return Formatting.DARK_PURPLE;
            }
            default -> {
                return Formatting.WHITE;
            }
        }
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
