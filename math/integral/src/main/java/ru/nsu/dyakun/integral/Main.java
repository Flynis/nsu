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
        System.out.printf("Trapezoid: %.10f [%.2f]%n",
                calcTrapezoid(args),
                calcOrderOfAccuracy(args, Integral::calcTrapezoid));
        System.out.printf("Simpson: %.10f [%.2f]%n",
                calcSimpson(args),
                calcOrderOfAccuracy(args, Integral::calcSimpson));
        System.out.printf("QuadratureFormulaOn4Nodes: %.10f [%.2f]%n",
                calcQuadratureFormulaOn4Nodes(args),
                calcOrderOfAccuracy(args, Integral::calcQuadratureFormulaOn4Nodes));
    }
}