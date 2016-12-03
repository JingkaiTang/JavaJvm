package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.CodeAttribute;
import com.tangjingkai.jvm.classfile.LineNumberTableAttribute;
import com.tangjingkai.jvm.classfile.MemberInfo;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmMethod extends JJvmClassMember {
    int maxStack;
    int maxLocals;
    byte[] code;
    int argSlotCount;
    ExceptionTable exceptionTable;
    LineNumberTableAttribute lineNumberTable;

    public int getArgSlotCount() {
        return argSlotCount;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public byte[] getCode() {
        return code;
    }

    protected JJvmMethod(JJvmClass jjvmClass, MemberInfo method) {
        super(jjvmClass, method);
        CodeAttribute codeAttr = method.getCodeAttribute();
        if (codeAttr != null) {
            this.maxStack = Short.toUnsignedInt(codeAttr.getMaxStack());
            this.maxLocals = Short.toUnsignedInt(codeAttr.getMaxLocals());
            this.code = codeAttr.getCode();
            this.lineNumberTable = codeAttr.getLineNumberTableAttribute();
            this.exceptionTable = new ExceptionTable(codeAttr.getExceptionTable(), this.jjvmClass.getConstantPool());
        }
        this.argSlotCount = 0;
        JJvmMethodDescriptor md = JJvmMethodDescriptor.parse(method.getDescriptor());
        md.parameterTypes.forEach(s -> {
            this.argSlotCount++;
            if (s.equals("J") || s.equals("D")) {
                this.argSlotCount++;
            }
        });
        if (!isStatic()) {
            this.argSlotCount++;
        }
        if (isNative()) {
            // inject code for native method
            this.maxStack = 4;
            this.maxLocals = this.argSlotCount;
            switch (md.returnType.charAt(0)) {
                case 'V':
                    // return
                    this.code = new byte[] {(byte) 0xfe, (byte) 0xb1};
                    break;
                case 'D':
                    // dreturn
                    this.code = new byte[] {(byte) 0xfe, (byte) 0xaf};
                    break;
                case 'F':
                    // freturn
                    this.code = new byte[] {(byte) 0xfe, (byte) 0xae};
                    break;
                case 'J':
                    // lreturn
                    this.code = new byte[] {(byte) 0xfe, (byte) 0xad};
                    break;
                case 'L':
                case '[':
                    // areturn
                    this.code = new byte[] {(byte) 0xfe, (byte) 0xb0};
                    break;
                default:
                    // ireturn
                    this.code = new byte[] {(byte) 0xfe, (byte) 0xac};
            }
        }
    }

    public static JJvmMethod[] extractMethods(JJvmClass jjvmClass, MemberInfo[] methods) {
        if (methods == null) {
            return null;
        }

        JJvmMethod[] jjvmMethods = new JJvmMethod[methods.length];
        for (int i = 0; i < methods.length; i++) {
            jjvmMethods[i] = new JJvmMethod(jjvmClass, methods[i]);
        }
        return jjvmMethods;
    }

    public int findExceptionHandler(JJvmClass exClass, int pc) {
        ExceptionHandler handler = this.exceptionTable.findExceptionHandler(exClass, pc);
        if (handler != null) {
            return handler.getHandlerPC();
        }
        return -1;
    }

    public boolean isSynchronized() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_SYNCHRONIZED);
    }

    public boolean isBridge() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_BRIDGE);
    }

    public boolean isVarargs() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_VARARGS);
    }

    public boolean isNative() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_NATIVE);
    }

    public boolean isStrict() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_STRICT);
    }

    public boolean isAbstract() {
        return 0 != (accessFlags & JJvmAccessFlag.ACC_ABSTRACT);
    }

    public int getLineNumber(int pc) {
        if (isNative()) {
            return -2;
        }
        if (lineNumberTable == null) {
            return -1;
        }
        return lineNumberTable.getLineNumber(pc);
    }
}
