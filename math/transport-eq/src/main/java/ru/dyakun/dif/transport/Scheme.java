package ru.dyakun.dif.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Scheme {

    public static final Function V = (t, x) -> (x - t < 0) ? 1 : 0;
    protected final Function Vd;

    protected static final Logger logger = LoggerFactory.getLogger(Scheme.class);
    protected final double[] u;
    protected final double[] f;
    protected final double h;
    protected final double r;
    protected final double tau;
    protected final double offset;
    protected final int n;
    protected double t;

    public Scheme(double h, double r, double offset, double b) {
        this.h = h;
        this.r = r;
        this.offset = offset;
        tau = r * h;
        n = (int) ((b - offset) / h) + 1;
        logger.info("N = {}", n);
        u = new double[n];
        f = new double[n];
        t = 0.0;
        Vd = (t, j) -> V.apply(t, offset + j * h);
        for(int j = 0; j < n; j++) {
            u[j] = Vd.apply(0, j);
        }
    }

    public abstract void calcU();

    public double[] next() {
        if(t == 0.0) {
            t += tau;
            return u;
        }
        System.arraycopy(u, 0, f, 0, n);
        calcU();
        t += tau;
        return u;
    }

    public double getTime() {
        return t;
    }

}
