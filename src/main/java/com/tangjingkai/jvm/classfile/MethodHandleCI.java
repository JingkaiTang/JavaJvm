package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class MethodHandleCI implements ConstantInfo {
    byte refKind;
    short refIndex;

    @Override
    public void readInfo(ClassReader reader) {
        refKind = reader.readU1();
        refIndex = reader.readU2();
    }
}
