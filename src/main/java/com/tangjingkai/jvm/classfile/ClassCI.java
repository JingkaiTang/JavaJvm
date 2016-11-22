package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class ClassCI implements ConstantInfo {
    ConstantPool constantPool;
    short nameIndex;

    public ClassCI(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    @Override
    public void readInfo(ClassReader reader) {
        nameIndex = reader.readU2();
    }

    public String getName() {
        return constantPool.getUtf8(nameIndex);
    }
}
