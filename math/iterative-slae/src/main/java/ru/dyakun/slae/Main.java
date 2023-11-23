package ru.dyakun.slae;

public class Main {

    private static void print(double[] x) {
        for(int i = 0; i < x.length; i++) {
            System.out.printf("x[%d] = %.9f%n", i + 1, x[i]);
        }
    }

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Expected n, eps and gamma");
            return;
        }
        int n = Integer.parseInt(args[0]);
        double eps = Double.parseDouble(args[1]);
        double gamma = Double.parseDouble(args[2]);
        Matrix a1 = new Matrix(n);
        double[] f1 = a1.fillDefault();
        double[] x1 = Slae.calcIterative(a1, f1);
        System.out.println("Test 1");
        print(x1);
        Matrix a2 = new Matrix(n);
        double[] f2 = a2.fillEps(eps);
        double[] x2 = Slae.calcIterative(a2, f2);
        System.out.printf("Test 2 [f = %.6f]%n", f2[0]);
        print(x2);
        Matrix a3 = new Matrix(n);
        double[] f3 = a3.fillGamma(gamma);
        double[] x3 = Slae.calcIterative(a3, f3);
        System.out.printf("Test 3 [c1 = %.6f, f1 = %.6f]%n", a3.get(0, 0), f3[0]);
        print(x3);
    }

}
