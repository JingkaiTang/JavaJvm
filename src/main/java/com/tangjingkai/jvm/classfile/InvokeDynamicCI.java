package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class InvokeDynamicCI implements ConstantInfo {
    short bootstrapMethodAttrIndex;
    short nameAndTypeIndex;

    @Override
    public void readInfo(ClassReader reader) {
        bootstrapMethodAttrIndex = reader.readU2();
        nameAndTypeIndex = reader.readU2();
    }
}
