package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/17/16.
 */
public class UnparsedAttribute implements AttributeInfo {
    String name;
    int length;
    byte[] info;

    public UnparsedAttribute(String name, int length) {
        this.name = name;
        this.length = length;
    }

    @Override
    public void readInfo(ClassReader reader) {
        info = reader.readBytes(length);
    }
}
