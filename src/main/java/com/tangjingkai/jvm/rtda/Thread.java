package com.tangjingkai.jvm.rtda;

/**
 * Created by totran on 11/16/16.
 */
public class Thread {
    int pc;
    Stack<Frame> stack;

    public Thread() {
        stack = new Stack<>(1024);
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

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }
}
