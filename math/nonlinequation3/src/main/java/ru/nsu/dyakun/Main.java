package ru.nsu.dyakun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        if(args.length == 4) {
            Solver solver = new Solver(args);
            printResult(solver.solve());
            return;
        }
        if(args.length == 0) {
            var in = new BufferedReader(new InputStreamReader(System.in, System.console().charset())) ;
            try {
                while (true) {
                    String input = in.readLine();
                    if("exit".equals(input)) {
                        return;
                    }
                    Solver solver = new Solver(input);
                    printResult(solver.solve());
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return;
        }
        System.out.println("Expect 4 options");
    }

    private static void printResult(double[] arr) {
        int i = 0;
        while (i < arr.length && !Double.isNaN(arr[i])) {
            System.out.printf("%f ", arr[i]);
            i++;
        }
        System.out.println(" ");
    }
}
