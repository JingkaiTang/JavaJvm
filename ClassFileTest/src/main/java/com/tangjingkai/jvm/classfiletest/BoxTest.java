package com.tangjingkai.jvm.classfiletest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by totran on 11/30/16.
 */
public class BoxTest {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        System.out.println(list.toString());
        for (int x : list) {
            System.out.println(x);
        }
    }
}
