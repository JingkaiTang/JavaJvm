package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.InterfaceMethodRefCI;

/**
 * Created by totran on 11/19/16.
 */
public class InterfaceMethodRef extends MethodLikeRef {
    JJvmMethod method;
    public InterfaceMethodRef(JJvmConstantPool constantPool, InterfaceMethodRefCI constantInfo) {
        super(constantPool, constantInfo);
    }

    public JJvmMethod resolvedInterfaceMethod() {
        if (method == null) {
            resolveInterfaceMethodRef();
        }
        return method;
    }

    private void resolveInterfaceMethodRef() {
        JJvmClass cls = resolvedClass();
        if (!cls.isInterface()) {
            throw new IncompatibleClassChangeError();
        }

        JJvmMethod m = lookupInterfaceMethod(cls, name, descriptor);
        if (m == null) {
            throw new NoSuchMethodError();
        }

        if (!m.isAccessibleTo(constantPool.jjvmClass)) {
            throw new IllegalAccessError();
        }

        this.method = m;
    }

    private JJvmMethod lookupInterfaceMethod(JJvmClass iface, String name, String descriptor) {
        if (iface.methods == null) {
            return null;
        }

        for (JJvmMethod m: iface.methods) {
            if (m.name.equals(name) && m.descriptor.equals(descriptor)) {
                return m;
            }
        }

        return lookupMethodInInterfaces(iface.interfaces, name, descriptor);
    }
}
