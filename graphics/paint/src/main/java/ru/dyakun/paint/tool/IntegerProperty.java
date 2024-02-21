package ru.dyakun.paint.tool;

public class IntegerProperty {

    private int val;
    private final int max;
    private int min;
    private final String name;

    public IntegerProperty(int val, int min, int max, String name) {
        this.val = val;
        this.min = min;
        this.max = max;
        this.name = name;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        if(val < min) {
            val = min;
        }
        if(val > max) {
            val = max;
        }
        this.val = val;
    }

    public String getName() {
        return name;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        if (min <= val) {
            this.min = min;
        } else {
            throw new IllegalStateException("Min must be <= val");
        }
    }

}
