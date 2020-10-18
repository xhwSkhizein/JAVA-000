package demo.jmm;

/**
 * @author hongweixu
 * @since 2020-10-16 18:52
 */
public class Test {


    public static void main(String[] args) {
        new A();
    }
}

class B {
    public B() {
        foo();
    }

    public void foo() {
    }
}

class A extends B {
    int a = 10;

    @Override
    public void foo() {
        if (a == 10) {
            System.out.print("10");
        } else {
            System.out.print("0");
        }
    }
}
