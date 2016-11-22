package com.tangjingkai.jvm.rtda.heap;

import com.tangjingkai.jvm.classfile.*;

/**
 * Created by totran on 11/19/16.
 */
public class JJvmConstantPool {
    JJvmClass jjvmClass;
    Object[] constants;

    public JJvmConstantPool(JJvmClass jjvmClass, ConstantPool constantPool) {
        this.jjvmClass = jjvmClass;
        int cpCount = constantPool.getConstantInfos().length;
        this.constants = new Object[cpCount];
        for (int i = 1; i < cpCount; i++) {
            ConstantInfo constantInfo = constantPool.getConstantInfos()[i];
            if (constantInfo instanceof IntegerCI) {
                this.constants[i] = ((IntegerCI) constantInfo).getVal();
            } else if (constantInfo instanceof FloatCI) {
                this.constants[i] = ((FloatCI) constantInfo).getVal();
            } else if (constantInfo instanceof LongCI) {
                this.constants[i] = ((LongCI) constantInfo).getVal();
                i++;
            } else if (constantInfo instanceof DoubleCI) {
                this.constants[i] = ((DoubleCI) constantInfo).getVal();
                i++;
            } else if (constantInfo instanceof StringCI) {
                this.constants[i] = ((StringCI) constantInfo).getString();
            } else if (constantInfo instanceof ClassCI) {
                this.constants[i] = new ClassRef(this, (ClassCI) constantInfo);
            } else if (constantInfo instanceof FieldRefCI) {
                this.constants[i] = new FieldRef(this, (FieldRefCI) constantInfo);
            } else if (constantInfo instanceof MethodRefCI) {
                this.constants[i] = new MethodRef(this, (MethodRefCI) constantInfo);
            } else if (constantInfo instanceof InterfaceMethodRefCI) {
                this.constants[i] = new InterfaceMethodRef(this, (InterfaceMethodRefCI) constantInfo);
            } else {
                //throw new RuntimeException("Unsupported Constant Info found!");
            }
        }
    }

    public Object getConstant(int index) {
        return constants[index];
    }
}
