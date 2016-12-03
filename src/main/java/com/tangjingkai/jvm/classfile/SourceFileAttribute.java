package com.tangjingkai.jvm.classfile;

/**
 * Created by totran on 11/17/16.
 */
public class SourceFileAttribute implements AttributeInfo {
    ConstantPool constantPool;
    short sourceFileIndex;

    public SourceFileAttribute(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    @Override
    public void readInfo(ClassReader reader) {
        sourceFileIndex = reader.readU2();
    }

    public String getFileName() {
        return constantPool.getUtf8(sourceFileIndex);
    }
}
