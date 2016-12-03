package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.ExceptionsAttribute;

/**
 * Created by totran on 12/1/16.
 */
public class ExceptionTable {
    ExceptionHandler[] handlers;

    public ExceptionTable(ExceptionsAttribute.ExceptionTableEntry[] entries, JJvmConstantPool cp) {
        handlers = new ExceptionHandler[entries.length];
        for (int i = 0; i < handlers.length; i++) {
            ExceptionsAttribute.ExceptionTableEntry entry = entries[i];
            handlers[i] = new ExceptionHandler(entry.getStartPc(), entry.getEndPc(), entry.getHandlerPc(), getCatchType(Short.toUnsignedInt(entry.getCatchType()), cp));
        }
    }

    private ClassRef getCatchType(int index, JJvmConstantPool cp) {
        if (index == 0) {
            return null;
        }
        return (ClassRef) cp.getConstant(index);
    }

    public ExceptionHandler findExceptionHandler(JJvmClass exClass, int pc) {
        for (ExceptionHandler handler : handlers) {
            if (pc >= handler.startPC && pc < handler.endPC) {
                if (handler.catchType == null) {
                    return handler;
                }
                JJvmClass catchClass = handler.catchType.resolvedClass();
                if (catchClass == exClass || catchClass.isSubClassOf(exClass)) {
                    return handler;
                }
            }
        }
        return null;
    }
}
