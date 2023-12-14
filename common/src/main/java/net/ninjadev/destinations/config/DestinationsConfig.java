package net.ninjadev.destinations.config;

import com.google.gson.annotations.Expose;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.ninjadev.destinations.config.option.Option;

public class DestinationsConfig extends Config {

    @Expose private Option<Integer> maxCreated;
    @Expose private Option<Integer> xpCostMultiplier;
    @Expose private Option<String> baseBlock;
    @Expose private Option<String> topBlock;
    @Expose private Option<String> item;

    @Override
    public String getName() {
        return "general";
    }

    @Override
    protected void reset() {
        maxCreated = new Option.IntValue(18, "The maximum number of destinations a player can create.");
        xpCostMultiplier = new Option.IntValue(0, "The multiplier for the cost of XP during travel. Calculation is cost = distanceInBlocks * multiplier. Set to 0 for no cost.");
        baseBlock = new Option.StringValue(Registries.BLOCK.getId(Blocks.POLISHED_ANDESITE).toString(), "The id of the base block for the Destination Multi-Block Structure");
        topBlock = new Option.StringValue(Registries.BLOCK.getId(Blocks.LAPIS_BLOCK).toString(), "The id of the top block for the Destination Multi-Block Structure");
        item = new Option.StringValue(Registries.ITEM.getId(Items.LAPIS_LAZULI).toString(), "The id of the item used to activate the Destination Structure or access the UI.");
    }

    public int getMaxCreated() {
        return maxCreated.getValue();
    }

    public int getXpCostMultiplier() {
        return xpCostMultiplier.getValue();
    }

    public Block getBaseBlock() {
        return Registries.BLOCK.get(new Identifier(this.baseBlock.getValue()));
    }

    public Block getTopBlock() {
        return Registries.BLOCK.get(new Identifier(this.topBlock.getValue()));
    }

    public Item getItem() {
        return Registries.ITEM.get(new Identifier(this.item.getValue()));
    }
}
