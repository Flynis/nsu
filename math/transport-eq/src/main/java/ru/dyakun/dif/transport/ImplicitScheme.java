package ru.dyakun.dif.transport;

public class ImplicitScheme extends Scheme {

    public ImplicitScheme(double h, double r, double offset, double b) {
        super(h, r, offset, b);
    }

    public void calcU() {
        u[0] = Vd.apply(0, 0);
        u[n - 1] = Vd.apply(0, n - 1);
        double a = -r / 2;
        double b = r / 2;
        double c = 1.0;
        f[1] += b * u[0];
        f[n - 2] += a * u[n - 1];
        double[] alpha = new double[n - 1];
        double[] beta = new double[n - 1];
        alpha[0] = -b / c;
        beta[1] = f[1] / c;
        for(int j = 2; j <= n - 2; j++) {
            double divisor = a * alpha[j - 1] + c;
            alpha[j] = -b / divisor;
            beta[j] = (f[j] - a * beta[j - 1]) / divisor;
        }
        u[n - 2] = beta[n - 2];
        for(int j = n - 3; j > 0; j--) {
            u[j] = alpha[j] * u[j + 1] + beta[j];
        }
    }

}
