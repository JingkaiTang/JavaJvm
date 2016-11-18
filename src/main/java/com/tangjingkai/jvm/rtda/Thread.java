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

    public Frame buildFrame(short maxLocals, short maxStack) {
        return new Frame(this, maxLocals, maxStack);
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
