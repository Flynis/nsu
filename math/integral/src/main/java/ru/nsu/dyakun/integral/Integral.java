package ru.nsu.dyakun.integral;

import java.util.function.Function;

public class Integral {
    private Integral() {
        throw new AssertionError();
    }

    public static double calcTrapezoid(Args args) {
        int n = args.n();
        double a = args.a();
        double b = args.b();
        var f = args.f();
        double h = (b - a) / n;
        double sum = 0;
        double i = a + h;
        for(int j = 1; j < n; j++, i += h) {
            sum += f.apply(i);
        }
        return h / 2 * (f.apply(a) + f.apply(b) + 2 * sum);
    }

    public static double calcSimpson(Args args) {
        int n = args.n();
        double a = args.a();
        double b = args.b();
        var f = args.f();
        if(n % 2 != 0) {
            throw new IllegalArgumentException("n must be even");
        }
        int N = n / 2;
        double h = (b - a) / n;
        double delta = 2 * h;
        double sum_2i_1 = 0;
        double i = a + h;
        for(int j = 0; j < N; j++, i += delta) {
            sum_2i_1 += f.apply(i);
        }
        double sum_2i = 0;
        i = a + delta;
        for(int j = 1; j < N; j++, i += delta) {
            sum_2i += f.apply(i);
        }
        return h / 3 * (f.apply(a) + f.apply(b) + 4 * sum_2i_1 + 2 * sum_2i);
    }

    public static double calcQuadratureFormulaOn4Nodes(Args args) {
        int n = args.n();
        double a = args.a();
        double b = args.b();
        var f = args.f();
        if(n % 3 != 0) {
            throw new IllegalArgumentException("n must be divisible by 3");
        }
        int N = n / 3;
        double delta = (b - a) / N;
        double sum_3i_1_2 = 0;
        double i = a + delta / 3;
        for(int j = 0; j < N; j++, i += delta) {
            sum_3i_1_2 += f.apply(i) + f.apply(i + delta / 3);
        }
        double sum_3i = 0;
        i = a + delta;
        for(int j = 1; j < N; j++, i += delta) {
            sum_3i += f.apply(i);
        }
        return delta / 8 * (f.apply(a) + f.apply(b) + 3 * sum_3i_1_2 + 2 * sum_3i);
    }

    public static double calcOrderOfAccuracy(Args args, Function<Args ,Double> f) {
        double s1 = f.apply(args);
        double s2 = f.apply(new Args(args.n() * 2, args.a(), args.b(), args.f()));
        double s3 = f.apply(new Args(args.n() * 4, args.a(), args.b(), args.f()));
        return Math.log(Math.abs((s1 - s2) / (s2 - s3))) / Math.log(2);
    }
}
