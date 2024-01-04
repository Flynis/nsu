package ru.dyakun.dif.transport;

public class Slae {

    private Slae() {
        throw new AssertionError();
    }

    public static void solve(Matrix a, double[] f, double[] x) {
        int n = f.length;
        double[] alpha = new double[n];
        double[] beta = new double[n];
        alpha[0] = -a.get(0, 1) / a.get(0, 0);
        beta[0] = f[0] / a.get(0, 0);
        for(int i = 1; i < n - 1; i++) {
            double ai = -a.get(i, i - 1);
            double bi = -a.get(i, i + 1);
            double divisor = a.get(i, i) - alpha[i - 1] * ai;
            alpha[i] = bi / divisor;
            beta[i] = (f[i] + beta[i - 1] * ai) / divisor;
        }
        alpha[n - 1] = 0;
        double an = -a.get(n - 1,  n - 2);
        beta[n - 1] = (f[n - 1] + beta[n - 2] * an) /
                (a.get(n - 1, n - 1) - alpha[n - 2] * an);
        x[n - 1] = beta[n - 1];
        for(int i = n - 2; i >= 0; i--) {
            x[i] = alpha[i] * x[i + 1] + beta[i];
        }
    }

}
