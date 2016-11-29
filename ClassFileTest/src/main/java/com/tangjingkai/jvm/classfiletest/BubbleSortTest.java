package com.tangjingkai.jvm.classfiletest;

/**
 * Created by totran on 11/29/16.
 */
public class BubbleSortTest {
    public static void main(String[] args) {
        int[] arr = {22, 84, 77, 11, 95, 9, 78, 56, 36, 97, 65, 36, 10, 24, 92, 48};
        bubbleSort(arr);
        printArray(arr);
    }

    private static void printArray(int[] arr) {
        for (int i: arr) {
            System.out.println(i);
        }
    }

    private static void bubbleSort(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 1; j < arr.length-i; j++) {
                if (arr[j-1] > arr[j]) {
                    int tmp = arr[j-1];
                    arr[j-1] = arr[j];
                    arr[j] = tmp;
                }
            }
        }
    }
}
