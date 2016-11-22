package com.tangjingkai.jvm.rtda;

import com.tangjingkai.jvm.rtda.heap.JJvmMethod;

/**
 * Created by totran on 11/16/16.
 */
public class Thread {
    int pc;
    Stack<Frame> stack;

    public Thread() {
        stack = new Stack<>(1024);
    }

    public Frame buildFrame(JJvmMethod method) {
        return new Frame(this, method);
    }

    public void pushFrame(Frame frame) {
        stack.push(frame);
    }

    public Frame popFrame() {
        return stack.pop();
    }

    public Frame currentFrame() {
        return stack.top();
    }

    public int getPC() {
        return pc;
    }

    public void setPC(int pc) {
        this.pc = pc;
    }
}
