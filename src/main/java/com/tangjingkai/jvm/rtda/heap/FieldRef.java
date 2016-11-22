package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.FieldRefCI;

/**
 * Created by totran on 11/19/16.
 */
public class FieldRef extends MemberRef {
    JJvmField field;

    public FieldRef(JJvmConstantPool constantPool, FieldRefCI constantInfo) {
        super(constantPool, constantInfo);
    }

    public JJvmField resolvedField() {
        if (field == null) {
            resolveFieldRef();
        }
        return field;
    }

    private void resolveFieldRef() {
        JJvmClass dClass = constantPool.jjvmClass;
        JJvmClass cClass = resolvedClass();
        JJvmField field = lookupField(cClass, name, descriptor);

        if (field == null) {
            throw new RuntimeException("java.lang.NoSunFieldError");
        }

        if (!field.isAccessibleTo(dClass)) {
            throw new RuntimeException("java.lang.IllegalAccessError");
        }

        this.field = field;
    }

    private JJvmField lookupField(JJvmClass cls, String name, String descriptor) {
        if (cls.fields != null) {
            for (JJvmField field : cls.fields) {
                if (field.name.equals(name) && field.descriptor.equals(descriptor)) {
                    return field;
                }
            }
        }

        if (cls.interfaces != null) {
            for (JJvmClass intfs: cls.interfaces) {
                JJvmField field = lookupField(intfs, name, descriptor);
                if (field != null) {
                    return field;
                }
            }
        }

        if (cls.superClass != null) {
            return lookupField(cls.superClass, name, descriptor);
        }

        return null;
    }
}
