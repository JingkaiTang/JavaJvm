package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class DoubleCI implements ConstantInfo {
    double val;

    @Override
    public void readInfo(ClassReader reader) {
        long highBytes = Integer.toUnsignedLong(reader.readU4());
        long lowBytes = Integer.toUnsignedLong(reader.readU4());
        val = Double.longBitsToDouble((highBytes << 32) | lowBytes);
    }

    public double getVal() {
        return val;
    }
}
