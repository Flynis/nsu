package ru.dyakun.slae;

public class Matrix {
    private static final int FIRST = 2;
    private static final int SECOND = -1;
    private final int n;
    private final double[] array;

    public Matrix(int n) {
        this.n = n;
        array = new double[n * n];
    }

    public double get(int i, int j) {
        return array[i * n + j];
    }

    public void set(int i, int j, double val) {
        array[i * n + j] = val;
    }

    public double[] fillDefault() {
        double[] f = new double[n];
        for(int i = 0; i < n; i++) {
            f[i] = FIRST;
        }
        set(0, 0, FIRST);
        set(0, 1, SECOND);
        for(int i = 1; i < n - 1; i++) {
            set(i, i - 1, SECOND);
            set(i, i, FIRST);
            set(i, i + 1, SECOND);
        }
        set(n - 1, n - 1, FIRST);
        set(n - 1, n - 2, SECOND);
        return f;
    }

    public double[] fillEps(double eps) {
        double[] f = fillDefault();
        for(int i = 0; i < n; i++) {
            f[i] += eps;
        }
        return f;
    }

    public double[] fillGamma(double gamma) {
        double[] f = new double[n];
        for(int i = 0; i < n; i++) {
            f[i] = 2 * (i + 2) + gamma;
        }
        set(0, 0, 2 + gamma);
        set(0, 1, SECOND);
        for(int i = 1; i < n - 1; i++) {
            set(i, i - 1, SECOND);
            set(i, i, 2 * (i + 1) + gamma);
            set(i, i + 1, SECOND);
        }
        set(n - 1, n - 1, 2 * n + gamma);
        set(n - 1, n - 2, SECOND);
        return f;
    }

}
