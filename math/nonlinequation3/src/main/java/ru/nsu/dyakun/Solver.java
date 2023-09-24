package ru.nsu.dyakun;

import static java.lang.Math.*;

public class Solver {
    private static final double delta = 1.0;
    private final double eps;
    private final double _a;
    private final double _b;
    private final double _c;
    private final double[] roots = { Double.NaN, Double.NaN, Double.NaN };
    private int rootsCount = 0;

    Solver(String[] args) {
        eps = Double.parseDouble(args[0]);
        _a = Double.parseDouble(args[1]);
        _b = Double.parseDouble(args[2]);
        _c = Double.parseDouble(args[3]);
        System.out.printf("eps = %f a = %f b = %f c = %f%n", eps, _a, _b, _c);
    }

    Solver(String args) {
        this(args.strip().split(" "));
    }

    private void appendRoot(double val) {
        roots[rootsCount] = val;
        rootsCount++;
        System.out.println("Root added");
    }

    private double f(double x) {
        double x2 = x * x;
        return x2 * x + _a * x2 + _b * x + _c;
    }

    public double[] solve() {
        exploreDerivativeOfFunction();
        return roots;
    }

    private void exploreDerivativeOfFunction() {
        // 3x^2 + 2ax + b
        double d = _a * _a - 3 * _b;
        System.out.printf("d = %f%n", d);
        if(d > eps) {
            double alpha = (-_a + sqrt(d)) / 3;
            double beta = (-_a - sqrt(d)) / 3;
            if(beta < alpha) {
                double tmp = alpha;
                alpha = beta;
                beta = tmp;
            }
            double falpha = f(alpha);
            double fbeta = f(beta);
            System.out.printf("alpha: f(%f) = %f | beta: f(%f) = %f%n", alpha, falpha, beta, fbeta);
            if(falpha > eps) {
                if(abs(fbeta) < eps) {
                    appendRoot(beta);
                    appendRoot(roots[0]);
                    appendRoot(findRootMinusInf(alpha));
                } else if(fbeta < -eps) {
                    appendRoot(findRootMinusInf(alpha));
                    appendRoot(findRootBisect(alpha, beta));
                    appendRoot(findRootInf(beta));
                } else {
                    appendRoot(findRootMinusInf(alpha));
                }
            } else if(falpha < - eps && fbeta < -eps) {
                appendRoot(findRootInf(beta));
            } else if(abs(falpha) < eps) {
                if(fbeta < -eps) {
                    appendRoot(alpha);
                    appendRoot(roots[0]);
                    appendRoot(findRootInf(beta));
                } else if (abs(fbeta) < eps) {
                    appendRoot((alpha + beta) / 2);
                    appendRoot(roots[0]);
                    appendRoot(roots[0]);
                }
            }
            return;
        }
        double f0 = f(0.0);
        if(abs(f0) < eps) {
            appendRoot(0.0);
        } else if(f0 > eps) {
            appendRoot(findRootMinusInf(0.0));
        } else {
            appendRoot(findRootInf(0.0));
        }
        double gamma = -_a / 3;
        System.out.printf("gamma: %f%n", gamma);
        if(abs(d) < eps && abs(gamma - roots[0]) < eps) {
            appendRoot(roots[0]);
            appendRoot(roots[0]);
        }
    }

    private double findRootInf(double a) {
        System.out.printf("[%f, inf)%n", a);
        double d = delta;
        while (f(a + d) < 0.0) {
            d *= 2;
        }
        return findRootBisect(a, a + d);
    }

    private double findRootMinusInf(double b) {
        System.out.printf("(-inf, %f]%n", b);
        double d = delta;
        while (f(b - d) > 0.0) {
            d *= 2;
        }
        return findRootBisect(b - d, b);
    }

    private double findRootBisect(double a, double b) {
        double fa = f(a);
        double fb = f(b);
        System.out.printf("bisect: f(%f) = %f f(%f) = %f%n", a, fa, b, fb);
        if(fa > 0.0 && fb > 0.0 || fa < -0.0 && fb < -0.0) {
            throw new IllegalArgumentException("f(a) * f(b) must be < 0");
        }
        if(abs(fa) < eps) {
            return a;
        }
        if(abs(fb) < eps) {
            return b;
        }
        if(fa > 0.0 && fb < 0.0) {
            double tmp = a;
            a = b;
            b = tmp;
        }
        double c = (b + a) / 2;
        double fc = f(c);
        System.out.printf("bisect: f(%f) = %f %f%n", c, fc, abs(fc));
        if(abs(fc) < eps) {
            return c;
        }
        if(fc > eps) {
            return findRootBisect(a, c);
        } else {
            return findRootBisect(c, b);
        }
    }
}
