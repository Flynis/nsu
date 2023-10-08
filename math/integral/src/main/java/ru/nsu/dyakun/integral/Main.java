package ru.nsu.dyakun.integral;

import java.util.function.Function;

import static ru.nsu.dyakun.integral.Integral.*;

public class Main {
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Expect number of elementary segments");
            return;
        }
        int n = Integer.parseInt(args[0]);
        double a = 5;
        double b = 7;
        double expected = 823.7226395026771;
        Function<Double, Double> f = x -> Math.exp(x) * Math.cos(x);
        calc(expected, new Args(n, a, b, f));
    }

    private static void calc(double expected, Args args) {
        System.out.printf("Expected: %.10f%n", expected);
        double trapezoid = calcTrapezoid(args);
        System.out.printf("Trapezoid: %.10f %.8f [%.6f]%n",
                trapezoid,
                Math.abs(expected - trapezoid),
                calcOrderOfAccuracy(args, Integral::calcTrapezoid));
        double simpson = calcSimpson(args);
        System.out.printf("Simpson: %.10f %.8f [%.6f]%n",
                simpson,
                Math.abs(expected - simpson),
                calcOrderOfAccuracy(args, Integral::calcSimpson));
        double quadra = calcQuadratureFormulaOn4Nodes(args);
        System.out.printf("QuadratureFormulaOn4Nodes: %.10f %.8f [%.6f]%n",
                quadra,
                Math.abs(expected - quadra),
                calcOrderOfAccuracy(args, Integral::calcQuadratureFormulaOn4Nodes));
    }
}