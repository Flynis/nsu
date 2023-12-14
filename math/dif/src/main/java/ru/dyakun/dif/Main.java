package ru.dyakun.dif;

public class Main {

    private static final int DIVISOR = 3;

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Expected parameters: <b h>");
            return;
        }
        double b = Double.parseDouble(args[0]);
        double h = Double.parseDouble(args[1]);
        double h2 = h / DIVISOR;
        System.out.printf("b=%.8f h1=%.8f h2=%.8f%n", b, h, h2);

        SchemeKit kit1 = new SchemeKit(h);
        SchemeKit kit2 = new SchemeKit(h2);
        Func exact = SchemeKit.createExactSolution();

        System.out.println("\nSchema1");
        printSimple(b, h, exact, kit1.schema1(), kit2.schema1());

        System.out.println("\nSchema2");
        printSimple(b, h, exact, kit1.schema2(), kit2.schema2());

        System.out.println("\nCompact1");
        printCompact(b, h, exact, kit1.compact(), kit2.compact(), kit1.getCompact1Y1(), kit2.getCompact1Y1());

        System.out.println("\nCompact2");
        printCompact(b, h, exact, kit1.compact(), kit2.compact(), kit1.getCompact2Y1(), kit2.getCompact2Y1());

        System.out.println("\nCompact3");
        printCompact(b, h, exact, kit1.compact(), kit2.compact(), kit1.getCompact3Y1(), kit2.getCompact3Y1());

    }

    public static void printSimple(double b, double h, Func exact, Schema schema1, Schema schema2) {
        System.out.println(" j         xj            ex            ydh1         ydh2         pj            yh1           yh2");
        int n = (int) (b / h);
        int j = 0;
        int j3 = 0;
        double xj = 0.0;
        double yex = exact.apply(xj);
        double y1 = 0.0;
        double y2 = 0.0;
        double dyh1 = 0.0;
        double dyh2 = 0.0;
        double pj = 1.0;
        System.out.printf("%3d  %12.8f  %12.8f  %12.8f %12.8f %12.8f  %12.8f  %12.8f%n", j, xj, yex, dyh1, dyh2, pj, y1, y2);

        for(j = 1; j < n; j += 1) {
            xj = j * h;
            yex = exact.apply(xj);

            y1 = schema1.apply(j, y1);
            do {
                j3 += 1;
                y2 = schema2.apply(j3, y2);
            } while (j3 / DIVISOR != j);

            dyh1 = Math.abs(yex - y1);
            dyh2 = Math.abs(yex - y2);

            pj = Math.log(dyh1 / dyh2) / Math.log(3);

            System.out.printf("%3d  %12.8f  %12.8f  %12.8f %12.8f %12.8f  %12.8f  %12.8f%n", j, xj, yex, dyh1, dyh2, pj, y1, y2);
        }
    }

    public static void printCompact(double b, double h, Func exact, Schema schema1, Schema schema2, double y1p, double y2p) {
        System.out.println(" j         xj            ex            ydh1         ydh2         pj            yh1           yh2");
        int n = (int) (b / h);
        int j = 0;
        int j3 = 0;
        double xj = 0.0;
        double yex = exact.apply(xj);
        double y1pp = 0.0;
        double y2pp = 0.0;
        double y1 = 0.0;
        double y2 = 0.0;
        double dyh1 = 0.0;
        double dyh2 = 0.0;
        double pj = 1.0;
        System.out.printf("%3d  %12.8f  %12.8f  %12.8f %12.8f %12.8f  %12.8f  %12.8f%n", j, xj, yex, dyh1, dyh2, pj, y1, y2);

        j++;
        xj = j * h;
        yex = exact.apply(xj);
        y1 = y1p;
        j3++;
        do {
            j3 += 1;
            y2 = schema2.apply(j3, y2pp);
            y2pp = y2p;
            y2p = y2;
        } while (j3 / DIVISOR != j);

        dyh1 = Math.abs(yex - y1);
        dyh2 = Math.abs(yex - y2);

        pj = Math.log(dyh1 / dyh2) / Math.log(3);

        System.out.printf("%3d  %12.8f  %12.8f  %12.8f %12.8f %12.8f  %12.8f  %12.8f%n", j, xj, yex, dyh1, dyh2, pj, y1, y2);

        for(j = 2; j < n; j += 1) {
            xj = j * h;
            yex = exact.apply(xj);

            y1 = schema1.apply(j, y1pp);
            y1pp = y1p;
            y1p = y1;
            do {
                j3 += 1;
                y2 = schema2.apply(j3, y2pp);
                y2pp = y2p;
                y2p = y2;
            } while (j3 / DIVISOR != j);

            dyh1 = Math.abs(yex - y1);
            dyh2 = Math.abs(yex - y2);

            pj = Math.log(dyh1 / dyh2) / Math.log(3);

            System.out.printf("%3d  %12.8f  %12.8f  %12.8f %12.8f %12.8f  %12.8f  %12.8f%n", j, xj, yex, dyh1, dyh2, pj, y1, y2);
        }
    }

}