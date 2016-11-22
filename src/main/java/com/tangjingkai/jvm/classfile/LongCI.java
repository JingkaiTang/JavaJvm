package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class LongCI implements ConstantInfo {
    long val;

    @Override
    public void readInfo(ClassReader reader) {
        long highBytes = Integer.toUnsignedLong(reader.readU4());
        long lowBytes = Integer.toUnsignedLong(reader.readU4());
        val = (highBytes << 32) | lowBytes;
    }

    public long getVal() {
        return val;
    }
}
