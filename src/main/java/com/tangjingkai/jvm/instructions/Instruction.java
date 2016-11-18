package com.tangjingkai.jvm.instructions;

import com.tangjingkai.jvm.rtda.Frame;

/**
 * Created by totran on 11/17/16.
 */
public interface Instruction {
    void fetchOperands(BytecodeReader reader);

    void execute(Frame frame);
}
