package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class Utf8CI implements ConstantInfo {
    String str;

    @Override
    public void readInfo(ClassReader reader) {
        int length = Short.toUnsignedInt(reader.readU2());
        byte[] data = reader.readBytes(length);
        str = new String(data);
    }
}
