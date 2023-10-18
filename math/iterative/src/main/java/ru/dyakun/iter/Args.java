package ru.dyakun.iter;

import java.util.Objects;
import java.util.function.Function;

public record Args(
        double eps,
        double a,
        double b,
        Function<Double, Double> f
) {
    public Args {
        if(eps <= 0) {
            throw new IllegalArgumentException("eps must be > 0");
        }
        if(b < a) {
            throw new IllegalArgumentException("b must be > a");
        }
        Objects.requireNonNull(f);
    }
}
