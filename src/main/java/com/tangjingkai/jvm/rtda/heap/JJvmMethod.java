package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.CodeAttribute;
import com.tangjingkai.jvm.classfile.MemberInfo;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmMethod extends JJvmClassMember {
    int maxStack;
    int maxLocals;
    byte[] code;

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
}
