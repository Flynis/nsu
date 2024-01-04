package ru.dyakun.dif.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Scheme {

    public static final Function V = (t, x) -> (x - t < 0) ? 1 : 0;

    protected static final Logger logger = LoggerFactory.getLogger(Scheme.class);
    protected final double[] values;
    protected final double h;
    protected final double r;
    protected final double tau;
    protected final double a;
    protected final int n;
    protected double t;

    public Scheme(double h, double r, double a, double b) {
        this.h = h;
        this.r = r;
        this.a = a;
        tau = r * h;
        n = (int) ((b - a) / h) + 1;
        logger.info("N = {}", n);
        values = new double[n];
        t = 0.0;
        for(int j = 0; j < n; j++) {
            values[j] = V.apply(0, a + j * h);
        }
    }

    public abstract double[] next();

    public double getTime() {
        return t;
    }

}
