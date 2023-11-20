package ru.dyakun.iter;

import java.util.function.Function;

import static java.lang.Math.abs;

public class IterativeMethods {
    private final double eps;
    private final double a;
    private final double b;
    private final Function<Double, Double> f;
    private int iterationCounter = 0;

    public IterativeMethods(Args args) {
        eps = args.eps();
        a = args.a();
        b = args.b();
        f = args.f();
    }

    public int getIterationCounter() {
        int result = iterationCounter;
        iterationCounter = 0;
        return result;
    }

    public double findRootBisect() {
        return findRootBisect(a, b);
    }

    private double findRootBisect(double _a, double _b) {
        iterationCounter++;
        double fa = f.apply(_a);
        double fb = f.apply(_b);
        checkInterval(fa, fb);
        if(fa > 0.0 && fb < 0.0) {
            double tmp = _a;
            _a = _b;
            _b = tmp;
        }
        double c = (_b + _a) / 2;
        double fc = f.apply(c);
        //System.out.printf("bisect: f(%f) = %f %f%n", c, fc, abs(fc));
        if(abs(fc) < eps) {
            return c;
        }
        if(fc > eps) {
            return findRootBisect(_a, c);
        } else {
            return findRootBisect(c, _b);
        }
    }

    private void checkInterval(double fa, double fb) {
        if(fa > 0.0 && fb > 0.0 || fa < -0.0 && fb < -0.0) {
            throw new IllegalArgumentException("f(a) * f(b) must be < 0");
        }
    }
}
