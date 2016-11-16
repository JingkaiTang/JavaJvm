package com.tangjingkai.jvm.rtda;

/**
 * Created by totran on 11/16/16.
 */
public class Frame {
    LocalVars localVars;
    OperandStack operandStack;

    public Frame(int maxLocals, int maxStack) {
        localVars = new LocalVars(maxLocals);
        operandStack = new OperandStack(maxStack);
    }
}
