package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.InterfaceMethodRefCI;

/**
 * Created by totran on 11/19/16.
 */
public class InterfaceMethodRef extends MemberRef {
    JJvmMethod method;
    public InterfaceMethodRef(JJvmConstantPool constantPool, InterfaceMethodRefCI constantInfo) {
        super(constantPool, constantInfo);
    }
}
