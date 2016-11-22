package com.tangjingkai.jvm.rtda.heap;

/**
 * Created by totran on 11/19/16.
 */
public class SymRef {
    JJvmConstantPool constantPool;
    String className;
    JJvmClass jjvmClass;

    public JJvmClass resolvedClass() {
        if (jjvmClass == null) {
            resolveClassRef();
        }
        return jjvmClass;
    }

    private void resolveClassRef() {
        JJvmClass dClass = constantPool.jjvmClass;
        JJvmClass cClass = dClass.loader.loadClass(className);
        if (!cClass.isAccessibleTo(dClass)) {
            throw new RuntimeException("java.lang.IllegalAccessError");
        }
        this.jjvmClass = cClass;
    }
}
