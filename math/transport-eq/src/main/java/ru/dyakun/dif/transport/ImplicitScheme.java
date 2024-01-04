package ru.dyakun.dif.transport;

public class ImplicitScheme extends Scheme {

    private final double[] f;
    private final Matrix matrix;

    public ImplicitScheme(double h, double r, double a, double b) {
        super(h, r, a, b);
        f = new double[n];
        matrix = new Matrix(n);
        matrix.set(0, 0, 1.0);
        matrix.set(n - 1, n - 1, 1.0);
        for(int j = 1; j < n - 1; j++) {
            matrix.set(j, j - 1, -r / 2);
            matrix.set(j, j, 1.0);
            matrix.set(j, j + 1, r / 2);
        }
    }

    @Override
    public double[] next() {
        if(t == 0.0) {
            t += tau;
            return values;
        }
        System.arraycopy(values, 0, f, 0, n);
        f[0] = V.apply(t, a);
        f[n - 1] = V.apply(t, a + (n - 1) * h);
        Slae.solve(matrix, f, values);
        t += tau;
        return values;
    }

}
