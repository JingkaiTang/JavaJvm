package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.MemberRefCI;

/**
 * Created by totran on 11/22/16.
 */
public class MethodLikeRef extends MemberRef {
    public MethodLikeRef(JJvmConstantPool constantPool, MemberRefCI memberRefCI) {
        super(constantPool, memberRefCI);
    }

    protected JJvmMethod lookupMethodInInterfaces(JJvmClass[] interfaces, String name, String descriptor) {
        if (interfaces == null) {
            return null;
        }

        for (JJvmClass i : interfaces) {
            if (i.methods == null) {
                return null;
            }
            for (JJvmMethod m : i.methods) {
                if (m.name.equals(name) && m.descriptor.equals(descriptor)) {
                    return m;
                }
            }
            JJvmMethod m = lookupMethodInInterfaces(i.interfaces, name, descriptor);
            if (m != null) {
                return m;
            }
        }
        return null;
    }
}
