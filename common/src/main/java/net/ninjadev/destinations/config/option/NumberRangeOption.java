package net.ninjadev.destinations.config.option;

import com.google.gson.annotations.Expose;

public class NumberRangeOption extends Option<Double> {

    @Expose private final Double min;
    @Expose private final Double max;

    public NumberRangeOption(Double defaultValue, Double minValue, Double maxValue, String comment) {
        super(defaultValue, comment);
        this.min = minValue;
        this.max = maxValue;

        withinRange(defaultValue);
    }

    public double clampValue(double value) {
        return this.clamp(value, this.min, this.max);
    }

    private double clamp(double num, double min, double max) {
        if (num < min) {
            return min;
        } else {
            return Math.min(num, max);
        }
    }

    public boolean withinRange(double range) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum value cannot be larger than maximum value!");
        } else if (range < min) {
            throw new IllegalArgumentException("Value cannot be lower than minimum value!");
        } else if (range > max) {
            throw new IllegalArgumentException("Value cannot be larger than maximum value!");
        }
        return true;
    }

    public void set(int range) {
        if (this.withinRange(range)) this.value = (double) range;
    }
}
