package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class MethodTypeCI implements ConstantInfo {
    short descriptorIndex;

    @Override
    public void readInfo(ClassReader reader) {
        descriptorIndex = reader.readU2();
    }
}
