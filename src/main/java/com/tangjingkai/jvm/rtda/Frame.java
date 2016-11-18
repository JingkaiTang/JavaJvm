package com.tangjingkai.jvm.rtda;

/**
 * Created by totran on 11/16/16.
 */
public class Frame {
    LocalVars localVars;
    OperandStack operandStack;
    Thread thread;
    int nextPC;

    public int getNextPC() {
        return nextPC;
    }

    public Frame(Thread thread, int maxLocals, int maxStack) {
        this.localVars = new LocalVars(maxLocals);
        this.operandStack = new OperandStack(maxStack);
        this.thread = thread;
    }

    public Frame(Thread thread, short maxLocals, short maxStack) {
        this(thread, Short.toUnsignedInt(maxLocals), Short.toUnsignedInt(maxStack));
    }

    public void setNextPC(int nextPC) {
        this.nextPC = nextPC;
    }

    public Thread getThread() {
        return thread;
    }

    public LocalVars getLocalVars() {
        return localVars;
    }

    public OperandStack getOperandStack() {
        return operandStack;
    }
}
