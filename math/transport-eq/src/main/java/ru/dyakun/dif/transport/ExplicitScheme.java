package ru.dyakun.dif.transport;

public class ExplicitScheme extends Scheme {

    private final double[] prev;

    public ExplicitScheme(double h, double r, double a, double b) {
        super(h, r, a, b);
        prev = new double[n];
    }

    @Override
    public double[] next() {
        if(t == 0.0) {
            t += tau;
            return values;
        }
        System.arraycopy(values, 0, prev, 0, n);
        values[0] = V.apply(t, a);
        values[n - 1] = V.apply(t, a + (n - 1) * h);
        for(int j = 1; j < n - 1; j++) {
            values[j] = prev[j] * (1 - r) + r * prev[j - 1];
        }
        t += tau;
        return values;
    }

}
