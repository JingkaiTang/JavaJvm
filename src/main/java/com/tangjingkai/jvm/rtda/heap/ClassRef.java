package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.ClassCI;

/**
 * Created by totran on 11/19/16.
 */
public class ClassRef extends SymRef {
    public ClassRef(JJvmConstantPool constantPool, ClassCI constantInfo) {
        this.constantPool = constantPool;
        this.className = constantInfo.getName();
    }
}
