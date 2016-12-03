package com.tangjingkai.jvm.rtda.heap;

/**
 * Created by totran on 12/1/16.
 */
public class ExceptionHandler {
    int startPC;
    int endPC;
    int handlerPC;
    ClassRef catchType;

    public ExceptionHandler(int startPC, int endPC, int handlerPC, ClassRef catchType) {
        this.startPC = startPC;
        this.endPC = endPC;
        this.handlerPC = handlerPC;
        this.catchType = catchType;
    }

    public int getHandlerPC() {
        return 0;
    }
}
