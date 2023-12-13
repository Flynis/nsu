package ru.dykun.dif;

public class Main {

    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Expected parameters: <a b h>");
        }
        double a = Double.parseDouble(args[0]);
        double b = Double.parseDouble(args[1]);
        double h = Double.parseDouble(args[2]);

        SchemeKit kit1 = new SchemeKit(a, h);
        SchemeKit kit2 = new SchemeKit(a, h / 3);
        Func exact = SchemeKit.createExactSolution(a);

        System.out.println("Schema1");
        print(a, b, h, exact, kit1.schema1(), kit2.schema1());

        System.out.println("Schema2");
        print(a, b, h, exact, kit1.schema2(), kit2.schema2());

        System.out.println("Compact1");
        print(a, b, h, exact, kit1.compact1(), kit2.compact1());

        System.out.println("Compact2");
        print(a, b, h, exact, kit1.compact2(), kit2.compact2());

        System.out.println("Compact3");
        print(a, b, h, exact, kit1.compact3(), kit2.compact3());

    }

    public static void print(double a, double b, double h, Func exact, Func schema1, Func schema2) {
        int n = (int) ((b - a) / h);
        for(int j = 0; j < n; j += 1) {
            double xj = a + j * h;
            double yex = exact.apply(xj);

            double yh1 = schema1.apply(xj);
            double yh2 = schema2.apply(xj);

            double dyh1 = Math.abs(yex - yh1);
            double dyh2 = Math.abs(yex - yh2);

            double pj = Math.log(dyh1 / dyh2) / Math.log(3);

            System.out.printf("%2d %9.8f ex=%9.8f dyh1=%9.8f dyh2=%9.8f pj=%10.8f%n", j, xj, yex, dyh1, dyh2, pj);
        }
    }

}