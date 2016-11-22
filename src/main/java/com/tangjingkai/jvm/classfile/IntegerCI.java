package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class IntegerCI implements ConstantInfo {
    int val;

    public int getVal() {
        return val;
    }

    @Override
    public void readInfo(ClassReader reader) {
        val = reader.readU4();
    }
}
