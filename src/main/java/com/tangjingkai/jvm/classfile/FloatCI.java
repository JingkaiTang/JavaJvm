package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class FloatCI implements ConstantInfo {
    float val;

    @Override
    public void readInfo(ClassReader reader) {
        int bytes = reader.readU4();
        val = Float.intBitsToFloat(bytes);
    }

    public float getVal() {
        return val;
    }
}
