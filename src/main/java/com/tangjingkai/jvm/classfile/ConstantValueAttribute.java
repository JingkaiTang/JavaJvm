package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/17/16.
 */
public class ConstantValueAttribute implements AttributeInfo {
    short constantValueIndex;

    @Override
    public void readInfo(ClassReader reader) {
        constantValueIndex = reader.readU2();
    }

    public short getConstantValueIndex() {
        return constantValueIndex;
    }
}
