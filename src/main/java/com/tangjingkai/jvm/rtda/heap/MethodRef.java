package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.MethodRefCI;

/**
 * Created by totran on 11/19/16.
 */
public class MethodRef extends MethodLikeRef {
    JJvmMethod method;
    public MethodRef(JJvmConstantPool constantPool, MethodRefCI constantInfo) {
        super(constantPool, constantInfo);
    }

    public JJvmMethod resolvedMethod() {
        if (method == null) {
            resolveMethodRef();
        }
        return method;
    }

    private void resolveMethodRef() {
        JJvmClass cls = resolvedClass();
        if (cls.isInterface()) {
            throw new IncompatibleClassChangeError();
        }

        JJvmMethod m = lookupMethod(cls, name, descriptor);
        if (m == null) {
            throw new NoSuchMethodError();
        }

        if (!m.isAccessibleTo(constantPool.jjvmClass)) {
            throw new IllegalAccessError();
        }

        this.method = m;
    }

    private JJvmMethod lookupMethod(JJvmClass cls, String name, String descriptor) {
        JJvmMethod m = lookupMethodInClass(cls, name, descriptor);
        if (m == null) {
            m = lookupMethodInInterfaces(cls.interfaces, name, descriptor);
        }
        return m;
    }

    public static JJvmMethod lookupMethodInClass(JJvmClass cls, String name, String descriptor) {
        for (JJvmClass c = cls; c != null; c = c.superClass) {
            if (c.methods == null) {
                continue;
            }
            for (JJvmMethod m: c.methods) {
                if (m.name.equals(name) && m.descriptor.equals(descriptor)) {
                    return m;
                }
            }
        }
        return null;
    }
}
