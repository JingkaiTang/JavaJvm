package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/19/16.
 */
public class NameAndTypeCI implements ConstantInfo {
    short nameIndex;
    short descriptorIndex;

    @Override
    public void readInfo(ClassReader reader) {
        nameIndex = reader.readU2();
        descriptorIndex = reader.readU2();
    }

    public static class NTValue {
        public String name;
        public String type;
    }
}
