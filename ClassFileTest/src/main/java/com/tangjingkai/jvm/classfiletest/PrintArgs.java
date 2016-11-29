package com.tangjingkai.jvm.classfiletest;

/**
 * Created by totran on 11/29/16.
 */
public class PrintArgs {
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
    }
}
