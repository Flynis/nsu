package ru.dyakun.dif;

import java.util.function.Function;

public class SchemeKit {
    private final Function<Integer, Double> g;
    private final double h;

    public SchemeKit(double h) {
        this.h = h;
        g = j -> Math.exp(j * h) * Math.cos(j * h);
    }

    public static Func createExactSolution() {
        return xj -> (Math.exp(xj) / 2) * (Math.sin(xj) + Math.cos(xj)) - 0.5;
    }

    public Schema schema1() {
        return (j, y) -> y + h * g.apply(j - 1);
    }

    public Schema schema2() {
        return (j, y) -> y + (h / 2) * (g.apply(j) + g.apply(j - 1));
    }

    public Schema compact() {
        return (j, y) ->
                y + (h / 3) * (g.apply(j - 2) + 4 * g.apply(j - 1) + g.apply(j));
    }

    public double getCompact1Y1() {
        return h * g.apply(0);
    }

    public double getCompact2Y1() {
        return (h / 2) * (g.apply(0) + g.apply(1));
    }

    public double getCompact3Y1() {
        return (h / 12) *
                (8 * g.apply(1) + 5 * g.apply(0) - g.apply(2));
    }

}
