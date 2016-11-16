package com.tangjingkai.jvm.rtda;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by totran on 11/16/16.
 */
public class Stack<E> {
    int maxSize;
    Deque<E> deque;
    public Stack(int maxSize) {
        this.maxSize = maxSize;
        deque = new ArrayDeque<>();
    }

    public void push(E e) {
        if (deque.size() >= maxSize) {
            throw new RuntimeException("java.lang.StackOverflowError");
        }
        deque.push(e);
    }

    public E pop() {
        if (deque.size() == 0) {
            throw new RuntimeException("jvm stack is empty!");
        }
        return deque.pop();
    }

    public E top() {
        if (deque.size() == 0) {
            throw new RuntimeException("jvm stack is empty!");
        }
        return deque.peek();
    }
}
