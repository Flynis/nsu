package ru.dyakun.dif.transport;

public class ImplicitScheme extends Scheme {

    public ImplicitScheme(double h, double r, double offset, double b) {
        super(h, r, offset, b);
    }

    @Override
    public void calcU() {
        double a = -r / 2;
        double b = r / 2;
        double c = 1.0;
        double[] alpha = new double[n - 1];
        double[] beta = new double[n - 1];
        alpha[0] = 0;
        beta[0] = Vd.apply(t, 0);
        for(int j = 1; j < n - 1; j++) {
            double divisor = a * alpha[j - 1] + c;
            alpha[j] = -b / divisor;
            beta[j] = (f[j] - a * beta[j - 1]) / divisor;
        }
        u[n - 1] = Vd.apply(t, n - 1);
        for(int j = n - 2; j >= 0; j--) {
            u[j] = alpha[j] * u[j + 1] + beta[j];
        }
        if(Math.abs(t - 7.0) < tau) {
            logger.info("h = {}, tau = {}, r = {}, t = {}", h, tau, r, t);
            for(int j = 0; j < n; j++) {
                logger.info("{}   {}", offset + j * h, u[j]);
            }
        }
    }

}
