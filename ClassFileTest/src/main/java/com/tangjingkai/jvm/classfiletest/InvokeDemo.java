package com.tangjingkai.jvm.classfiletest;

/**
 * Created by totran on 11/23/16.
 */
public class InvokeDemo implements Runnable {
    public void run() {

    }

    public static void main(String[] args) {
        new InvokeDemo().test();
    }

    public void test() {
        InvokeDemo.staticMethod();
        InvokeDemo demo = new InvokeDemo();
        demo.instanceMethod();
        super.equals(null);
        this.run();
        ((Runnable) demo).run();
    }

    public static void staticMethod() {

    }

    private void instanceMethod() {

    }
}
