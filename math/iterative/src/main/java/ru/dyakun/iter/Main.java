package ru.dyakun.iter;

public class Main {
    public static void main(String[] args) {
        Args f2 = new Args(0.00001, -4, -3, x -> Math.pow(x, 3) - 10 * x + 20);
        Args f3 = new Args(0.00001, 2, 4, x -> Math.pow(x, 3) - 4 * x - 10);
        printSolve("f2", f2, -3.89102041);
        printSolve("f3", f3, 2.76081783);
    }

    private static void printSolve(String name, Args args, double root) {
        var iter = new IterativeMethods(args);
        double z = iter.findRootBisect();
        int iterations = iter.getIterationCounter();
        System.out.printf("%s: expected root = %.8f%n", name, root);
        System.out.printf("%s: result = %.8f, diff = %.8f, iterations = %d%n", name, z, Math.abs(z - root), iterations);
    }
}