package com.tangjingkai.jvm.rtda;

import com.tangjingkai.jvm.rtda.heap.JJvmMethod;

/**
 * Created by totran on 11/16/16.
 */
public class Frame {
    LocalVars localVars;
    OperandStack operandStack;
    Thread thread;
    JJvmMethod method;
    int nextPC;

    public Frame(Thread thread, JJvmMethod method) {
        this.thread = thread;
        this.method = method;
        this.localVars = new LocalVars(method.getMaxLocals());
        this.operandStack = new OperandStack(method.getMaxStack());
    }

    public JJvmMethod getMethod() {
        return method;
    }

    public int getNextPC() {
        return nextPC;
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
