package ru.nsu.dyakun.integral;

import java.util.Objects;
import java.util.function.Function;

public record Args(int n, double a, double b, Function<Double, Double> f) {
    public Args {
        if(n < 1) {
            throw new IllegalArgumentException("n must be >= 1");
        }
        if(b < a) {
            throw new IllegalArgumentException("b must be > a");
        }
        Objects.requireNonNull(f);
    }
}
