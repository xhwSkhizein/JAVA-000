package demo.jvm01;

import java.util.function.BiFunction;

/**
 * @author hongweixu
 * @since 2020-10-15 21:11
 */
public class DemoWithMoreTest {
    public static void foo() {
        int a = 1000;
        int b = 2;
        int c = (a + b) * 512;
    }

    public void barDouble() {
        double a = 10.32D;
    }

    public void barLong() {
        long b = 91L;
    }

    public double barCalculate(Number a, Number b, BiFunction<Number, Number, Double> calculator) {
        Double result = calculator.apply(a, b);
        return result;
    }

    public static void main(String[] args) {
        DemoWithMoreTest demo = new DemoWithMoreTest();

        double result = demo.barCalculate(10L, 5.D, (a, b) -> a.doubleValue() * b.doubleValue());

        System.out.println(result);
    }

}
