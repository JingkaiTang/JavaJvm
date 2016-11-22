package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.MethodRefCI;

/**
 * Created by totran on 11/19/16.
 */
public class MethodRef extends MemberRef {
    JJvmMethod method;
    public MethodRef(JJvmConstantPool constantPool, MethodRefCI constantInfo) {
        super(constantPool, constantInfo);
    }
}
