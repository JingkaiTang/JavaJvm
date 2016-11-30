package com.tangjingkai.jvm.classfiletest;

/**
 * Created by totran on 11/30/16.
 */
public class ObjectTest {
    public static void main(String[] args) {
        Object o1 = new ObjectTest();
        Object o2 = new ObjectTest();
        System.out.println(o1.hashCode());
        System.out.println(o2.toString());
        System.out.println(o1.equals(o2));
        System.out.println(o1.equals(o1));
    }
}
