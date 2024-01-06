package ru.dyakun.dif.transport;

public class ExplicitScheme extends Scheme {

    public ExplicitScheme(double h, double r, double offset, double b) {
        super(h, r, offset, b);
    }

    @Override
    public void calcU() {
        u[0] = Vd.apply(0, 0);
        u[n - 1] = Vd.apply(0, n - 1);
        for(int j = 1; j < n - 1; j++) {
            u[j] = f[j] * (1 - r) + r * f[j - 1];
        }
    }

}
