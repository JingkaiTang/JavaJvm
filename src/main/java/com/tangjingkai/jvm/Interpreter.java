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
            loop(thread);
        } catch (Exception e) {
            e.printStackTrace();
            //catchErr(thread);
        }
    }

    private void catchErr(Thread thread) {
        while (!thread.isStackEmpty()) {
            Frame frame = thread.popFrame();
            JJvmMethod method = frame.getMethod();
            String className = method.getJJvmClass().getName();
            System.out.println(String.format(">> pc:%04d %s.%s%s", frame.getNextPC(), className, method.getName(), method.getDescriptor()));
        }
    }

    private void  logInst(Frame frame, Instruction inst) {
        JJvmMethod method = frame.getMethod();
        String className = method.getJJvmClass().getName();
        int pc = frame.getThread().getPC();
        System.out.println(String.format("%s.%s() #%02d", className, method.getName(), pc));
    }

    private void loop(Thread thread) {
        BytecodeReader reader = new BytecodeReader();
        while (true) {
            Frame frame = thread.currentFrame();
            int pc = frame.getNextPC();
            thread.setPC(pc);

            reader.reset(frame.getMethod().getCode(), pc);
            byte opcode = reader.readU1();
            Instruction inst = Instructions.decode(opcode);
            inst.fetchOperands(reader);
            frame.setNextPC(reader.getPC());

            //logInst(frame, inst);

            inst.execute(frame);
            if (thread.isStackEmpty()) {
                break;
            }
        }
    }
}
