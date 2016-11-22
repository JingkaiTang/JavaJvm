package com.tangjingkai.jvm.rtda.heap;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmObject {
    JJvmClass cls;
    JJvmSlots fields;

    public JJvmClass getJJvmClass() {
        return cls;
    }

    public JJvmSlots getFields() {
        return fields;
    }

    public JJvmObject(JJvmClass jjvmClass) {
        this.cls = jjvmClass;
        this.fields = new JJvmSlots(jjvmClass.instanceSlotCount);
    }

    public boolean isInstanceOf(JJvmClass jjvmClass) {
        return cls.isAssignableFrom(cls);
    }
}
