package ru.dykun.dif;

public class SchemeKit {
    private static final Func g = xj -> Math.exp(xj) * Math.cos(xj);
    private final double a;
    private final double h;

    public SchemeKit(double a, double h) {
        this.a = a;
        this.h = h;
    }

    public static Func createExactSolution(double a) {
        double c = -(Math.exp(a) / 2) * (Math.sin(a) + Math.cos(a));
        return xj -> (Math.exp(xj) / 2) * (Math.sin(xj) + Math.cos(xj)) + c;
    }

    public Func schema1() {
        return xj -> (xj - a) * g.apply(xj);
    }

    public Func schema2() {
        return xj -> ((xj - a) / 2) * (g.apply(xj + h) + g.apply(xj));
    }

    private Double g4(double xj) {
        return g.apply(xj + h) + 4 * g.apply(xj) + g.apply(xj - h);
    }

    private Func createCompact(double y1) {
        return xj -> {
            double res = ((xj - a) / 2 / 3) * g4(xj);
            int s = (int)((xj - a) / h) % 2;
            return res * s + y1;
        };
    }

    public Func compact1() {
        return createCompact(h * g.apply(a));
    }

    public Func compact2() {
        return createCompact((h / 2) * (g.apply(a) + g.apply(a + h)));
    }

    public Func compact3() {
        return createCompact((h / 12) *
                (8 * g.apply(a + h) + 5 * g.apply(a) - g.apply(a + 2 * h)));
    }

}
