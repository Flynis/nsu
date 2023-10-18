package ru.dyakun.iter;

import java.util.Objects;
import java.util.function.Function;

public record Args(
        double eps,
        double a,
        double b,
        double x0,
        double z,
        Function<Double, Double> f,
        Function<Double, Double> f_
) {
    public Args {
        if(eps <= 0) {
            throw new IllegalArgumentException("eps must be > 0");
        }
        if(b < a) {
            throw new IllegalArgumentException("b must be > a");
        }
        if(x0 < a || x0 > b) {
            throw new IllegalArgumentException("x0 must be in [a, b]");
        }
        if(z < a || z > b) {
            throw new IllegalArgumentException("z must be in [a, b]");
        }
        Objects.requireNonNull(f);
        Objects.requireNonNull(f_);
    }
}
