package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class StringCI implements ConstantInfo {
    ConstantPool constantPool;
    short stringIndex;

    public StringCI(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    @Override
    public void readInfo(ClassReader reader) {
        stringIndex = reader.readU2();
    }

    public String getString() {
        return constantPool.getUtf8(stringIndex);
    }
}
