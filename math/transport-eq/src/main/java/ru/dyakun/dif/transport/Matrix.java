package ru.dyakun.dif.transport;

public class Matrix {

    private final int n;
    private final double[] array;

    public Matrix(int n) {
        this.n = n;
        array = new double[n * n];
    }

    public double get(int i, int j) {
        return array[i * n + j];
    }

    public void set(int i, int j, double val) {
        array[i * n + j] = val;
    }

}
