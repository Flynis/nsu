package ru.nsu.dyakun;

import static java.lang.Math.*;

public class Solver {
    private final double eps;
    private final double a;
    private final double b;
    private final double c;
    private final double[] result = new double[3];
    private int rootsCount = 0;
    private boolean isInfinite = false;

    Solver(String[] args) {
        eps = Double.parseDouble(args[0]);
        a = Double.parseDouble(args[1]);
        b = Double.parseDouble(args[2]);
        c = Double.parseDouble(args[3]);
    }

    Solver(String args) {
        this(args.strip().split("-"));
    }

    private void setInfinite() {
        isInfinite = true;
    }

    private void appendToResult(double val) {
        result[rootsCount] = val;
        rootsCount++;
    }

    public void solve() {
        if(abs(a) < eps) {

        }
    }

    private void solve2() {
        if(abs(b) < eps) {

        }
    }

    private void solve3() {

    }
}
