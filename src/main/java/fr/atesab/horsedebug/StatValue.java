package fr.atesab.horsedebug;

import java.util.function.DoubleFunction;

import net.minecraft.util.Formatting;

public class StatValue {
    public static class StatValueBuilder {
        private double base = 0;
        private double scale = 1;
        private double added = 0;
        private DoubleFunction<Double> showFunction = d -> d;

        private StatValueBuilder() {
        }

        /**
         * add to the base
         * 
         * @param base the value to add
         * @return the builder
         */
        public StatValueBuilder base(double base) {
            this.base += base;
            return this;
        }

        /**
         * scale the stat
         * 
         * @param scale the value to scale
         * @return the builder
         */
        public StatValueBuilder scale(double scale) {
            this.scale *= scale;
            return this;
        }

        /**
         * scale the stat when formatting
         * 
         * @param scale the value to scale
         * @return the builder
         */
        public StatValueBuilder showScale(double scale) {
            return showFunc(d -> d * scale);
        }

        /**
         * scale the stat when formatting
         * 
         * @param showFunction the function
         * @return the builder
         */
        public StatValueBuilder showFunc(DoubleFunction<Double> showFunction) {
            this.showFunction = showFunction;
            return this;
        }

        /**
         * add to the base
         * 
         * @param value the value to add
         * @return the builder
         */
        public StatValueBuilder add(double value) {
            this.added += value;
            return this;
        }

        /**
         * build the StatValue
         * 
         * @return the statvalue
         */
        public StatValue build() {
            double min = base * scale;
            double max = (base + added) * scale;
            return new StatValue(min, max, showFunction);
        }
    }

    /**
     * @return a builder of StatValue
     */
    public static StatValueBuilder builder() {
        return new StatValueBuilder();
    }

    private double min;
    private double max;
    private DoubleFunction<Double> showFunction;

    private StatValue(double min, double max, DoubleFunction<Double> showFunction) {
        this.min = min;
        this.max = max;
        this.showFunction = showFunction;
    }

    /**
     * @return the maximum value
     */
    public double getMax() {
        return max;
    }

    /**
     * @return the average value
     */
    public double getAverage() {
        return (max + min) / 2;
    }

    /**
     * @return the minimum value
     */
    public double getMin() {
        return min;
    }

    /**
     * @return the scale
     */
    public double getScale() {
        return max - min;
    }

    /**
     * normalize a value
     * 
     * @param value the value to normalize
     * @return the normalize value
     */
    public double normalized(double value) {
        return value / getScale();
    }

    /**
     * @return the minimum 80%
     */
    public double getBadValue() {
        return getMin() + getScale() / 5;
    }

    /**
     * @return the top 20%
     */
    public double getExcellentValue() {
        return getMax() - getScale() / 5;
    }

    /**
     * format a value with this stat
     * 
     * @param value the value to format
     * @return the formatted value
     */
    public String getFormattedText(double value) {
        return getFormattedText(value, "", false);
    }

    /**
     * 
     * format a value with this stat
     * 
     * @param value  the value to format
     * @param unit   the unit to format
     * @param stared if the value is stared
     * @return the formatted value
     */
    public String getFormattedText(double value, String unit, boolean stared) {
        return (value >= getExcellentValue() ? Formatting.GOLD
                : (value <= getBadValue()) ? Formatting.RED : Formatting.GREEN).toString()
                + HorseDebugMain.significantNumbers(showFunction.apply(value))
                + unit
                + (stared ? (Formatting.YELLOW + " " + HorseDebugMain.UTF8_STAR) : "");
    }

}
