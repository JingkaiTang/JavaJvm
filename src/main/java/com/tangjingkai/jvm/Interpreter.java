package com.tangjingkai.jvm;

import com.tangjingkai.jvm.instructions.BytecodeReader;
import com.tangjingkai.jvm.instructions.Instruction;
import com.tangjingkai.jvm.instructions.Instructions;
import com.tangjingkai.jvm.rtda.Frame;
import com.tangjingkai.jvm.rtda.Thread;
import com.tangjingkai.jvm.rtda.heap.JJvmMethod;

/**
 * Created by totran on 11/17/16.
 */
public class Interpreter {
    public void interpret(JJvmMethod method) {
        Thread thread = new Thread();
        Frame frame = thread.buildFrame(method);
        thread.pushFrame(frame);

        try {
            loop(thread, method.getCode());
        } catch (Exception e) {
            e.printStackTrace();
            catchErr(frame);
        }
    }

    private void catchErr(Frame frame) {
        System.out.println(frame.getLocalVars());
        System.out.println(frame.getOperandStack());
    }

    private void loop(Thread thread, byte[] bytecode) {
        Frame frame = thread.popFrame();
        BytecodeReader reader = new BytecodeReader();
        while (true) {
            int pc = frame.getNextPC();
            thread.setPC(pc);
            reader.reset(bytecode, pc);
            byte opcode = reader.readU1();
            Instruction inst = Instructions.decode(opcode);
            inst.fetchOperands(reader);
            frame.setNextPC(reader.getPC());

            System.out.println(String.format("PC:%2d inst:%s", pc, inst.getClass().getSimpleName()));
            inst.execute(frame);
        }
    }
}
